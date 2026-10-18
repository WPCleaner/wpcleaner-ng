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
import java.util.function.Consumer;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.api.repository.namespace.CommonNamespaces;
import org.wpcleaner.api.repository.namespace.Namespace;
import org.wpcleaner.api.settings.SettingsPersistence;
import org.wpcleaner.api.utils.GT;
import org.wpcleaner.api.utils.JsonUtils;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.application.gui.javafx.core.action.content.RetrievePagesContentTask;
import org.wpcleaner.application.gui.javafx.core.window.JavaFxWindow;
import org.wpcleaner.application.gui.javafx.recentchanges.FilteredRecentChange;
import org.wpcleaner.application.gui.javafx.recentchanges.JavaFxRecentChangesWindowServices;
import org.wpcleaner.application.gui.javafx.recentchanges.RecentChangesConfigurableAction;

public final class UserPageHostingAction implements RecentChangesConfigurableAction {

  private final JavaFxRecentChangesWindowServices services;
  private final JavaFxWindow<?> owner;

  public UserPageHostingAction(
      final JavaFxRecentChangesWindowServices services, final JavaFxWindow<?> owner) {
    this.services = services;
    this.owner = Objects.requireNonNull(owner);
  }

  @Override
  public boolean canApply(@Nullable final FilteredRecentChange rc) {
    if (rc == null || rc.ns() == null || rc.ns() != CommonNamespaces.USER.id) {
      return false;
    }
    final int colon = rc.title().indexOf(':');
    final String titleWithoutNamespace = colon >= 0 ? rc.title().substring(colon + 1) : rc.title();
    return !titleWithoutNamespace.contains("/");
  }

  @Override
  public void apply(@Nullable final FilteredRecentChange rc) {
    if (rc == null || !canApply(rc)) {
      return;
    }
    final WikiDefinition wiki = services.user().getCurrentUser().wiki();
    final String wikiCode = wiki.code();

    final Optional<UserPageHostingConfig> configOpt = loadConfig(wikiCode);
    if (configOpt.isEmpty() || !configOpt.get().isComplete()) {
      showConfigWindow(
          wikiCode, configOpt.orElse(null), newConfig -> promptActionWindow(rc, wiki, newConfig));
      return;
    }

    promptActionWindow(rc, wiki, configOpt.get());
  }

  @Override
  public void configure() {
    final WikiDefinition wiki = services.user().getCurrentUser().wiki();
    final String wikiCode = wiki.code();
    final Optional<UserPageHostingConfig> configOpt = loadConfig(wikiCode);
    showConfigWindow(wikiCode, configOpt.orElse(null), _ -> {});
  }

  private void showConfigWindow(
      final String wikiCode,
      @Nullable final UserPageHostingConfig initialConfig,
      final Consumer<UserPageHostingConfig> afterSave) {
    new UserPageHostingConfigWindow(
        services,
        owner.getStage(),
        initialConfig,
        newConfig -> {
          saveConfig(wikiCode, newConfig);
          afterSave.accept(newConfig);
        });
  }

  private void promptActionWindow(
      final FilteredRecentChange rc,
      final WikiDefinition wiki,
      final UserPageHostingConfig config) {
    final Namespace userNamespace =
        Namespace.findNamespace(
                services.namespaceRepository().getNamespaces(), CommonNamespaces.USER.id)
            .orElseThrow(() -> new IllegalStateException("USER namespace not found"));
    final int colon = rc.title().indexOf(':');
    final String username = colon >= 0 ? rc.title().substring(colon + 1) : rc.title();
    final String userPageTitle =
        userNamespace.name() + ":" + userNamespace.caseType().normalize(username);
    final Namespace userTalkNamespace =
        Namespace.findNamespace(
                services.namespaceRepository().getNamespaces(), CommonNamespaces.USER_TALK.id)
            .orElseThrow(() -> new IllegalStateException("USER_TALK namespace not found"));
    final String userTalkPageTitle =
        userTalkNamespace.name() + ":" + userTalkNamespace.caseType().normalize(username);

    owner.executeAsync(
        new RetrievePagesContentTask(
            wiki, List.of(userPageTitle, userTalkPageTitle), services.apiRevisions()),
        pagesContent ->
            displayActionWindow(
                rc,
                wiki,
                config,
                userTalkPageTitle,
                pagesContent.getOrDefault(userPageTitle, ""),
                pagesContent.getOrDefault(userTalkPageTitle, "")),
        e ->
            owner.showError(
                GT._T(
                    "Error loading page contents: %s",
                    Objects.requireNonNullElseGet(
                        e.getMessage(), () -> e.getClass().getSimpleName()))));
  }

  private void displayActionWindow(
      final FilteredRecentChange rc,
      final WikiDefinition wiki,
      final UserPageHostingConfig config,
      final String userTalkPageTitle,
      final String userPageContent,
      final String userTalkPageContent) {
    new UserPageHostingActionWindow(
        services,
        owner.getStage(),
        wiki,
        config,
        rc.title(),
        userPageContent,
        userTalkPageTitle,
        userTalkPageContent);
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
}
