/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wpcleaner.application.gui.javafx;

import java.io.InputStream;
import java.util.Optional;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.wpcleaner.lib.image.ImageCollection;
import org.wpcleaner.lib.image.ImageLoader;
import org.wpcleaner.lib.image.ImageSize;

public final class JavaFxImageLoader {

  private final ImageLoader imageLoader;

  public JavaFxImageLoader(final ImageLoader imageLoader) {
    this.imageLoader = imageLoader;
  }

  @SuppressWarnings("PMD.AvoidCatchingGenericException")
  public Optional<Image> getImage(final ImageCollection image, final ImageSize size) {
    return imageLoader
        .getImageResource(image, size)
        .flatMap(
            resource -> {
              try (InputStream is = resource.getInputStream()) {
                return Optional.of(new Image(is));
              } catch (final java.io.IOException
                  | NullPointerException
                  | IllegalArgumentException e) {
                return Optional.empty();
              }
            });
  }

  public Optional<ImageView> getImageView(final ImageCollection image, final ImageSize size) {
    return getImage(image, size).map(ImageView::new);
  }

  public void setWindowIcon(final Stage stage) {
    getImage(ImageCollection.LOGO_WPCLEANER, ImageSize.ICON)
        .ifPresent(img -> stage.getIcons().add(img));
  }
}
