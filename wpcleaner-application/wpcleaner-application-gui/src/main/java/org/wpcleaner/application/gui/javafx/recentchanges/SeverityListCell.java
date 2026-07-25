package org.wpcleaner.application.gui.javafx.recentchanges;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import javafx.scene.control.ListCell;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.application.gui.javafx.JavaFxImageLoader;
import org.wpcleaner.lib.image.ImageSize;

final class SeverityListCell extends ListCell<@Nullable Severity> {

  private final JavaFxImageLoader imageLoader;

  SeverityListCell(final JavaFxImageLoader imageLoader) {
    this.imageLoader = imageLoader;
  }

  @Override
  protected void updateItem(@Nullable final Severity item, final boolean empty) {
    super.updateItem(item, empty);
    if (empty || item == null) {
      setText(null);
      setGraphic(null);
    } else {
      setText(item.getName());
      imageLoader.getImageView(item.getImage(), ImageSize.BUTTON).ifPresent(this::setGraphic);
    }
  }
}
