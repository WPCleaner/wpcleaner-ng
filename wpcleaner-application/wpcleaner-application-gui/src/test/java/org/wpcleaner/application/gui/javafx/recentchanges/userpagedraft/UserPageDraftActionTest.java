package org.wpcleaner.application.gui.javafx.recentchanges.userpagedraft;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.assertj.core.api.Assertions;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.wpcleaner.api.api.CurrentUserService;
import org.wpcleaner.api.api.edit.Edit;
import org.wpcleaner.api.api.edit.EditQueryByTitle;
import org.wpcleaner.api.api.edit.EditQueryCommon;
import org.wpcleaner.api.api.edit.MockApiEdit;
import org.wpcleaner.api.api.move.MockApiMove;
import org.wpcleaner.api.api.move.Move;
import org.wpcleaner.api.api.move.MoveQueryByTitle;
import org.wpcleaner.api.api.move.MoveQueryCommon;
import org.wpcleaner.api.api.query.meta.siteinfo.Namespace;
import org.wpcleaner.api.api.query.meta.tokens.MockApiTokens;
import org.wpcleaner.api.api.query.prop.revisions.MockApiRevisions;
import org.wpcleaner.api.repository.namespace.CommonNamespaces;
import org.wpcleaner.api.settings.SettingsPersistence;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.api.wiki.definition.WikipediaDefinitions;
import org.wpcleaner.application.gui.GuiLogin;
import org.wpcleaner.application.gui.GuiSpringBootTest;
import org.wpcleaner.application.gui.javafx.JavaFxTest;
import org.wpcleaner.application.gui.javafx.JavaFxWindowsRegistry;
import org.wpcleaner.application.gui.javafx.core.action.content.PageContentDisplay;
import org.wpcleaner.application.gui.javafx.core.action.content.PageContentExpectations;
import org.wpcleaner.application.gui.javafx.core.pageanalysis.PageAnalysisScrollPane;
import org.wpcleaner.application.gui.javafx.core.window.EmptyWindow;
import org.wpcleaner.application.gui.javafx.recentchanges.FilteredRecentChange;
import org.wpcleaner.application.gui.javafx.recentchanges.JavaFxRecentChangesWindowServices;
import org.wpcleaner.application.gui.javafx.recentchanges.RecentChangesAction;
import org.wpcleaner.application.gui.javafx.recentchanges.options.RecentChangesFilter;

@GuiSpringBootTest
class UserPageDraftActionTest extends JavaFxTest {

  private static final WikiDefinition WIKI = WikipediaDefinitions.EN;
  private static final String CSRF_TOKEN = "csrf-token+\\";
  private static final String CONFIG =
      """
      {
        "moveToDraft": {
          "subPageName": "Draft",
          "summary": "Moving to draft"
        },
        "userPage": {
          "summary": "Summary",
          "text": "User page text"
        },
        "userTalkPage": {
          "summary": "Talk summary",
          "texts": [
            {
              "addedByDefault": true,
              "label": "Notice",
              "text": "Notice for __USER__ on __DRAFT__"
            }
          ]
        }
      }
      """;

  @Autowired private JavaFxRecentChangesWindowServices services;
  @Autowired private CurrentUserService currentUserService;
  @Autowired private GuiLogin guiLogin;
  @Autowired private JavaFxWindowsRegistry windowsRegistry;
  @Autowired private MockApiEdit mockApiEdit;
  @Autowired private MockApiMove mockApiMove;
  @Autowired private MockApiRevisions mockApiRevisions;
  @Autowired private MockApiTokens mockApiTokens;

  @Value("${version.prefix}")
  private String versionPrefix;

  @BeforeEach
  void setUp() throws IOException {
    guiLogin.login(
        WIKI,
        "Bot",
        List.of(
            new Namespace(
                "User", "first-letter", false, CommonNamespaces.USER.id, "User", "User", true),
            new Namespace(
                "User talk",
                "first-letter",
                false,
                CommonNamespaces.USER_TALK.id,
                "User talk",
                "User talk",
                true)));
    Files.deleteIfExists(getConfigFile());
  }

  @AfterEach
  void tearDown() throws InterruptedException, ExecutionException, TimeoutException, IOException {
    runOnJavaFx(windowsRegistry::closeAllWindows);
    Files.deleteIfExists(getConfigFile());
    currentUserService.logout();
  }

  @DisplayName("canApply returns true only for user pages that are not subpages")
  @Test
  void canApply() throws InterruptedException, ExecutionException, TimeoutException {
    final RecentChangesAction action = createAction();

    Assertions.assertThat(
            action.canApply(createRecentChange(CommonNamespaces.USER.id, "User:NicoV")))
        .isTrue();
    Assertions.assertThat(
            action.canApply(createRecentChange(CommonNamespaces.USER.id, "User:NicoV/sandbox")))
        .isFalse();
    Assertions.assertThat(
            action.canApply(createRecentChange(CommonNamespaces.MAIN.id, "Main Page")))
        .isFalse();
    Assertions.assertThat(action.canApply(createRecentChange(null, "User:NicoV"))).isFalse();
  }

  @DisplayName("configure displays configuration window")
  @Test
  void configureDisplaysConfigurationWindow()
      throws InterruptedException, ExecutionException, TimeoutException {
    final UserPageDraftAction action = createAction();

    runOnJavaFx(action::configure);

    Assertions.assertThat(waitForWindow(windowsRegistry, UserPageDraftConfigWindow.class))
        .isNotNull();
  }

  @DisplayName("apply retrieves user page, talk page, and draft sub-page in a single API call")
  @Test
  void applyRetrievesPagesInSingleApiCall()
      throws InterruptedException, ExecutionException, TimeoutException, IOException {
    writeConfig();
    givenPagesContent();
    final UserPageDraftAction action = createAction();

    runOnJavaFx(() -> action.apply(createRecentChange(CommonNamespaces.USER.id, "User:NicoV")));

    final UserPageDraftActionWindow window =
        waitForWindow(windowsRegistry, UserPageDraftActionWindow.class);
    runOnJavaFx(
        () -> {
          assertDisplay(
              window, UserPageDraftActionWindow.USER_PAGE_DISPLAY_ID, "User:NicoV", "User content");
          assertDisplay(
              window,
              UserPageDraftActionWindow.USER_TALK_PAGE_DISPLAY_ID,
              "User talk:NicoV",
              "Talk content");
          assertDisplay(
              window,
              UserPageDraftActionWindow.DRAFT_DISPLAY_ID,
              "User:NicoV/Draft",
              "Draft content");
        });
  }

  @DisplayName("executeAction moves user page, updates user page, and updates talk page")
  @Test
  void executeActionMovesUserPageEditsUserPageAndAppendsTalkPage()
      throws InterruptedException, ExecutionException, TimeoutException, IOException {
    writeConfig();
    givenPagesContent();
    mockApiTokens.givenCsrfToken(WIKI, CSRF_TOKEN);
    mockApiMove
        .givenMove(
            WIKI,
            new MoveQueryByTitle(
                "User:NicoV",
                MoveQueryCommon.emptyBuilder()
                    .to("User:NicoV/Draft")
                    .reason(withPrefix("Moving to draft"))
                    .noRedirect(true)
                    .ignoreWarnings(true)
                    .token(CSRF_TOKEN)
                    .build()))
        .willReturn(new Move("User:NicoV", "Moving to draft", false, "User:NicoV/Draft"));
    mockApiEdit
        .givenEdit(
            WIKI,
            new EditQueryByTitle(
                "User:NicoV",
                EditQueryCommon.emptyBuilder()
                    .text("User page text")
                    .summary(withPrefix("Summary"))
                    .token(CSRF_TOKEN)
                    .build()))
        .willReturn(new Edit(Edit.RESULT_SUCCESS, 1, "User:NicoV", null));
    mockApiEdit
        .givenEdit(
            WIKI,
            new EditQueryByTitle(
                "User talk:NicoV",
                EditQueryCommon.emptyBuilder()
                    .appendText("\n\nNotice for NicoV on Draft")
                    .summary(withPrefix("Talk summary"))
                    .token(CSRF_TOKEN)
                    .build()))
        .willReturn(new Edit(Edit.RESULT_SUCCESS, 2, "User talk:NicoV", null));
    final UserPageDraftAction action = createAction();

    runOnJavaFx(() -> action.apply(createRecentChange(CommonNamespaces.USER.id, "User:NicoV")));
    final UserPageDraftActionWindow window =
        waitForWindow(windowsRegistry, UserPageDraftActionWindow.class);
    runOnJavaFx(() -> lookup(window, UserPageDraftActionWindow.OK_ID, Button.class).fire());

    Assertions.assertThat(closeAlert(window))
        .isEqualTo(
            "The user page has been moved to draft and replaced, and the talk page updated.");
  }

  private UserPageDraftAction createAction()
      throws InterruptedException, ExecutionException, TimeoutException {
    return callOnJavaFx(() -> new UserPageDraftAction(services, new EmptyWindow<>(services)));
  }

  private void givenPagesContent() {
    PageContentExpectations.givenPagesContent(
        mockApiRevisions,
        WIKI,
        Map.of(
            "User:NicoV", "User content",
            "User talk:NicoV", "Talk content",
            "User:NicoV/Draft", "Draft content"));
  }

  private static void writeConfig() throws IOException {
    final Path configFile = getConfigFile();
    Files.createDirectories(configFile.getParent());
    Files.writeString(configFile, CONFIG, StandardCharsets.UTF_8);
  }

  private static Path getConfigFile() {
    return SettingsPersistence.getFolder().resolve(WIKI.code()).resolve("rc-userpage-draft.json");
  }

  private String withPrefix(final String summary) {
    return "WPCleanerNG v%s - %s".formatted(versionPrefix, summary);
  }

  private static void assertDisplay(
      final UserPageDraftActionWindow window,
      final String displayId,
      final String title,
      final String content) {
    final VBox display = lookup(window, displayId, VBox.class);
    Assertions.assertThat(lookup(display, PageContentDisplay.TITLE_ID, Label.class).getText())
        .isEqualTo(title);
    Assertions.assertThat(
            lookup(display, PageContentDisplay.CONTENT_ID, PageAnalysisScrollPane.class)
                .getArea()
                .getText())
        .isEqualTo(content);
  }

  private static FilteredRecentChange createRecentChange(
      @Nullable final Integer ns, final String title) {
    return new FilteredRecentChange(
        "comment",
        0,
        URI.create("https://diff"),
        RecentChangesFilter.ACCEPT_ALL,
        ns,
        URI.create("https://page"),
        1,
        2,
        3,
        List.of(),
        null,
        title,
        "User");
  }
}
