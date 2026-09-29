package org.wpcleaner.application.gui.javafx.recentchanges.userpagehosting;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.wpcleaner.api.api.edit.EditQueryByTitle;
import org.wpcleaner.api.api.edit.EditQueryCommon;
import org.wpcleaner.api.api.query.list.tags.Tag;
import org.wpcleaner.api.api.query.meta.tokens.Tokens;
import org.wpcleaner.api.api.query.meta.tokens.TokensParameters;
import org.wpcleaner.api.api.query.prop.revisions.Page;
import org.wpcleaner.api.api.query.prop.revisions.RevisionSlot;
import org.wpcleaner.api.api.query.prop.revisions.RevisionsParameters;
import org.wpcleaner.api.api.query.prop.revisions.RevisionsQuery;
import org.wpcleaner.api.repository.namespace.CommonNamespaces;
import org.wpcleaner.api.repository.namespace.Namespace;
import org.wpcleaner.api.settings.SettingsPersistence;
import org.wpcleaner.api.utils.GT;
import org.wpcleaner.api.utils.JsonUtils;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.application.gui.javafx.core.window.JavaFxWindow;
import org.wpcleaner.application.gui.javafx.recentchanges.FilteredRecentChange;
import org.wpcleaner.application.gui.javafx.recentchanges.JavaFxRecentChangesWindowServices;
import org.wpcleaner.application.gui.javafx.recentchanges.RecentChangesAction;

public final class UserPageHostingAction implements RecentChangesAction {

  private final JavaFxRecentChangesWindowServices services;
  private final JavaFxWindow<?> owner;

  public UserPageHostingAction(
      final JavaFxRecentChangesWindowServices services, final JavaFxWindow<?> owner) {
    this.services = services;
    this.owner = Objects.requireNonNull(owner);
  }

  @Override
  public boolean canApply(final FilteredRecentChange rc) {
    if (rc.ns() == null || rc.ns() != CommonNamespaces.USER.id) {
      return false;
    }
    final int colon = rc.title().indexOf(':');
    final String titleWithoutNamespace = colon >= 0 ? rc.title().substring(colon + 1) : rc.title();
    return !titleWithoutNamespace.contains("/");
  }

  @Override
  public void apply(final FilteredRecentChange rc) {
    if (!canApply(rc)) {
      return;
    }
    final WikiDefinition wiki = services.user().getCurrentUser().wiki();
    final String wikiCode = wiki.code();

    final Optional<UserPageHostingConfig> configOpt = loadConfig(wikiCode);
    if (configOpt.isEmpty() || !configOpt.get().isComplete()) {
      new UserPageHostingConfigWindow(
          services,
          owner.getStage(),
          configOpt.orElse(null),
          newConfig -> {
            saveConfig(wikiCode, newConfig);
            promptActionWindow(rc, wiki, newConfig);
          });
      return;
    }

    promptActionWindow(rc, wiki, configOpt.get());
  }

  private void promptActionWindow(
      final FilteredRecentChange rc,
      final WikiDefinition wiki,
      final UserPageHostingConfig config) {
    final int colon = rc.title().indexOf(':');
    final String username = colon >= 0 ? rc.title().substring(colon + 1) : rc.title();
    final Namespace userTalkNamespace =
        services.namespaceRepository().getNamespaces().stream()
            .filter(ns -> ns.id() == CommonNamespaces.USER_TALK.id)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("USER_TALK namespace not found"));
    final String userTalkPageTitle = userTalkNamespace.name() + ":" + username;

    final String userPageContent = retrievePageContent(wiki, rc.title());
    final String userTalkPageContent = retrievePageContent(wiki, userTalkPageTitle);

    new UserPageHostingActionWindow(
        services,
        owner.getStage(),
        config,
        rc.title(),
        userPageContent,
        userTalkPageTitle,
        userTalkPageContent,
        actionParams -> executeAction(wiki, rc.title(), userTalkPageTitle, config, actionParams));
  }

  private void executeAction(
      final WikiDefinition wiki,
      final String userPageTitle,
      final String userTalkPageTitle,
      final UserPageHostingConfig config,
      final UserPageHostingActionParams actionParams) {
    final List<Tag> wikiTags = services.tagRepository().getTags();
    final Optional<String> wpcleanerTag =
        wikiTags.stream().map(Tag::name).filter("wpcleaner"::equalsIgnoreCase).findFirst();
    final List<String> tagsToUse = wpcleanerTag.map(List::of).orElse(null);

    final Tokens tokens =
        services.apiTokens().requestTokens(wiki, List.of(TokensParameters.Type.CSRF));
    final String csrfToken = tokens.csrf();
    if (csrfToken == null) {
      owner.showError(
          GT._T("Error"),
          GT._T("Unable to retrieve CSRF token"),
          GT._T("Please check your connection and login status."));
      return;
    }

    services
        .apiEdit()
        .edit(
            wiki,
            new EditQueryByTitle(
                userPageTitle,
                EditQueryCommon.emptyBuilder()
                    .text(config.userPageText())
                    .summary(actionParams.userPageComment())
                    .token(csrfToken)
                    .tags(tagsToUse)
                    .build()));

    if (!actionParams.selectedTalkPageTexts().isEmpty()) {
      final String textToAppend =
          "\n\n" + String.join("\n\n", actionParams.selectedTalkPageTexts());
      services
          .apiEdit()
          .edit(
              wiki,
              new EditQueryByTitle(
                  userTalkPageTitle,
                  EditQueryCommon.emptyBuilder()
                      .appendText(textToAppend)
                      .summary(actionParams.userTalkPageComment())
                      .token(csrfToken)
                      .tags(tagsToUse)
                      .build()));
    }

    owner.showInformation(
        GT._T("Success"),
        GT._T("Action completed successfully"),
        GT._T("The user page has been replaced and the talk page updated."));
  }

  private File getConfigFile(final String wikiCode) {
    final Path path = SettingsPersistence.getFolder().resolve(wikiCode);
    try {
      Files.createDirectories(path);
    } catch (final IOException e) {
      throw new IllegalStateException(e);
    }
    return path.resolve("rc-userpage-hosting.json").toFile();
  }

  private Optional<UserPageHostingConfig> loadConfig(final String wikiCode) {
    final File file = getConfigFile(wikiCode);
    if (!file.exists()) {
      return Optional.empty();
    }
    try {
      final String json = Files.readString(file.toPath(), StandardCharsets.UTF_8);
      return Optional.ofNullable(JsonUtils.readValue(json, UserPageHostingConfig.class));
    } catch (final IOException e) {
      return Optional.empty();
    }
  }

  private void saveConfig(final String wikiCode, final UserPageHostingConfig config) {
    final File file = getConfigFile(wikiCode);
    JsonUtils.writeValue(file, config);
  }

  private String retrievePageContent(final WikiDefinition wiki, final String title) {
    final RevisionsQuery query =
        RevisionsQuery.emptyBuilder()
            .properties(
                Set.of(RevisionsParameters.Properties.CONTENT, RevisionsParameters.Properties.IDS))
            .slots(Set.of("main"))
            .build();
    final List<Page> pages =
        services.apiRevisions().retrieveRevisionsByTitle(wiki, List.of(title), query);
    if (pages.isEmpty()) {
      return "";
    }
    return pages.getFirst().revisions().stream()
        .findFirst()
        .map(revision -> revision.slots().get("main"))
        .filter(Objects::nonNull)
        .map(RevisionSlot::content)
        .orElse("");
  }
}
