package org.wpcleaner.application.gui.javafx.core.control;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.Tooltip;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.api.utils.GT;
import org.wpcleaner.application.gui.javafx.JavaFxImageLoader;
import org.wpcleaner.lib.image.ImageCollection;
import org.wpcleaner.lib.image.ImageSize;

public final class MoveLastButton<T> extends Button {

  public MoveLastButton(final JavaFxImageLoader imageLoader, final ListView<@Nullable T> listView) {
    super();
    setStyle("-fx-background-color: transparent; -fx-padding: 1px;");
    imageLoader
        .getImageView(ImageCollection.MOVE_LAST, ImageSize.BUTTON)
        .ifPresent(this::setGraphic);
    setTooltip(new Tooltip(GT._T("Move last")));
    disableProperty()
        .bind(
            listView
                .getSelectionModel()
                .selectedIndexProperty()
                .lessThan(0)
                .or(
                    listView
                        .getSelectionModel()
                        .selectedIndexProperty()
                        .greaterThanOrEqualTo(
                            javafx.beans.binding.Bindings.size(listView.getItems()).subtract(1))));
    setOnAction(
        _ -> {
          final T selected = listView.getSelectionModel().getSelectedItem();
          if (selected != null) {
            final int index = listView.getSelectionModel().getSelectedIndex();
            if (index >= 0 && index < listView.getItems().size() - 1) {
              listView.getItems().remove(index);
              listView.getItems().add(selected);
              listView.getSelectionModel().select(listView.getItems().size() - 1);
            }
          }
        });
  }
}
