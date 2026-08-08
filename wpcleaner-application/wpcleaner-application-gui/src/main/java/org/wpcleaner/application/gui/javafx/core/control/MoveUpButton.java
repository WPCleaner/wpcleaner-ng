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

public final class MoveUpButton<T> extends Button {

  public MoveUpButton(final JavaFxImageLoader imageLoader, final ListView<@Nullable T> listView) {
    super();
    setStyle(DefaultStyles.TOOLBAR_ELEMENT);
    imageLoader.getImageView(ImageCollection.MOVE_UP, ImageSize.BUTTON).ifPresent(this::setGraphic);
    setTooltip(new Tooltip(GT._T("Move up")));
    disableProperty()
        .bind(listView.getSelectionModel().selectedIndexProperty().lessThanOrEqualTo(0));
    setOnAction(
        _ -> {
          final T selected = listView.getSelectionModel().getSelectedItem();
          if (selected != null) {
            final int index = listView.getSelectionModel().getSelectedIndex();
            if (index > 0) {
              listView.getItems().remove(index);
              listView.getItems().add(index - 1, selected);
              listView.getSelectionModel().select(index - 1);
            }
          }
        });
  }
}
