package org.wpcleaner.application.gui.javafx.recentchanges.userpagehosting;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.ArrayList;
import java.util.List;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.api.analysis.PageAnalysisFactory;
import org.wpcleaner.api.utils.GT;
import org.wpcleaner.application.gui.javafx.core.pageanalysis.PageAnalysisArea;
import org.wpcleaner.application.gui.javafx.core.pageanalysis.PageAnalysisScrollPane;
import org.wpcleaner.application.gui.javafx.core.pageanalysis.coloration.PageSyntaxColorizer;

final class UserPageHostingActionDialog extends Dialog<@Nullable UserPageHostingActionParams> {

  private static final double LABEL_WIDTH = 120.0;

  private final TextArea userPageTextArea = new TextArea();
  private final TextField userPageCommentField = new TextField();
  private final TextField userTalkPageCommentField = new TextField();

  public UserPageHostingActionDialog(
      final UserPageHostingConfig config,
      final String userPageTitle,
      final String userPageContent,
      final String userTalkPageTitle,
      final String userTalkPageContent,
      final PageSyntaxColorizer colorizer,
      final PageAnalysisFactory pageAnalysisFactory) {
    super();
    setTitle(GT._T("Confirm User Page Hosting Action"));

    final DialogPane dialogPane = getDialogPane();
    dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

    final List<CheckBox> checkBoxes = new ArrayList<>();

    final TabPane previewTabPane = new TabPane();

    final Tab userPageTab = new Tab(GT._T("User page"));
    userPageTab.setClosable(false);
    final PageAnalysisScrollPane userPageScrollPane = new PageAnalysisScrollPane(colorizer);
    final PageAnalysisArea userPagePreviewArea = userPageScrollPane.getArea();
    userPagePreviewArea.setPrefHeight(350);
    userPagePreviewArea.updateText(userPageTitle, userPageContent, pageAnalysisFactory);
    userPageTab.setContent(userPageScrollPane);

    final Tab userTalkPageTab = new Tab(GT._T("User talk page"));
    userTalkPageTab.setClosable(false);
    final PageAnalysisScrollPane userTalkPageScrollPane = new PageAnalysisScrollPane(colorizer);
    final PageAnalysisArea userTalkPagePreviewArea = userTalkPageScrollPane.getArea();
    userTalkPagePreviewArea.setPrefHeight(350);
    userTalkPagePreviewArea.updateText(userTalkPageTitle, userTalkPageContent, pageAnalysisFactory);
    userTalkPageTab.setContent(userTalkPageScrollPane);

    previewTabPane.getTabs().addAll(userPageTab, userTalkPageTab);

    final VBox mainBox = new VBox(10);
    mainBox.getChildren().add(createUserPageGroup(config));
    mainBox.getChildren().add(createUserTalkPageGroup(config, checkBoxes));
    mainBox.getChildren().add(previewTabPane);

    dialogPane.setContent(mainBox);
    dialogPane.setPrefWidth(950);

    final Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
    okButton.addEventFilter(
        javafx.event.ActionEvent.ACTION,
        event -> {
          if (userPageCommentField.getText().isBlank()
              || userTalkPageCommentField.getText().isBlank()) {
            event.consume();
          }
        });

    setResultConverter(
        buttonType -> {
          if (buttonType == ButtonType.OK) {
            return new UserPageHostingActionParams(
                userPageCommentField.getText(),
                userTalkPageCommentField.getText(),
                getSelectedTexts(checkBoxes));
          }
          return null;
        });
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
