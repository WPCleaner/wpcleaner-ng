package org.wpcleaner.application.gui.javafx.recentchanges.userpagedraft;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Objects;
import java.util.function.Consumer;
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
import org.wpcleaner.api.utils.GT;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.application.gui.javafx.core.action.content.append.ContentAppendAction;
import org.wpcleaner.application.gui.javafx.core.action.content.movetodraft.MoveToDraftAction;
import org.wpcleaner.application.gui.javafx.core.action.content.replace.ContentReplaceAction;
import org.wpcleaner.application.gui.javafx.core.window.JavaFxWindow;
import org.wpcleaner.application.gui.javafx.recentchanges.JavaFxRecentChangesWindowServices;

final class UserPageDraftActionWindow extends JavaFxWindow<JavaFxRecentChangesWindowServices> {

  private final String userName;
  private final MoveToDraftAction moveToDraftAction;
  private final Consumer<UserPageDraftActionParams> onActionConfirmed;
  private final ContentReplaceAction userPageAction;
  private final ContentAppendAction userTalkPageAction;

  @SuppressWarnings("PMD.ExcessiveParameterList")
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
      final String initialDraftContent,
      final Consumer<UserPageDraftActionParams> onActionConfirmed) {
    super(services, Objects.requireNonNull(owner));
    stage.initModality(Modality.WINDOW_MODAL);
    final int colon = userPageTitle.indexOf(':');
    this.userName = colon >= 0 ? userPageTitle.substring(colon + 1) : userPageTitle;
    this.onActionConfirmed = Objects.requireNonNull(onActionConfirmed);
    this.userPageAction =
        new ContentReplaceAction(
            services.colorizer(),
            services.pageAnalysisFactory(),
            services.apiEdit(),
            wiki,
            GT._T("User page"),
            config.userPage(),
            userPageTitle,
            userPageContent);
    this.userTalkPageAction =
        new ContentAppendAction(
            services.colorizer(),
            services.pageAnalysisFactory(),
            services.apiEdit(),
            wiki,
            GT._T("User talk page"),
            config.userTalkPage(),
            userTalkPageTitle,
            userTalkPageContent);
    this.moveToDraftAction =
        new MoveToDraftAction(
            this,
            services.imageLoader(),
            services.colorizer(),
            services.pageAnalysisFactory(),
            services.apiEdit(),
            services.apiRevisions(),
            wiki,
            GT._T("Move to draft"),
            config.moveToDraft(),
            userPageTitle,
            initialSubPageName,
            Objects.requireNonNull(initialDraftContent));
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

    matrix.add(userPageAction.getConfigurationControl(), 0, 0);
    matrix.add(userTalkPageAction.getConfigurationControl(), 1, 0);
    matrix.add(moveToDraftAction.getConfigurationControl(), 2, 0);

    matrix.add(userPageAction.getDisplayPane(), 0, 1);
    matrix.add(userTalkPageAction.getDisplayPane(), 1, 1);
    matrix.add(moveToDraftAction.getDisplayPane(), 2, 1);

    VBox.setVgrow(matrix, Priority.ALWAYS);

    mainContainer.getChildren().add(matrix);

    final Button okButton = new Button(GT._T("OK"));
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
    final UserPageDraftActionParams params =
        new UserPageDraftActionParams(
            this, userName, moveToDraftAction, userPageAction, userTalkPageAction);
    onActionConfirmed.accept(params);
    stage.close();
  }
}
