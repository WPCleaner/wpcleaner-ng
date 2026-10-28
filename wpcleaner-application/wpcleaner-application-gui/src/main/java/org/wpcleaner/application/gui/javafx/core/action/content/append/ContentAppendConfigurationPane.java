package org.wpcleaner.application.gui.javafx.core.action.content.append;

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
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
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
import org.wpcleaner.lib.image.ImageLoader;
import org.wpcleaner.lib.image.ImageSize;

public final class ContentAppendConfigurationPane extends TitledPane {

  public static final String ADD_ID = "add";
  public static final String REMOVE_ID = "remove";
  public static final String SUMMARY_ID = "summary";
  public static final String TEXTS_ID = "texts";

  private static final double LABEL_WIDTH = 120.0;

  private final JavaFxImageLoader imageLoader;
  private final ObservableList<ContentAppendTextModel> models = FXCollections.observableArrayList();
  private final TextField summaryField;
  private final TableView<@Nullable ContentAppendTextModel> tableView = new TableView<>();

  public ContentAppendConfigurationPane(
      final String title,
      final ImageLoader imageLoader,
      @Nullable final ContentAppendConfiguration initialConfig) {
    super();
    setText(title);
    setCollapsible(false);
    setMaxHeight(Double.MAX_VALUE);

    this.imageLoader = new JavaFxImageLoader(imageLoader);
    this.summaryField = new TextField();
    this.summaryField.setId(SUMMARY_ID);

    if (initialConfig != null) {
      this.summaryField.setText(initialConfig.summary());
      for (final ContentAppendConfigurationText text : initialConfig.texts()) {
        models.add(new ContentAppendTextModel(text));
      }
    }

    final GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);
    grid.setMaxHeight(Double.MAX_VALUE);

    final Label summaryLabel = new Label(GT._T("Summary:"));
    summaryLabel.setMinWidth(LABEL_WIDTH);
    summaryLabel.setPrefWidth(LABEL_WIDTH);
    grid.add(summaryLabel, 0, 0);
    grid.add(summaryField, 1, 0);
    GridPane.setHgrow(summaryField, Priority.ALWAYS);

    final Label staticTextsLabel = new Label(GT._T("Additional texts:"));
    staticTextsLabel.setMinWidth(LABEL_WIDTH);
    staticTextsLabel.setPrefWidth(LABEL_WIDTH);
    tableView.setId(TEXTS_ID);
    tableView.setEditable(true);
    tableView.setPrefHeight(150);
    tableView.setMaxHeight(Double.MAX_VALUE);
    tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

    final TableColumn<ContentAppendTextModel, Boolean> defaultCol =
        new TableColumn<>(GT._T("By default"));
    defaultCol.setCellValueFactory(data -> data.getValue().addedByDefaultProperty());
    defaultCol.setCellFactory(CheckBoxTableCell.forTableColumn(defaultCol));
    defaultCol.setMinWidth(100);
    defaultCol.setMaxWidth(100);
    defaultCol.setSortable(false);

    final TableColumn<ContentAppendTextModel, String> labelCol = new TableColumn<>(GT._T("Label"));
    labelCol.setCellValueFactory(data -> data.getValue().labelProperty());
    labelCol.setCellFactory(TextFieldTableCell.forTableColumn());
    labelCol.setPrefWidth(120);
    labelCol.setSortable(false);

    final TableColumn<ContentAppendTextModel, String> textCol =
        new TableColumn<>(GT._T("Additional text"));
    textCol.setCellValueFactory(data -> data.getValue().textProperty());
    textCol.setCellFactory(_ -> new ContentAppendTextAreaTableCell<>());
    textCol.setSortable(false);

    tableView.getColumns().add(defaultCol);
    tableView.getColumns().add(labelCol);
    tableView.getColumns().add(textCol);
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
    VBox.setVgrow(tableView, Priority.ALWAYS);
    grid.add(staticTextsLabel, 0, 1);
    grid.add(tableBox, 1, 1);
    GridPane.setHgrow(tableBox, Priority.ALWAYS);
    GridPane.setVgrow(tableBox, Priority.ALWAYS);

    setContent(grid);
  }

  private Button createAddButton() {
    final Button button = new Button();
    button.setId(ADD_ID);
    button.setStyle(DefaultStyles.TOOLBAR_ELEMENT);
    imageLoader
        .getImageView(ImageCollection.LIST_ADD, ImageSize.BUTTON)
        .ifPresent(button::setGraphic);
    button.setTooltip(new Tooltip(GT._T("Add")));
    button.setOnAction(_ -> models.add(new ContentAppendTextModel(false, "", "")));
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
          final ContentAppendTextModel selected = tableView.getSelectionModel().getSelectedItem();
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
          final ContentAppendTextModel selected = tableView.getSelectionModel().getSelectedItem();
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
          final ContentAppendTextModel selected = tableView.getSelectionModel().getSelectedItem();
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
          final ContentAppendTextModel selected = tableView.getSelectionModel().getSelectedItem();
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

  private Button createRemoveButton() {
    final Button button = new Button();
    button.setId(REMOVE_ID);
    button.setStyle(DefaultStyles.TOOLBAR_ELEMENT);
    imageLoader
        .getImageView(ImageCollection.LIST_REMOVE, ImageSize.BUTTON)
        .ifPresent(button::setGraphic);
    button.setTooltip(new Tooltip(GT._T("Remove")));
    button.disableProperty().bind(tableView.getSelectionModel().selectedItemProperty().isNull());
    button.setOnAction(
        _ -> {
          final ContentAppendTextModel selected = tableView.getSelectionModel().getSelectedItem();
          if (selected != null) {
            models.remove(selected);
          }
        });
    return button;
  }

  public ContentAppendConfiguration getContentAppendConfiguration() {
    return new ContentAppendConfiguration(getSummary(), getTexts());
  }

  public String getSummary() {
    return summaryField.getText();
  }

  public List<ContentAppendConfigurationText> getTexts() {
    final List<ContentAppendConfigurationText> texts = new ArrayList<>();
    for (final ContentAppendTextModel model : models) {
      final String lbl = model.labelProperty().get();
      final String txt = model.textProperty().get();
      if (lbl != null && !lbl.isBlank() && txt != null && !txt.isBlank()) {
        texts.add(
            new ContentAppendConfigurationText(model.addedByDefaultProperty().get(), lbl, txt));
      }
    }
    return List.copyOf(texts);
  }

  public boolean isComplete() {
    return !summaryField.getText().isBlank();
  }
}
