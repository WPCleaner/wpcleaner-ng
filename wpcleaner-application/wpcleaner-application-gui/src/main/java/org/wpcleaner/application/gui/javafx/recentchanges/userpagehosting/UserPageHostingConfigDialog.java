package org.wpcleaner.application.gui.javafx.recentchanges.userpagehosting;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.ArrayList;
import java.util.List;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.api.utils.GT;
import org.wpcleaner.application.gui.javafx.JavaFxImageLoader;
import org.wpcleaner.application.gui.javafx.core.control.DefaultStyles;
import org.wpcleaner.lib.image.ImageCollection;
import org.wpcleaner.lib.image.ImageSize;

final class UserPageHostingConfigDialog extends Dialog<@Nullable UserPageHostingConfig> {

  private static final double LABEL_WIDTH = 120.0;

  private final JavaFxImageLoader imageLoader;
  private final TextArea userPageTextArea = new TextArea();
  private final TextField userPageSummaryField = new TextField();
  private final TextField userTalkPageSummaryField = new TextField();
  private final TableView<@Nullable TalkPageTextModel> tableView = new TableView<>();
  private final ObservableList<TalkPageTextModel> models = FXCollections.observableArrayList();

  public UserPageHostingConfigDialog(
      final JavaFxImageLoader imageLoader, @Nullable final UserPageHostingConfig initialConfig) {
    super();
    this.imageLoader = imageLoader;
    setTitle(GT._T("Configure User Page Hosting Prevention"));

    final DialogPane dialogPane = getDialogPane();
    dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

    final VBox mainBox = new VBox(10);
    mainBox.getChildren().add(createUserPageGroup(initialConfig));
    mainBox.getChildren().add(createUserTalkPageGroup(initialConfig));

    dialogPane.setContent(mainBox);
    dialogPane.setPrefWidth(750);

    final Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
    okButton.addEventFilter(
        javafx.event.ActionEvent.ACTION,
        event -> {
          if (userPageTextArea.getText().isBlank()
              || userPageSummaryField.getText().isBlank()
              || userTalkPageSummaryField.getText().isBlank()) {
            event.consume();
          }
        });

    setResultConverter(
        buttonType -> {
          if (buttonType == ButtonType.OK) {
            return convertResult();
          }
          return null;
        });
  }

  private TitledPane createUserPageGroup(@Nullable final UserPageHostingConfig config) {
    final GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);

    final Label summaryLabel = new Label(GT._T("Summary:"));
    summaryLabel.setMinWidth(LABEL_WIDTH);
    summaryLabel.setPrefWidth(LABEL_WIDTH);
    grid.add(summaryLabel, 0, 0);
    grid.add(userPageSummaryField, 1, 0);
    GridPane.setHgrow(userPageSummaryField, Priority.ALWAYS);

    final Label staticTextLabel = new Label(GT._T("Static text:"));
    staticTextLabel.setMinWidth(LABEL_WIDTH);
    staticTextLabel.setPrefWidth(LABEL_WIDTH);
    userPageTextArea.setPrefRowCount(4);
    grid.add(staticTextLabel, 0, 1);
    grid.add(userPageTextArea, 1, 1);
    GridPane.setHgrow(userPageTextArea, Priority.ALWAYS);

    if (config != null) {
      userPageSummaryField.setText(config.userPageSummary());
      userPageTextArea.setText(config.userPageText());
    }

    final TitledPane titledPane = new TitledPane(GT._T("User page"), grid);
    titledPane.setCollapsible(false);
    return titledPane;
  }

  private TitledPane createUserTalkPageGroup(@Nullable final UserPageHostingConfig config) {
    final GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);

    final Label summaryLabel = new Label(GT._T("Summary:"));
    summaryLabel.setMinWidth(LABEL_WIDTH);
    summaryLabel.setPrefWidth(LABEL_WIDTH);
    grid.add(summaryLabel, 0, 0);
    grid.add(userTalkPageSummaryField, 1, 0);
    GridPane.setHgrow(userTalkPageSummaryField, Priority.ALWAYS);

    final Label staticTextsLabel = new Label(GT._T("Static texts:"));
    staticTextsLabel.setMinWidth(LABEL_WIDTH);
    staticTextsLabel.setPrefWidth(LABEL_WIDTH);
    tableView.setEditable(true);
    tableView.setPrefHeight(150);
    tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

    final TableColumn<TalkPageTextModel, String> labelCol = new TableColumn<>(GT._T("Label"));
    labelCol.setCellValueFactory(data -> data.getValue().labelProperty());
    labelCol.setCellFactory(TextFieldTableCell.forTableColumn());
    labelCol.setPrefWidth(120);

    final TableColumn<TalkPageTextModel, String> textCol = new TableColumn<>(GT._T("Static text"));
    textCol.setCellValueFactory(data -> data.getValue().textProperty());
    textCol.setCellFactory(_ -> new TextAreaTableCell<>());

    final TableColumn<TalkPageTextModel, Boolean> defaultCol =
        new TableColumn<>(GT._T("By default"));
    defaultCol.setCellValueFactory(data -> data.getValue().addedByDefaultProperty());
    defaultCol.setCellFactory(CheckBoxTableCell.forTableColumn(defaultCol));
    defaultCol.setMinWidth(100);
    defaultCol.setMaxWidth(100);

    tableView.getColumns().add(defaultCol);
    tableView.getColumns().add(labelCol);
    tableView.getColumns().add(textCol);

    if (config != null) {
      userTalkPageSummaryField.setText(config.userTalkPageSummary());
      for (final UserTalkPageTextConfig text : config.userTalkPageTexts()) {
        models.add(new TalkPageTextModel(text.label(), text.text(), text.addedByDefault()));
      }
    }
    tableView.setItems(models);

    final HBox buttonsBox =
        new HBox(
            5,
            createAddButton(),
            createRemoveButton(),
            new Separator(Orientation.VERTICAL),
            createMoveFirstButton(),
            createMoveUpButton(),
            createMoveDownButton(),
            createMoveLastButton());
    final VBox tableBox = new VBox(5, tableView, buttonsBox);
    grid.add(staticTextsLabel, 0, 1);
    grid.add(tableBox, 1, 1);
    GridPane.setHgrow(tableBox, Priority.ALWAYS);

    final TitledPane titledPane = new TitledPane(GT._T("User talk page"), grid);
    titledPane.setCollapsible(false);
    return titledPane;
  }

  private Button createAddButton() {
    final Button addButton = new Button(GT._T("Add"));
    addButton.setOnAction(_ -> models.add(new TalkPageTextModel("", "", false)));
    return addButton;
  }

  private Button createRemoveButton() {
    final Button removeButton = new Button(GT._T("Remove"));
    removeButton.setOnAction(
        _ -> {
          final TalkPageTextModel selected = tableView.getSelectionModel().getSelectedItem();
          if (selected != null) {
            models.remove(selected);
          }
        });
    return removeButton;
  }

  private Button createMoveFirstButton() {
    final Button button = new Button();
    button.setStyle(DefaultStyles.TOOLBAR_ELEMENT);
    imageLoader
        .getImageView(ImageCollection.MOVE_FIRST, ImageSize.BUTTON)
        .ifPresent(button::setGraphic);
    button.setTooltip(new Tooltip(GT._T("Move first")));
    button
        .disableProperty()
        .bind(tableView.getSelectionModel().selectedIndexProperty().lessThanOrEqualTo(0));
    button.setOnAction(
        _ -> {
          final TalkPageTextModel selected = tableView.getSelectionModel().getSelectedItem();
          if (selected != null) {
            final int index = tableView.getSelectionModel().getSelectedIndex();
            if (index > 0) {
              models.remove(index);
              models.addFirst(selected);
              tableView.getSelectionModel().select(0);
            }
          }
        });
    return button;
  }

  private Button createMoveUpButton() {
    final Button button = new Button();
    button.setStyle(DefaultStyles.TOOLBAR_ELEMENT);
    imageLoader
        .getImageView(ImageCollection.MOVE_UP, ImageSize.BUTTON)
        .ifPresent(button::setGraphic);
    button.setTooltip(new Tooltip(GT._T("Move up")));
    button
        .disableProperty()
        .bind(tableView.getSelectionModel().selectedIndexProperty().lessThanOrEqualTo(0));
    button.setOnAction(
        _ -> {
          final TalkPageTextModel selected = tableView.getSelectionModel().getSelectedItem();
          if (selected != null) {
            final int index = tableView.getSelectionModel().getSelectedIndex();
            if (index > 0) {
              models.remove(index);
              models.add(index - 1, selected);
              tableView.getSelectionModel().select(index - 1);
            }
          }
        });
    return button;
  }

  private Button createMoveDownButton() {
    final Button button = new Button();
    button.setStyle(DefaultStyles.TOOLBAR_ELEMENT);
    imageLoader
        .getImageView(ImageCollection.MOVE_DOWN, ImageSize.BUTTON)
        .ifPresent(button::setGraphic);
    button.setTooltip(new Tooltip(GT._T("Move down")));
    button
        .disableProperty()
        .bind(
            tableView
                .getSelectionModel()
                .selectedIndexProperty()
                .lessThan(0)
                .or(
                    tableView
                        .getSelectionModel()
                        .selectedIndexProperty()
                        .greaterThanOrEqualTo(Bindings.size(tableView.getItems()).subtract(1))));
    button.setOnAction(
        _ -> {
          final TalkPageTextModel selected = tableView.getSelectionModel().getSelectedItem();
          if (selected != null) {
            final int index = tableView.getSelectionModel().getSelectedIndex();
            if (index >= 0 && index < models.size() - 1) {
              models.remove(index);
              models.add(index + 1, selected);
              tableView.getSelectionModel().select(index + 1);
            }
          }
        });
    return button;
  }

  private Button createMoveLastButton() {
    final Button button = new Button();
    button.setStyle(DefaultStyles.TOOLBAR_ELEMENT);
    imageLoader
        .getImageView(ImageCollection.MOVE_LAST, ImageSize.BUTTON)
        .ifPresent(button::setGraphic);
    button.setTooltip(new Tooltip(GT._T("Move last")));
    button
        .disableProperty()
        .bind(
            tableView
                .getSelectionModel()
                .selectedIndexProperty()
                .lessThan(0)
                .or(
                    tableView
                        .getSelectionModel()
                        .selectedIndexProperty()
                        .greaterThanOrEqualTo(Bindings.size(tableView.getItems()).subtract(1))));
    button.setOnAction(
        _ -> {
          final TalkPageTextModel selected = tableView.getSelectionModel().getSelectedItem();
          if (selected != null) {
            final int index = tableView.getSelectionModel().getSelectedIndex();
            if (index >= 0 && index < models.size() - 1) {
              models.remove(index);
              models.add(selected);
              tableView.getSelectionModel().select(models.size() - 1);
            }
          }
        });
    return button;
  }

  private UserPageHostingConfig convertResult() {
    final List<UserTalkPageTextConfig> texts = new ArrayList<>();
    for (final TalkPageTextModel model : models) {
      final String lbl = model.labelProperty().get();
      final String txt = model.textProperty().get();
      if (lbl != null && !lbl.isBlank() && txt != null && !txt.isBlank()) {
        texts.add(new UserTalkPageTextConfig(lbl, txt, model.addedByDefaultProperty().get()));
      }
    }
    return new UserPageHostingConfig(
        userPageSummaryField.getText(),
        userPageTextArea.getText(),
        userTalkPageSummaryField.getText(),
        texts);
  }
}
