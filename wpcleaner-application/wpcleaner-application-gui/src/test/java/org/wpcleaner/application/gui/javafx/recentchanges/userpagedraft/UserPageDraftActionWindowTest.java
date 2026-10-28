package org.wpcleaner.application.gui.javafx.recentchanges.userpagedraft;

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
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
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
import org.wpcleaner.api.api.move.MockApiMove;
import org.wpcleaner.api.api.move.Move;
import org.wpcleaner.api.api.move.MoveQueryByTitle;
import org.wpcleaner.api.api.move.MoveQueryCommon;
import org.wpcleaner.api.api.query.meta.tokens.MockApiTokens;
import org.wpcleaner.api.api.query.prop.revisions.MockApiRevisions;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.api.wiki.definition.WikipediaDefinitions;
import org.wpcleaner.application.gui.GuiSpringBootTest;
import org.wpcleaner.application.gui.javafx.JavaFxTest;
import org.wpcleaner.application.gui.javafx.core.action.content.PageContentDisplay;
import org.wpcleaner.application.gui.javafx.core.action.content.PageContentExpectations;
import org.wpcleaner.application.gui.javafx.core.action.content.append.ContentAppendActionControl;
import org.wpcleaner.application.gui.javafx.core.action.content.append.ContentAppendConfiguration;
import org.wpcleaner.application.gui.javafx.core.action.content.append.ContentAppendConfigurationText;
import org.wpcleaner.application.gui.javafx.core.action.content.movetodraft.MoveToDraftActionControl;
import org.wpcleaner.application.gui.javafx.core.action.content.movetodraft.MoveToDraftConfiguration;
import org.wpcleaner.application.gui.javafx.core.action.content.replace.ContentReplaceActionControl;
import org.wpcleaner.application.gui.javafx.core.action.content.replace.ContentReplaceConfiguration;
import org.wpcleaner.application.gui.javafx.core.pageanalysis.PageAnalysisScrollPane;
import org.wpcleaner.application.gui.javafx.recentchanges.JavaFxRecentChangesWindowServices;

@GuiSpringBootTest
class UserPageDraftActionWindowTest extends JavaFxTest {

  private static final WikiDefinition WIKI = WikipediaDefinitions.EN;
  private static final String CSRF_TOKEN = "csrf-token+\\";
  private static final UserPageDraftConfig CONFIG =
      new UserPageDraftConfig(
          new MoveToDraftConfiguration("Draft", "moveSummary"),
          new ContentReplaceConfiguration("userPageSummary", "userPageText"),
          new ContentAppendConfiguration(
              "userTalkPageSummary",
              List.of(new ContentAppendConfigurationText(true, "label1", "text1"))));

  @Autowired private JavaFxRecentChangesWindowServices services;
  @Autowired private CurrentUserService currentUserService;
  @Autowired private MockApiEdit mockApiEdit;
  @Autowired private MockApiMove mockApiMove;
  @Autowired private MockApiRevisions mockApiRevisions;
  @Autowired private MockApiTokens mockApiTokens;

  @Value("${version.prefix}")
  private String versionPrefix;

  @Nullable private Stage currentOwnerStage;
  @Nullable private UserPageDraftActionWindow currentWindow;

  @BeforeEach
  void setUp() {
    currentUserService.logout();
  }

  @AfterEach
  void tearDown() throws InterruptedException, ExecutionException, TimeoutException {
    final UserPageDraftActionWindow windowToClose = currentWindow;
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

  @DisplayName("UserPageDraftActionWindow initializes controls properly")
  @Test
  void userPageDraftActionWindowInitialization()
      throws InterruptedException, ExecutionException, TimeoutException {
    final UserPageDraftActionWindow window = createWindow(CONFIG, "User:Test", "User talk:Test");
    final Stage ownerStage = currentOwnerStage;

    runOnJavaFx(
        () -> {
          Assertions.assertThat(window.getStage().getTitle())
              .isEqualTo("Confirm User Page Draft Action");
          Assertions.assertThat(window.getUserName()).isEqualTo("Test");
          Assertions.assertThat(window.getName()).isEqualTo("userPageDraftAction");
          Assertions.assertThat(window.getStage().getScene()).isNotNull();
          Assertions.assertThat(window.getStage().isShowing()).isTrue();
          Assertions.assertThat(window.getStage().getModality()).isEqualTo(Modality.WINDOW_MODAL);
          Assertions.assertThat(window.getStage().getOwner()).isEqualTo(ownerStage);

          final GridPane matrix =
              lookup(window, UserPageDraftActionWindow.MATRIX_ID, GridPane.class);
          Assertions.assertThat(((VBox) matrix.getParent()).getPrefWidth()).isEqualTo(1900.0);

          Assertions.assertThat(matrix.getChildren()).hasSize(6);
          Assertions.assertThat(matrix.getColumnConstraints()).hasSize(3);
          Assertions.assertThat(matrix.getColumnConstraints().get(0).getPercentWidth())
              .isEqualTo(100.0 / 3.0);
          Assertions.assertThat(matrix.getColumnConstraints().get(1).getPercentWidth())
              .isEqualTo(100.0 / 3.0);
          Assertions.assertThat(matrix.getColumnConstraints().get(2).getPercentWidth())
              .isEqualTo(100.0 / 3.0);

          final ContentReplaceActionControl userPageGroup =
              lookup(
                  window,
                  UserPageDraftActionWindow.USER_PAGE_ACTION_ID,
                  ContentReplaceActionControl.class);
          final ContentAppendActionControl userTalkPageGroup =
              lookup(
                  window,
                  UserPageDraftActionWindow.USER_TALK_PAGE_ACTION_ID,
                  ContentAppendActionControl.class);
          final MoveToDraftActionControl moveDraftGroup =
              lookup(
                  window,
                  UserPageDraftActionWindow.MOVE_TO_DRAFT_ACTION_ID,
                  MoveToDraftActionControl.class);

          final VBox userPageBox =
              lookup(window, UserPageDraftActionWindow.USER_PAGE_DISPLAY_ID, VBox.class);
          final VBox userTalkPageBox =
              lookup(window, UserPageDraftActionWindow.USER_TALK_PAGE_DISPLAY_ID, VBox.class);
          final VBox draftBox =
              lookup(window, UserPageDraftActionWindow.DRAFT_DISPLAY_ID, VBox.class);

          Assertions.assertThat(GridPane.getColumnIndex(userPageGroup)).isEqualTo(0);
          Assertions.assertThat(GridPane.getRowIndex(userPageGroup)).isEqualTo(0);
          Assertions.assertThat(userPageGroup.getText()).isEqualTo("User page");
          Assertions.assertThat(userPageGroup.getMaxHeight()).isEqualTo(Double.MAX_VALUE);

          Assertions.assertThat(GridPane.getColumnIndex(userTalkPageGroup)).isEqualTo(1);
          Assertions.assertThat(GridPane.getRowIndex(userTalkPageGroup)).isEqualTo(0);
          Assertions.assertThat(userTalkPageGroup.getText()).isEqualTo("User talk page");
          Assertions.assertThat(userTalkPageGroup.getMaxHeight()).isEqualTo(Double.MAX_VALUE);

          Assertions.assertThat(GridPane.getColumnIndex(moveDraftGroup)).isEqualTo(2);
          Assertions.assertThat(GridPane.getRowIndex(moveDraftGroup)).isEqualTo(0);
          Assertions.assertThat(moveDraftGroup.getText()).isEqualTo("Move to draft");
          Assertions.assertThat(moveDraftGroup.getMaxHeight()).isEqualTo(Double.MAX_VALUE);
          final TextField subPageNameField =
              lookup(moveDraftGroup, MoveToDraftActionControl.SUB_PAGE_NAME_ID, TextField.class);
          Assertions.assertThat(subPageNameField.getText()).isEqualTo("Draft");
          final Button refreshButton =
              lookup(moveDraftGroup, MoveToDraftActionControl.REFRESH_ID, Button.class);
          Assertions.assertThat(refreshButton.getTooltip().getText())
              .isEqualTo("Refresh draft page content");

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

          Assertions.assertThat(GridPane.getColumnIndex(draftBox)).isEqualTo(2);
          Assertions.assertThat(GridPane.getRowIndex(draftBox)).isEqualTo(1);
          final Label draftLabel = lookup(draftBox, PageContentDisplay.TITLE_ID, Label.class);
          Assertions.assertThat(draftLabel.getText()).isEqualTo("User:Test/Draft");

          final PageAnalysisScrollPane draftScrollPane =
              lookup(draftBox, PageContentDisplay.CONTENT_ID, PageAnalysisScrollPane.class);
          Assertions.assertThat(draftScrollPane.getArea().getPrefHeight()).isEqualTo(700.0);
          Assertions.assertThat(draftBox.getMaxHeight()).isEqualTo(Double.MAX_VALUE);

          Assertions.assertThat(userPageBox.getPrefWidth())
              .isEqualTo(userTalkPageBox.getPrefWidth());
          Assertions.assertThat(userPageBox.getPrefWidth()).isEqualTo(draftBox.getPrefWidth());
          Assertions.assertThat(HBox.getHgrow(userPageBox)).isEqualTo(Priority.ALWAYS);
          Assertions.assertThat(HBox.getHgrow(userTalkPageBox)).isEqualTo(Priority.ALWAYS);
          Assertions.assertThat(HBox.getHgrow(draftBox)).isEqualTo(Priority.ALWAYS);
        });
  }

  @DisplayName("Clicking refresh button updates draft preview area with new sub-page content")
  @Test
  void refreshDraftContentUpdatesPreview()
      throws InterruptedException, ExecutionException, TimeoutException {
    final UserPageDraftActionWindow window = createWindow(CONFIG, "User:Test", "User talk:Test");
    PageContentExpectations.givenPageContent(
        mockApiRevisions, WIKI, "User:Test/CustomDraft", "Updated draft content");

    runOnJavaFx(
        () -> {
          final MoveToDraftActionControl moveDraftGroup = getMoveToDraftControl(window);
          lookup(moveDraftGroup, MoveToDraftActionControl.SUB_PAGE_NAME_ID, TextField.class)
              .setText("CustomDraft");
          lookup(moveDraftGroup, MoveToDraftActionControl.REFRESH_ID, Button.class).fire();
        });

    final VBox draftBox = lookup(window, UserPageDraftActionWindow.DRAFT_DISPLAY_ID, VBox.class);
    waitForJavaFx(
        () ->
            Optional.of(lookup(draftBox, PageContentDisplay.TITLE_ID, Label.class).getText())
                .filter("User:Test/CustomDraft"::equals));
    Assertions.assertThat(
            callOnJavaFx(
                () ->
                    lookup(draftBox, PageContentDisplay.CONTENT_ID, PageAnalysisScrollPane.class)
                        .getArea()
                        .getText()))
        .isEqualTo("Updated draft content");
  }

  @DisplayName("Clicking OK moves the user page to draft, replaces it and updates the talk page")
  @Test
  void okMovesToDraftAndUpdatesPages()
      throws InterruptedException, ExecutionException, TimeoutException {
    final UserPageDraftConfig config =
        new UserPageDraftConfig(
            new MoveToDraftConfiguration("My Draft", "moveSummary"),
            new ContentReplaceConfiguration("userPageSummary", "userPageText"),
            new ContentAppendConfiguration(
                "userTalkPageSummary",
                List.of(
                    new ContentAppendConfigurationText(
                        true,
                        "label1",
                        "Draft of __USER__ in [[User:__USER_PAGE__/__DRAFT_PAGE__|__DRAFT__]]"))));
    final UserPageDraftActionWindow window =
        createWindow(config, "User:Jane Doe", "User talk:Jane Doe");
    mockApiTokens.givenCsrfToken(WIKI, CSRF_TOKEN);
    mockApiMove
        .givenMove(
            WIKI,
            new MoveQueryByTitle(
                "User:Jane Doe",
                MoveQueryCommon.emptyBuilder()
                    .to("User:Jane Doe/My Draft")
                    .reason(withPrefix("moveSummary"))
                    .noRedirect(true)
                    .ignoreWarnings(true)
                    .token(CSRF_TOKEN)
                    .build()))
        .willReturn(new Move("User:Jane Doe", "moveSummary", false, "User:Jane Doe/My Draft"));
    mockApiEdit
        .givenEdit(
            WIKI,
            new EditQueryByTitle(
                "User:Jane Doe",
                EditQueryCommon.emptyBuilder()
                    .text("userPageText")
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
                    .appendText("\n\nDraft of Jane Doe in [[User:Jane_Doe/My_Draft|My Draft]]")
                    .summary(withPrefix("userTalkPageSummary"))
                    .token(CSRF_TOKEN)
                    .build()))
        .willReturn(new Edit(Edit.RESULT_SUCCESS, 2, "User talk:Jane Doe", null));

    runOnJavaFx(() -> lookup(window, UserPageDraftActionWindow.OK_ID, Button.class).fire());

    Assertions.assertThat(closeAlert(window))
        .isEqualTo(
            "The user page has been moved to draft and replaced, and the talk page updated.");
    waitForJavaFx(() -> Optional.of(window.getStage().isShowing()).filter(showing -> !showing));
  }

  private UserPageDraftActionWindow createWindow(
      final UserPageDraftConfig config, final String userPageTitle, final String userTalkPageTitle)
      throws InterruptedException, ExecutionException, TimeoutException {
    return callOnJavaFx(
        () -> {
          final Stage owner = new Stage();
          currentOwnerStage = owner;
          final UserPageDraftActionWindow created =
              new UserPageDraftActionWindow(
                  services,
                  owner,
                  WIKI,
                  config,
                  userPageTitle,
                  "Initial user page",
                  userTalkPageTitle,
                  "Initial talk page",
                  "Draft",
                  "Initial draft");
          currentWindow = created;
          return created;
        });
  }

  private String withPrefix(final String summary) {
    return "WPCleanerNG v%s - %s".formatted(versionPrefix, summary);
  }

  private static MoveToDraftActionControl getMoveToDraftControl(
      final UserPageDraftActionWindow window) {
    return lookup(
        window, UserPageDraftActionWindow.MOVE_TO_DRAFT_ACTION_ID, MoveToDraftActionControl.class);
  }
}
