package org.wpcleaner.application.gui.javafx.recentchanges.userpagedraft;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Objects;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.wpcleaner.api.progress.LongRunningTask;
import org.wpcleaner.api.progress.ProgressStep;
import org.wpcleaner.api.progress.ProgressTracker;
import org.wpcleaner.api.utils.GT;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.application.gui.javafx.core.action.content.append.ContentAppendAction;
import org.wpcleaner.application.gui.javafx.core.action.content.append.ContentAppendContext;
import org.wpcleaner.application.gui.javafx.core.action.content.movetodraft.MoveToDraftAction;
import org.wpcleaner.application.gui.javafx.core.action.content.movetodraft.MoveToDraftContext;
import org.wpcleaner.application.gui.javafx.core.action.content.replace.ContentReplaceAction;
import org.wpcleaner.application.gui.javafx.core.action.content.replace.ContentReplaceContext;
import org.wpcleaner.application.gui.javafx.core.window.JavaFxWindow;
import org.wpcleaner.application.gui.javafx.recentchanges.JavaFxRecentChangesWindowServices;

final class UserPageDraftActionWindow extends JavaFxWindow<JavaFxRecentChangesWindowServices> {

  static final String DRAFT_DISPLAY_ID = "draft-display";
  static final String MATRIX_ID = "matrix";
  static final String MOVE_TO_DRAFT_ACTION_ID = "move-to-draft-action";
  static final String OK_ID = "ok";
  static final String USER_PAGE_ACTION_ID = "user-page-action";
  static final String USER_PAGE_DISPLAY_ID = "user-page-display";
  static final String USER_TALK_PAGE_ACTION_ID = "user-talk-page-action";
  static final String USER_TALK_PAGE_DISPLAY_ID = "user-talk-page-display";

  private final String userName;
  private final MoveToDraftAction moveToDraftAction;
  private final ContentReplaceAction userPageAction;
  private final ContentAppendAction userTalkPageAction;

  public UserPageDraftActionWindow(
      final JavaFxRecentChangesWindowServices services,
      final Stage owner,
      final WikiDefinition wiki,
      final UserPageDraftConfig config,
      final String userPageTitle,
      final String userPageContent,
      final String userTalkPageTitle,
      final String userTalkPageContent,
      final String initialSubPageName,
      final String initialDraftContent) {
    super(services, Objects.requireNonNull(owner));
    stage.initModality(Modality.WINDOW_MODAL);
    final int colon = userPageTitle.indexOf(':');
    this.userName = colon >= 0 ? userPageTitle.substring(colon + 1) : userPageTitle;
    this.userPageAction =
        new ContentReplaceAction(
            services.colorizer(),
            services.pageAnalysisFactory(),
            services.apiEdit(),
            config.userPage(),
            new ContentReplaceContext(GT._T("User page"), wiki, userPageTitle, userPageContent));
    this.userTalkPageAction =
        new ContentAppendAction(
            services.colorizer(),
            services.pageAnalysisFactory(),
            services.apiEdit(),
            config.userTalkPage(),
            new ContentAppendContext(
                GT._T("User talk page"), wiki, userTalkPageTitle, userTalkPageContent));
    this.moveToDraftAction =
        new MoveToDraftAction(
            this,
            services.imageLoader(),
            services.colorizer(),
            services.pageAnalysisFactory(),
            services.apiEdit(),
            services.apiRevisions(),
            config.moveToDraft(),
            new MoveToDraftContext(
                GT._T("Move to draft"),
                wiki,
                userPageTitle,
                initialSubPageName,
                Objects.requireNonNull(initialDraftContent)));
    stage.setTitle(GT._T("Confirm User Page Draft Action"));

    initialize();
    stage.show();
  }

  @Override
  public String getHelpPage() {
    return "User page draft";
  }

  @Override
  public String getName() {
    return "userPageDraftAction";
  }

  @Override
  protected Scene createScene() {
    final StackPane root = new StackPane();
    final VBox mainContainer = new VBox(10);
    mainContainer.setPadding(new Insets(10, 15, 10, 15));
    mainContainer.setPrefWidth(1900);

    final GridPane matrix = new GridPane();
    matrix.setId(MATRIX_ID);
    matrix.setHgap(10);
    matrix.setVgap(10);

    final ColumnConstraints col0 = new ColumnConstraints();
    col0.setPercentWidth(100.0 / 3.0);
    col0.setHgrow(Priority.ALWAYS);

    final ColumnConstraints col1 = new ColumnConstraints();
    col1.setPercentWidth(100.0 / 3.0);
    col1.setHgrow(Priority.ALWAYS);

    final ColumnConstraints col2 = new ColumnConstraints();
    col2.setPercentWidth(100.0 / 3.0);
    col2.setHgrow(Priority.ALWAYS);

    matrix.getColumnConstraints().addAll(col0, col1, col2);

    final RowConstraints rowTop = new RowConstraints();
    rowTop.setVgrow(Priority.NEVER);

    final RowConstraints rowBottom = new RowConstraints();
    rowBottom.setVgrow(Priority.ALWAYS);

    matrix.getRowConstraints().addAll(rowTop, rowBottom);

    userPageAction.getConfigurationControl().setId(USER_PAGE_ACTION_ID);
    userTalkPageAction.getConfigurationControl().setId(USER_TALK_PAGE_ACTION_ID);
    moveToDraftAction.getConfigurationControl().setId(MOVE_TO_DRAFT_ACTION_ID);
    userPageAction.getDisplayPane().setId(USER_PAGE_DISPLAY_ID);
    userTalkPageAction.getDisplayPane().setId(USER_TALK_PAGE_DISPLAY_ID);
    moveToDraftAction.getDisplayPane().setId(DRAFT_DISPLAY_ID);

    matrix.add(userPageAction.getConfigurationControl(), 0, 0);
    matrix.add(userTalkPageAction.getConfigurationControl(), 1, 0);
    matrix.add(moveToDraftAction.getConfigurationControl(), 2, 0);

    matrix.add(userPageAction.getDisplayPane(), 0, 1);
    matrix.add(userTalkPageAction.getDisplayPane(), 1, 1);
    matrix.add(moveToDraftAction.getDisplayPane(), 2, 1);

    VBox.setVgrow(matrix, Priority.ALWAYS);

    mainContainer.getChildren().add(matrix);

    final Button okButton = new Button(GT._T("OK"));
    okButton.setId(OK_ID);
    okButton.setDefaultButton(true);
    okButton.setOnAction(_ -> handleOk());

    final Button cancelButton = new Button(GT._T("Cancel"));
    cancelButton.setCancelButton(true);
    cancelButton.setOnAction(_ -> stage.close());

    final HBox buttons = new HBox(10, okButton, cancelButton);
    buttons.setAlignment(Pos.CENTER_RIGHT);
    mainContainer.getChildren().add(buttons);

    mainContainer.disableProperty().bind(loading);

    root.getChildren().addAll(mainContainer, progressOverlay);
    return new Scene(root);
  }

  public String getUserName() {
    return userName;
  }

  private void handleOk() {
    if (!userPageAction.isComplete()
        || !moveToDraftAction.isComplete()
        || !userTalkPageAction.isComplete()) {
      showError(GT._T("Please provide all required configuration elements."));
      return;
    }
    executeAsync(new ExecuteActionTask(), _ -> onFinished(), this::onError);
  }

  private void onFinished() {
    showInformation(
        GT._T("Success"),
        GT._T("Action completed successfully"),
        GT._T(
            "The user page has been moved to draft and replaced, and the talk page" + " updated."));
    stage.close();
  }

  private void onError(final Exception e) {
    showError(
        GT._T(
            "Error updating pages: %s",
            Objects.requireNonNullElseGet(e.getMessage(), () -> e.getClass().getSimpleName())));
  }

  private final class ExecuteActionTask implements LongRunningTask<Void> {

    @Override
    public Void call(final ProgressTracker tracker) {
      try (ProgressStep _ = tracker.start(GT._T("Moving user page to draft"))) {
        moveToDraftAction.execute();
      }
      try (ProgressStep _ = tracker.start(GT._T("Updating user page"))) {
        userPageAction.execute();
      }
      try (ProgressStep _ = tracker.start(GT._T("Updating user talk page"))) {
        userTalkPageAction.setVariable("__USER__", userName);
        userTalkPageAction.setVariable("__USER_PAGE__", userName.replace(' ', '_'));
        userTalkPageAction.setVariable("__DRAFT__", moveToDraftAction.getSubPageName());
        userTalkPageAction.setVariable(
            "__DRAFT_PAGE__", moveToDraftAction.getSubPageName().replace(' ', '_'));
        userTalkPageAction.execute();
      }
      return null;
    }
  }
}
