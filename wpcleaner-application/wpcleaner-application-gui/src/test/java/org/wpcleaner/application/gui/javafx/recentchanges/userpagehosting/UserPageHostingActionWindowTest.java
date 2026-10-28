package org.wpcleaner.application.gui.javafx.recentchanges.userpagehosting;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
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
import org.wpcleaner.api.api.query.meta.tokens.MockApiTokens;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.api.wiki.definition.WikipediaDefinitions;
import org.wpcleaner.application.gui.GuiSpringBootTest;
import org.wpcleaner.application.gui.javafx.JavaFxTest;
import org.wpcleaner.application.gui.javafx.core.action.content.PageContentDisplay;
import org.wpcleaner.application.gui.javafx.core.action.content.append.ContentAppendActionControl;
import org.wpcleaner.application.gui.javafx.core.action.content.append.ContentAppendConfiguration;
import org.wpcleaner.application.gui.javafx.core.action.content.append.ContentAppendConfigurationText;
import org.wpcleaner.application.gui.javafx.core.action.content.replace.ContentReplaceActionControl;
import org.wpcleaner.application.gui.javafx.core.action.content.replace.ContentReplaceConfiguration;
import org.wpcleaner.application.gui.javafx.core.pageanalysis.PageAnalysisScrollPane;
import org.wpcleaner.application.gui.javafx.recentchanges.JavaFxRecentChangesWindowServices;

@GuiSpringBootTest
class UserPageHostingActionWindowTest extends JavaFxTest {

  private static final WikiDefinition WIKI = WikipediaDefinitions.EN;
  private static final String CSRF_TOKEN = "csrf-token+\\";
  private static final UserPageHostingConfig CONFIG =
      new UserPageHostingConfig(
          new ContentReplaceConfiguration("userPageSummary", "userPageText"),
          new ContentAppendConfiguration(
              "userTalkPageSummary",
              List.of(new ContentAppendConfigurationText(true, "label1", "text1"))));

  @Autowired private JavaFxRecentChangesWindowServices services;
  @Autowired private CurrentUserService currentUserService;
  @Autowired private MockApiEdit mockApiEdit;
  @Autowired private MockApiTokens mockApiTokens;

  @Value("${version.prefix}")
  private String versionPrefix;

  @Nullable private Stage currentOwnerStage;
  @Nullable private UserPageHostingActionWindow currentWindow;

  @BeforeEach
  void setUp() {
    currentUserService.logout();
  }

  @AfterEach
  void tearDown() throws InterruptedException, ExecutionException, TimeoutException {
    final UserPageHostingActionWindow windowToClose = currentWindow;
    final Stage ownerStageToClose = currentOwnerStage;
    runOnJavaFx(
        () -> {
          if (windowToClose != null) {
            windowToClose.getStage().close();
          }
          if (ownerStageToClose != null) {
            ownerStageToClose.close();
          }
        });
  }

  @DisplayName("UserPageHostingActionWindow initializes controls properly")
  @Test
  void userPageHostingActionWindowInitialization()
      throws InterruptedException, ExecutionException, TimeoutException {
    final UserPageHostingActionWindow window = createWindow(CONFIG, "User:Test", "User talk:Test");
    final Stage ownerStage = currentOwnerStage;

    runOnJavaFx(
        () -> {
          Assertions.assertThat(window.getStage().getTitle())
              .isEqualTo("Confirm User Page Hosting Action");
          Assertions.assertThat(window.getName()).isEqualTo("userPageHostingAction");
          Assertions.assertThat(window.getStage().getScene()).isNotNull();
          Assertions.assertThat(window.getStage().isShowing()).isTrue();
          Assertions.assertThat(window.getStage().getModality()).isEqualTo(Modality.WINDOW_MODAL);
          Assertions.assertThat(window.getStage().getOwner()).isEqualTo(ownerStage);

          final GridPane matrix =
              lookup(window, UserPageHostingActionWindow.MATRIX_ID, GridPane.class);
          Assertions.assertThat(((VBox) matrix.getParent()).getPrefWidth()).isEqualTo(1900.0);

          Assertions.assertThat(matrix.getChildren()).hasSize(4);
          Assertions.assertThat(matrix.getColumnConstraints()).hasSize(2);
          Assertions.assertThat(matrix.getColumnConstraints().get(0).getPercentWidth())
              .isEqualTo(50.0);
          Assertions.assertThat(matrix.getColumnConstraints().get(1).getPercentWidth())
              .isEqualTo(50.0);

          final ContentReplaceActionControl userPageGroup =
              lookup(
                  window,
                  UserPageHostingActionWindow.USER_PAGE_ACTION_ID,
                  ContentReplaceActionControl.class);
          final ContentAppendActionControl userTalkPageGroup =
              lookup(
                  window,
                  UserPageHostingActionWindow.USER_TALK_PAGE_ACTION_ID,
                  ContentAppendActionControl.class);
          final VBox userPageBox =
              lookup(window, UserPageHostingActionWindow.USER_PAGE_DISPLAY_ID, VBox.class);
          final VBox userTalkPageBox =
              lookup(window, UserPageHostingActionWindow.USER_TALK_PAGE_DISPLAY_ID, VBox.class);

          Assertions.assertThat(GridPane.getColumnIndex(userPageGroup)).isEqualTo(0);
          Assertions.assertThat(GridPane.getRowIndex(userPageGroup)).isEqualTo(0);
          Assertions.assertThat(userPageGroup.getText()).isEqualTo("User page");
          Assertions.assertThat(userPageGroup.getMaxHeight()).isEqualTo(Double.MAX_VALUE);

          Assertions.assertThat(GridPane.getColumnIndex(userTalkPageGroup)).isEqualTo(1);
          Assertions.assertThat(GridPane.getRowIndex(userTalkPageGroup)).isEqualTo(0);
          Assertions.assertThat(userTalkPageGroup.getText()).isEqualTo("User talk page");
          Assertions.assertThat(userTalkPageGroup.getMaxHeight()).isEqualTo(Double.MAX_VALUE);

          Assertions.assertThat(GridPane.getColumnIndex(userPageBox)).isEqualTo(0);
          Assertions.assertThat(GridPane.getRowIndex(userPageBox)).isEqualTo(1);
          final Label userPageLabel = lookup(userPageBox, PageContentDisplay.TITLE_ID, Label.class);
          Assertions.assertThat(userPageLabel.getText()).isEqualTo("User:Test");
          final PageAnalysisScrollPane userPageScrollPane =
              lookup(userPageBox, PageContentDisplay.CONTENT_ID, PageAnalysisScrollPane.class);
          Assertions.assertThat(userPageScrollPane.getArea().getPrefHeight()).isEqualTo(700.0);
          Assertions.assertThat(userPageBox.getMaxHeight()).isEqualTo(Double.MAX_VALUE);

          Assertions.assertThat(GridPane.getColumnIndex(userTalkPageBox)).isEqualTo(1);
          Assertions.assertThat(GridPane.getRowIndex(userTalkPageBox)).isEqualTo(1);
          final Label userTalkPageLabel =
              lookup(userTalkPageBox, PageContentDisplay.TITLE_ID, Label.class);
          Assertions.assertThat(userTalkPageLabel.getText()).isEqualTo("User talk:Test");
          final PageAnalysisScrollPane userTalkPageScrollPane =
              lookup(userTalkPageBox, PageContentDisplay.CONTENT_ID, PageAnalysisScrollPane.class);
          Assertions.assertThat(userTalkPageScrollPane.getArea().getPrefHeight()).isEqualTo(700.0);
          Assertions.assertThat(userTalkPageBox.getMaxHeight()).isEqualTo(Double.MAX_VALUE);
        });
  }

  @DisplayName("Clicking OK replaces the user page and updates the talk page")
  @Test
  void okReplacesUserPageAndUpdatesTalkPage()
      throws InterruptedException, ExecutionException, TimeoutException {
    final UserPageHostingConfig config =
        new UserPageHostingConfig(
            new ContentReplaceConfiguration("userPageSummary", "{{Hosting notice}}"),
            new ContentAppendConfiguration(
                "userTalkPageSummary",
                List.of(
                    new ContentAppendConfigurationText(
                        true, "label1", "Hosting for __USER__ on [[User:__USER_PAGE__]]"),
                    new ContentAppendConfigurationText(false, "label2", "Not added"))));
    final UserPageHostingActionWindow window =
        createWindow(config, "User:Jane Doe", "User talk:Jane Doe");
    mockApiTokens.givenCsrfToken(WIKI, CSRF_TOKEN);
    mockApiEdit
        .givenEdit(
            WIKI,
            new EditQueryByTitle(
                "User:Jane Doe",
                EditQueryCommon.emptyBuilder()
                    .text("{{Hosting notice}}")
                    .summary(withPrefix("userPageSummary"))
                    .token(CSRF_TOKEN)
                    .build()))
        .willReturn(new Edit(Edit.RESULT_SUCCESS, 1, "User:Jane Doe", null));
    mockApiEdit
        .givenEdit(
            WIKI,
            new EditQueryByTitle(
                "User talk:Jane Doe",
                EditQueryCommon.emptyBuilder()
                    .appendText("\n\nHosting for Jane Doe on [[User:Jane_Doe]]")
                    .summary(withPrefix("userTalkPageSummary"))
                    .token(CSRF_TOKEN)
                    .build()))
        .willReturn(new Edit(Edit.RESULT_SUCCESS, 2, "User talk:Jane Doe", null));

    runOnJavaFx(() -> lookup(window, UserPageHostingActionWindow.OK_ID, Button.class).fire());

    Assertions.assertThat(closeAlert(window))
        .isEqualTo("The user page has been replaced and the talk page updated.");
    waitForJavaFx(() -> Optional.of(window.getStage().isShowing()).filter(showing -> !showing));
  }

  private UserPageHostingActionWindow createWindow(
      final UserPageHostingConfig config,
      final String userPageTitle,
      final String userTalkPageTitle)
      throws InterruptedException, ExecutionException, TimeoutException {
    return callOnJavaFx(
        () -> {
          final Stage owner = new Stage();
          currentOwnerStage = owner;
          final UserPageHostingActionWindow created =
              new UserPageHostingActionWindow(
                  services,
                  owner,
                  WIKI,
                  config,
                  userPageTitle,
                  "Initial user page",
                  userTalkPageTitle,
                  "Initial talk page");
          currentWindow = created;
          return created;
        });
  }

  private String withPrefix(final String summary) {
    return "WPCleanerNG v%s - %s".formatted(versionPrefix, summary);
  }
}
