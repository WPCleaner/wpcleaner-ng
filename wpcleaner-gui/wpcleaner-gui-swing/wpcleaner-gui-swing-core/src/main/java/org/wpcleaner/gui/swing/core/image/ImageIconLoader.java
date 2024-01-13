package org.wpcleaner.gui.swing.core.image;

/*
 * SPDX-FileCopyrightText: © 2024 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import javax.swing.ImageIcon;
import org.springframework.stereotype.Service;
import org.wpcleaner.lib.image.ImageCollection;
import org.wpcleaner.lib.image.ImageLoader;
import org.wpcleaner.lib.image.ImageSize;

@Service
public class ImageIconLoader {

  private final ImageLoader imageLoader;
  private final Map<ImageSize, Map<ImageCollection, Optional<ImageIcon>>> imageIconsCache;

  public ImageIconLoader(final ImageLoader imageLoader) {
    this.imageLoader = imageLoader;
    this.imageIconsCache = new EnumMap<>(ImageSize.class);
  }

  public Optional<ImageIcon> getImage(final ImageCollection image, final ImageSize size) {
    return imageIconsCache
        .computeIfAbsent(size, key -> new EnumMap<>(ImageCollection.class))
        .computeIfAbsent(image, key -> loadImage(image, size));
  }

  private Optional<ImageIcon> loadImage(final ImageCollection image, final ImageSize size) {
    return imageLoader
        .getImageResource(image, size)
        .map(
            resource -> {
              try {
                return resource.getURL();
              } catch (IOException e) {
                return null;
              }
            })
        .map(ImageIcon::new);
  }
}
