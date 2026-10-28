package org.wpcleaner.application.gui.javafx.core.action.content.append;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import org.assertj.core.api.Assertions;
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
import org.wpcleaner.api.api.query.meta.tokens.MockApiTokens;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.api.wiki.definition.WikipediaDefinitions;
import org.wpcleaner.application.gui.GuiSpringBootTest;
import org.wpcleaner.application.gui.javafx.JavaFxTest;
import org.wpcleaner.application.gui.javafx.core.action.content.PageContentDisplay;
import org.wpcleaner.application.gui.javafx.recentchanges.JavaFxRecentChangesWindowServices;

@GuiSpringBootTest
class ContentAppendActionTest extends JavaFxTest {

  private static final WikiDefinition WIKI = WikipediaDefinitions.EN;
  private static final String CSRF_TOKEN = "csrf-token";

  @Autowired private JavaFxRecentChangesWindowServices services;
  @Autowired private CurrentUserService currentUserService;
  @Autowired private MockApiEdit mockApiEdit;
  @Autowired private MockApiTokens mockApiTokens;

  @Value("${version.prefix}")
  private String versionPrefix;

  @BeforeEach
  void setUp() {
    currentUserService.logout();
  }

  @DisplayName("ContentAppendAction initializes controls and properties properly")
  @Test
  void initializationAndProperties()
      throws InterruptedException, ExecutionException, TimeoutException {
    final ContentAppendAction action =
        createAction(
            new ContentAppendConfiguration(
                "Summary", List.of(new ContentAppendConfigurationText(true, "Label", "Text"))),
            "User talk:Test",
            "Existing content");

    runOnJavaFx(
        () -> {
          Assertions.assertThat(action.getConfigurationControl())
              .isInstanceOf(ContentAppendActionControl.class);
          Assertions.assertThat(action.getDisplayPane()).isInstanceOf(PageContentDisplay.class);
          Assertions.assertThat(action.isComplete()).isTrue();
        });
  }

  @DisplayName("ContentAppendAction execute replaces variable placeholders in selected texts")
  @Test
  void executeReplacesVariables()
      throws InterruptedException, ExecutionException, TimeoutException {
    final ContentAppendAction action =
        createAction(
            new ContentAppendConfiguration(
                "Append summary",
                List.of(
                    new ContentAppendConfigurationText(
                        true,
                        "Notice",
                        "Hello __USER__, your draft is at __DRAFT__. Please check __DRAFT__!"))),
            "User talk:Alice",
            "");
    givenAppend(
        "User talk:Alice",
        "\n\nHello Alice, your draft is at User:Alice/Draft. Please check User:Alice/Draft!",
        "Append summary");

    action.setVariable("__USER__", "Alice");
    action.setVariable("__DRAFT__", "User:Alice/Draft");
    action.execute();
  }

  @DisplayName(
      "ContentAppendAction execute joins multiple selected texts with variable replacement")
  @Test
  void executeWithMultipleSelectedTexts()
      throws InterruptedException, ExecutionException, TimeoutException {
    final ContentAppendAction action =
        createAction(
            new ContentAppendConfiguration(
                "Multi summary",
                List.of(
                    new ContentAppendConfigurationText(true, "First", "Header for __USER__"),
                    new ContentAppendConfigurationText(true, "Second", "Draft link: __DRAFT__"))),
            "User talk:Bob",
            "");
    givenAppend("User talk:Bob", "\n\nHeader for Bob\n\nDraft link: Draft/1", "Multi summary");

    action.setVariable("__USER__", "Bob");
    action.setVariable("__DRAFT__", "Draft/1");
    action.execute();
  }

  @DisplayName("ContentAppendAction execute does nothing when selected texts is empty")
  @Test
  void executeWithEmptySelectedTexts()
      throws InterruptedException, ExecutionException, TimeoutException {
    final ContentAppendAction action =
        createAction(
            new ContentAppendConfiguration(
                "Summary", List.of(new ContentAppendConfigurationText(false, "Unchecked", "Text"))),
            "User talk:Test",
            "");

    action.execute();
  }

  private ContentAppendAction createAction(
      final ContentAppendConfiguration config, final String pageTitle, final String pageContent)
      throws InterruptedException, ExecutionException, TimeoutException {
    return callOnJavaFx(
        () ->
            new ContentAppendAction(
                services.colorizer(),
                services.pageAnalysisFactory(),
                services.apiEdit(),
                config,
                new ContentAppendContext("Append Action", WIKI, pageTitle, pageContent)));
  }

  private void givenAppend(final String title, final String appendText, final String summary) {
    mockApiTokens.givenCsrfToken(WIKI, CSRF_TOKEN);
    mockApiEdit
        .givenEdit(
            WIKI,
            new EditQueryByTitle(
                title,
                EditQueryCommon.emptyBuilder()
                    .appendText(appendText)
                    .summary("WPCleanerNG v%s - %s".formatted(versionPrefix, summary))
                    .token(CSRF_TOKEN)
                    .build()))
        .willReturn(new Edit(Edit.RESULT_SUCCESS, 1, title, null));
  }
}
