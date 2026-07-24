package org.wpcleaner.application.gui.javafx.core;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.Tooltip;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.application.gui.javafx.JavaFxImageLoader;
import org.wpcleaner.lib.image.ImageCollection;
import org.wpcleaner.lib.image.ImageSize;

public final class MoveFirstButton<T> extends Button {

  public MoveFirstButton(
      final JavaFxImageLoader imageLoader, final ListView<@Nullable T> listView) {
    super();
    setStyle("-fx-background-color: transparent; -fx-padding: 1px;");
    imageLoader
        .getImageView(ImageCollection.MOVE_FIRST, ImageSize.BUTTON)
        .ifPresent(this::setGraphic);
    setTooltip(new Tooltip("Move first"));
    disableProperty()
        .bind(listView.getSelectionModel().selectedIndexProperty().lessThanOrEqualTo(0));
    setOnAction(
        _ -> {
          final T selected = listView.getSelectionModel().getSelectedItem();
          if (selected != null) {
            final int index = listView.getSelectionModel().getSelectedIndex();
            if (index > 0) {
              listView.getItems().remove(index);
              listView.getItems().addFirst(selected);
              listView.getSelectionModel().select(0);
            }
          }
        });
  }
}
