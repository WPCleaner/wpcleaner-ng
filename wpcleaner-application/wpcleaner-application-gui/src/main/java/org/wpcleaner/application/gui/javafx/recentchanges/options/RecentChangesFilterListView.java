package org.wpcleaner.application.gui.javafx.recentchanges.options;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Optional;
import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.api.utils.GT;
import org.wpcleaner.application.gui.javafx.JavaFxImageLoader;
import org.wpcleaner.application.gui.javafx.core.control.DefaultStyles;
import org.wpcleaner.application.gui.javafx.core.control.MoveDownButton;
import org.wpcleaner.application.gui.javafx.core.control.MoveFirstButton;
import org.wpcleaner.application.gui.javafx.core.control.MoveLastButton;
import org.wpcleaner.application.gui.javafx.core.control.MoveUpButton;
import org.wpcleaner.application.gui.javafx.recentchanges.JavaFxRecentChangesWindowServices;
import org.wpcleaner.lib.image.ImageCollection;
import org.wpcleaner.lib.image.ImageSize;

public final class RecentChangesFilterListView extends ListView<@Nullable RecentChangesFilter> {

  private final JavaFxImageLoader imageLoader;

  public RecentChangesFilterListView(
      final JavaFxRecentChangesWindowServices services, final Stage owner, final ToolBar toolbar) {
    super();
    this.imageLoader = new JavaFxImageLoader(services.imageLoader());
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
                  final ImageView imageView =
                      Optional.ofNullable(item.severity())
                          .flatMap(
                              severity ->
                                  imageLoader.getImageView(severity.getImage(), ImageSize.BUTTON))
                          .orElse(null);
                  setGraphic(imageView);
                }
              }
            });

    final Button addFilterButton = createAddButton(services, owner);
    final Button editFilterButton = createEditButton(services, owner);
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
      final JavaFxRecentChangesWindowServices services, final Stage owner) {
    final Button button = new Button();
    button.setStyle(DefaultStyles.TOOLBAR_ELEMENT);
    imageLoader
        .getImageView(ImageCollection.LIST_ADD, ImageSize.BUTTON)
        .ifPresent(button::setGraphic);
    button.setTooltip(new Tooltip(GT._T("Add")));
    button.setOnAction(
        _ ->
            new RecentChangesFilterWindow(
                services, owner, null, newFilter -> getItems().add(newFilter)));
    return button;
  }

  private Button createEditButton(
      final JavaFxRecentChangesWindowServices services, final Stage owner) {
    final Button button = new Button();
    button.setStyle(DefaultStyles.TOOLBAR_ELEMENT);
    imageLoader.getImageView(ImageCollection.EDIT, ImageSize.BUTTON).ifPresent(button::setGraphic);
    button.setTooltip(new Tooltip(GT._T("Edit")));
    button.disableProperty().bind(getSelectionModel().selectedItemProperty().isNull());
    button.setOnAction(
        _ -> {
          final RecentChangesFilter selectedFilter = getSelectionModel().getSelectedItem();
          if (selectedFilter != null) {
            final int selectedIndex = getSelectionModel().getSelectedIndex();
            new RecentChangesFilterWindow(
                services,
                owner,
                selectedFilter,
                editedFilter -> getItems().set(selectedIndex, editedFilter));
          }
        });
    return button;
  }

  private Button createRemoveButton(final JavaFxImageLoader imageLoader) {
    final Button button = new Button();
    button.setStyle(DefaultStyles.TOOLBAR_ELEMENT);
    imageLoader
        .getImageView(ImageCollection.LIST_REMOVE, ImageSize.BUTTON)
        .ifPresent(button::setGraphic);
    button.setTooltip(new Tooltip(GT._T("Remove")));
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
