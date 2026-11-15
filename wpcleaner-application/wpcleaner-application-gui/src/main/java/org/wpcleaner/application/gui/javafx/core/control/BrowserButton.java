package org.wpcleaner.application.gui.javafx.core.control;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.net.URI;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.application.gui.javafx.JavaFxImageLoader;
import org.wpcleaner.lib.image.ImageCollection;
import org.wpcleaner.lib.image.ImageSize;

public final class BrowserButton extends Button {

  public BrowserButton(
      final JavaFxImageLoader imageLoader,
      final ImageCollection image,
      final String tooltip,
      final Supplier<@Nullable URI> uri,
      final Consumer<String> browse) {
    super();
    setStyle(DefaultStyles.TOOLBAR_ELEMENT);
    imageLoader.getImageView(image, ImageSize.BUTTON).ifPresent(this::setGraphic);
    setTooltip(new Tooltip(tooltip));
    setOnAction(
        _ -> {
          final URI currentUri = uri.get();
          if (currentUri != null) {
            browse.accept(currentUri.toString());
          }
        });
  }
}
