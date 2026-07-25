package org.wpcleaner.application.gui.javafx.recentchanges;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.api.api.query.list.tags.Tag;
import org.wpcleaner.api.repository.namespace.Namespace;
import org.wpcleaner.application.gui.javafx.JavaFxImageLoader;
import org.wpcleaner.application.gui.javafx.core.MoveDownButton;
import org.wpcleaner.application.gui.javafx.core.MoveFirstButton;
import org.wpcleaner.application.gui.javafx.core.MoveLastButton;
import org.wpcleaner.application.gui.javafx.core.MoveUpButton;
import org.wpcleaner.lib.image.ImageCollection;
import org.wpcleaner.lib.image.ImageSize;

public final class RecentChangesFilterListView extends ListView<@Nullable RecentChangesFilter> {

  public RecentChangesFilterListView(
      final JavaFxImageLoader imageLoader,
      final List<Namespace> availableNamespaces,
      final List<Tag> availableTags,
      final ToolBar toolbar) {
    super();
    setPrefHeight(150);
    setPrefWidth(250);
    setCellFactory(
        _ ->
            new ListCell<>() {
              @Override
              protected void updateItem(
                  @Nullable final RecentChangesFilter item, final boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                  setText(null);
                  setGraphic(null);
                } else {
                  setText(item.name());
                  if (item.severity() != null) {
                    imageLoader
                        .getImageView(item.severity().getImage(), ImageSize.BUTTON)
                        .ifPresentOrElse(this::setGraphic, () -> setGraphic(null));
                  } else {
                    setGraphic(null);
                  }
                }
              }
            });

    final Button addFilterButton = createAddButton(imageLoader, availableNamespaces, availableTags);
    final Button editFilterButton =
        createEditButton(imageLoader, availableNamespaces, availableTags);
    final Button removeFilterButton = createRemoveButton(imageLoader);

    final Button moveFirstButton = new MoveFirstButton<>(imageLoader, this);
    final Button moveUpButton = new MoveUpButton<>(imageLoader, this);
    final Button moveDownButton = new MoveDownButton<>(imageLoader, this);
    final Button moveLastButton = new MoveLastButton<>(imageLoader, this);

    toolbar
        .getItems()
        .addAll(
            addFilterButton,
            editFilterButton,
            removeFilterButton,
            new Separator(Orientation.VERTICAL),
            moveFirstButton,
            moveUpButton,
            moveDownButton,
            moveLastButton);
  }

  private Button createAddButton(
      final JavaFxImageLoader imageLoader,
      final List<Namespace> availableNamespaces,
      final List<Tag> availableTags) {
    final Button button = new Button();
    button.setStyle("-fx-background-color: transparent; -fx-padding: 1px;");
    imageLoader
        .getImageView(ImageCollection.LIST_ADD, ImageSize.BUTTON)
        .ifPresent(button::setGraphic);
    button.setTooltip(new Tooltip("Add"));
    button.setOnAction(
        _ -> {
          if (getScene() != null) {
            final javafx.stage.Window window = getScene().getWindow();
            if (window != null) {
              RecentChangesFilterDialog.showDialog(
                      window, imageLoader, availableNamespaces, availableTags, null)
                  .ifPresent(newFilter -> getItems().add(newFilter));
            }
          }
        });
    return button;
  }

  private Button createEditButton(
      final JavaFxImageLoader imageLoader,
      final List<Namespace> availableNamespaces,
      final List<Tag> availableTags) {
    final Button button = new Button();
    button.setStyle("-fx-background-color: transparent; -fx-padding: 1px;");
    imageLoader.getImageView(ImageCollection.EDIT, ImageSize.BUTTON).ifPresent(button::setGraphic);
    button.setTooltip(new Tooltip("Edit"));
    button.disableProperty().bind(getSelectionModel().selectedItemProperty().isNull());
    button.setOnAction(
        _ -> {
          final RecentChangesFilter selectedFilter = getSelectionModel().getSelectedItem();
          if (selectedFilter != null && getScene() != null) {
            final javafx.stage.Window window = getScene().getWindow();
            if (window != null) {
              final int selectedIndex = getSelectionModel().getSelectedIndex();
              RecentChangesFilterDialog.showDialog(
                      window, imageLoader, availableNamespaces, availableTags, selectedFilter)
                  .ifPresent(editedFilter -> getItems().set(selectedIndex, editedFilter));
            }
          }
        });
    return button;
  }

  private Button createRemoveButton(final JavaFxImageLoader imageLoader) {
    final Button button = new Button();
    button.setStyle("-fx-background-color: transparent; -fx-padding: 1px;");
    imageLoader
        .getImageView(ImageCollection.LIST_REMOVE, ImageSize.BUTTON)
        .ifPresent(button::setGraphic);
    button.setTooltip(new Tooltip("Remove"));
    button.disableProperty().bind(getSelectionModel().selectedItemProperty().isNull());
    button.setOnAction(
        _ -> {
          final int selectedIndex = getSelectionModel().getSelectedIndex();
          if (selectedIndex >= 0) {
            getItems().remove(selectedIndex);
          }
        });
    return button;
  }
}
