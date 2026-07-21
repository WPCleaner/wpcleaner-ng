/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wpcleaner.application.gui.javafx.login;

import javafx.scene.control.ListCell;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.application.gui.javafx.JavaFxImageLoader;
import org.wpcleaner.lib.image.ImageSize;

final class WikiListCell extends ListCell<@Nullable WikiDefinition> {

  private final JavaFxImageLoader imageLoader;

  WikiListCell(final JavaFxImageLoader imageLoader) {
    this.imageLoader = imageLoader;
  }

  @Override
  protected void updateItem(@Nullable final WikiDefinition item, final boolean empty) {
    super.updateItem(item, empty);
    if (empty || item == null) {
      setText(null);
      setGraphic(null);
    } else {
      setText("%s - %s".formatted(item.code(), item.name()));
      imageLoader.getImageView(item.image(), ImageSize.MENU).ifPresent(this::setGraphic);
    }
  }
}
