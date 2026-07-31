package org.wpcleaner.application.gui.javafx.login;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import javafx.scene.control.ListCell;
import org.wpcleaner.application.gui.javafx.JavaFxImageLoader;
import org.wpcleaner.lib.image.ImageSize;

final class WikiBuilderTypeListCell extends ListCell<WikiBuilderType> {

  private final JavaFxImageLoader imageLoader;

  WikiBuilderTypeListCell(final JavaFxImageLoader imageLoader) {
    this.imageLoader = imageLoader;
  }

  @Override
  protected void updateItem(final WikiBuilderType item, final boolean empty) {
    super.updateItem(item, empty);
    if (empty) {
      setText(null);
      setGraphic(null);
    } else {
      setText(item.toString());
      imageLoader.getImageView(item.getImage(), ImageSize.MENU).ifPresent(this::setGraphic);
    }
  }
}
