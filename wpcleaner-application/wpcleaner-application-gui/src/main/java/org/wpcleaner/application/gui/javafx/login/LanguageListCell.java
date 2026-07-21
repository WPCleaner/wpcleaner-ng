/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wpcleaner.application.gui.javafx.login;

import javafx.scene.control.ListCell;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.api.language.Language;
import org.wpcleaner.application.gui.javafx.JavaFxImageLoader;
import org.wpcleaner.lib.image.ImageSize;

final class LanguageListCell extends ListCell<@Nullable Language> {

  private final JavaFxImageLoader imageLoader;

  LanguageListCell(final JavaFxImageLoader imageLoader) {
    this.imageLoader = imageLoader;
  }

  @Override
  protected void updateItem(@Nullable final Language item, final boolean empty) {
    super.updateItem(item, empty);
    if (empty || item == null) {
      setText(null);
      setGraphic(null);
    } else {
      setText("%s - %s".formatted(item.getCode(), item.getDescription()));
      imageLoader.getImageView(item.getImage(), ImageSize.MENU).ifPresent(this::setGraphic);
    }
  }
}
