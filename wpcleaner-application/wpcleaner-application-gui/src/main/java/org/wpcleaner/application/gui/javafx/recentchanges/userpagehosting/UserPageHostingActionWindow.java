package org.wpcleaner.application.gui.javafx.recentchanges.userpagehosting;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.wpcleaner.api.utils.GT;
import org.wpcleaner.application.gui.javafx.core.pageanalysis.PageAnalysisArea;
import org.wpcleaner.application.gui.javafx.core.pageanalysis.PageAnalysisScrollPane;
import org.wpcleaner.application.gui.javafx.core.window.JavaFxWindow;
import org.wpcleaner.application.gui.javafx.recentchanges.JavaFxRecentChangesWindowServices;

@SuppressWarnings("PMD.CouplingBetweenObjects")
final class UserPageHostingActionWindow extends JavaFxWindow<JavaFxRecentChangesWindowServices> {

  private static final double LABEL_WIDTH = 120.0;

  private final UserPageHostingConfig config;
  private final String userPageTitle;
  private final String userPageContent;
  private final String userTalkPageTitle;
  private final String userTalkPageContent;
  private final Consumer<UserPageHostingActionParams> onActionConfirmed;
  private final TextArea userPageTextArea = new TextArea();
  private final TextField userPageCommentField = new TextField();
  private final TextField userTalkPageCommentField = new TextField();
  private final List<CheckBox> checkBoxes = new ArrayList<>();

  public UserPageHostingActionWindow(
      final JavaFxRecentChangesWindowServices services,
      final Stage owner,
      final UserPageHostingConfig config,
      final String userPageTitle,
      final String userPageContent,
      final String userTalkPageTitle,
      final String userTalkPageContent,
      final Consumer<UserPageHostingActionParams> onActionConfirmed) {
    super(services, Objects.requireNonNull(owner));
    stage.initModality(Modality.WINDOW_MODAL);
    this.config = Objects.requireNonNull(config);
    this.userPageTitle = Objects.requireNonNull(userPageTitle);
    this.userPageContent = Objects.requireNonNull(userPageContent);
    this.userTalkPageTitle = Objects.requireNonNull(userTalkPageTitle);
    this.userTalkPageContent = Objects.requireNonNull(userTalkPageContent);
    this.onActionConfirmed = Objects.requireNonNull(onActionConfirmed);
    stage.setTitle(GT._T("Confirm User Page Hosting Action"));

    initialize();
    stage.show();
  }

  @Override
  public String getName() {
    return "userPageHostingAction";
  }

  @Override
  protected Scene createScene() {
    final StackPane root = new StackPane();
    final VBox mainContainer = new VBox(10);
    mainContainer.setPadding(new Insets(10, 15, 10, 15));
    mainContainer.setPrefWidth(1900);

    final Label userPageLabel = new Label(GT._T("User page"));
    final PageAnalysisScrollPane userPageScrollPane =
        new PageAnalysisScrollPane(services.colorizer());
    final PageAnalysisArea userPagePreviewArea = userPageScrollPane.getArea();
    userPagePreviewArea.setPrefHeight(700);
    userPagePreviewArea.updateText(userPageTitle, userPageContent, services.pageAnalysisFactory());
    final VBox userPageBox = new VBox(5, userPageLabel, userPageScrollPane);
    userPageBox.setPrefWidth(0);
    userPageBox.setMaxWidth(Double.MAX_VALUE);
    VBox.setVgrow(userPageScrollPane, Priority.ALWAYS);
    HBox.setHgrow(userPageBox, Priority.ALWAYS);

    final Label userTalkPageLabel = new Label(GT._T("User talk page"));
    final PageAnalysisScrollPane userTalkPageScrollPane =
        new PageAnalysisScrollPane(services.colorizer());
    final PageAnalysisArea userTalkPagePreviewArea = userTalkPageScrollPane.getArea();
    userTalkPagePreviewArea.setPrefHeight(700);
    userTalkPagePreviewArea.updateText(
        userTalkPageTitle, userTalkPageContent, services.pageAnalysisFactory());
    final VBox userTalkPageBox = new VBox(5, userTalkPageLabel, userTalkPageScrollPane);
    userTalkPageBox.setPrefWidth(0);
    userTalkPageBox.setMaxWidth(Double.MAX_VALUE);
    VBox.setVgrow(userTalkPageScrollPane, Priority.ALWAYS);
    HBox.setHgrow(userTalkPageBox, Priority.ALWAYS);

    final HBox previewsBox = new HBox(10, userPageBox, userTalkPageBox);

    mainContainer.getChildren().add(createUserPageGroup(config));
    mainContainer.getChildren().add(createUserTalkPageGroup(config, checkBoxes));
    mainContainer.getChildren().add(previewsBox);

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

    root.getChildren().addAll(mainContainer, progressTracker.getProgressOverlay());
    return new Scene(root);
  }

  private void handleOk() {
    if (userPageCommentField.getText().isBlank() || userTalkPageCommentField.getText().isBlank()) {
      showError(GT._T("Please provide comments for both user page and talk page."));
      return;
    }
    final UserPageHostingActionParams params =
        new UserPageHostingActionParams(
            userPageCommentField.getText(),
            userTalkPageCommentField.getText(),
            List.copyOf(getSelectedTexts(checkBoxes)));
    onActionConfirmed.accept(params);
    stage.close();
  }

  private TitledPane createUserPageGroup(final UserPageHostingConfig config) {
    final GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);

    final Label summaryLabel = new Label(GT._T("Summary:"));
    summaryLabel.setMinWidth(LABEL_WIDTH);
    summaryLabel.setPrefWidth(LABEL_WIDTH);
    userPageCommentField.setText(config.userPageSummary());
    grid.add(summaryLabel, 0, 0);
    grid.add(userPageCommentField, 1, 0);
    GridPane.setHgrow(userPageCommentField, Priority.ALWAYS);

    final Label staticTextLabel = new Label(GT._T("Static text:"));
    staticTextLabel.setMinWidth(LABEL_WIDTH);
    staticTextLabel.setPrefWidth(LABEL_WIDTH);
    userPageTextArea.setText(config.userPageText());
    userPageTextArea.setEditable(false);
    userPageTextArea.setPrefRowCount(3);
    grid.add(staticTextLabel, 0, 1);
    grid.add(userPageTextArea, 1, 1);
    GridPane.setHgrow(userPageTextArea, Priority.ALWAYS);

    final TitledPane titledPane = new TitledPane(GT._T("User page"), grid);
    titledPane.setCollapsible(false);
    return titledPane;
  }

  private TitledPane createUserTalkPageGroup(
      final UserPageHostingConfig config, final List<CheckBox> checkBoxes) {
    final GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);

    final Label summaryLabel = new Label(GT._T("Summary:"));
    summaryLabel.setMinWidth(LABEL_WIDTH);
    summaryLabel.setPrefWidth(LABEL_WIDTH);
    userTalkPageCommentField.setText(config.userTalkPageSummary());
    grid.add(summaryLabel, 0, 0);
    grid.add(userTalkPageCommentField, 1, 0);
    GridPane.setHgrow(userTalkPageCommentField, Priority.ALWAYS);

    final Label staticTextsLabel = new Label(GT._T("Static texts:"));
    staticTextsLabel.setMinWidth(LABEL_WIDTH);
    staticTextsLabel.setPrefWidth(LABEL_WIDTH);

    final VBox checkboxesBox = new VBox(5);
    addCheckBoxes(checkboxesBox, checkBoxes, config);

    grid.add(staticTextsLabel, 0, 1);
    grid.add(checkboxesBox, 1, 1);
    GridPane.setHgrow(checkboxesBox, Priority.ALWAYS);

    final TitledPane titledPane = new TitledPane(GT._T("User talk page"), grid);
    titledPane.setCollapsible(false);
    return titledPane;
  }

  private void addCheckBoxes(
      final VBox container, final List<CheckBox> checkBoxes, final UserPageHostingConfig config) {
    for (final UserTalkPageTextConfig textConfig : config.userTalkPageTexts()) {
      final CheckBox cb = new CheckBox(textConfig.label());
      cb.setSelected(textConfig.addedByDefault());
      cb.setUserData(textConfig.text());
      checkBoxes.add(cb);
      container.getChildren().add(cb);
    }
  }

  private List<String> getSelectedTexts(final List<CheckBox> checkBoxes) {
    final List<String> selectedTexts = new ArrayList<>();
    for (final CheckBox cb : checkBoxes) {
      if (cb.isSelected()) {
        selectedTexts.add((String) cb.getUserData());
      }
    }
    return selectedTexts;
  }
}
