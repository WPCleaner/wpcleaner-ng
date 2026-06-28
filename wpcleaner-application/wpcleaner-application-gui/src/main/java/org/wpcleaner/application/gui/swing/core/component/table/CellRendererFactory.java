package org.wpcleaner.application.gui.swing.core.component.table;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.springframework.stereotype.Service;
import org.wpcleaner.application.gui.swing.core.image.ImageIconLoader;
import org.wpcleaner.lib.image.ImageCollection;
import org.wpcleaner.lib.image.ImageSize;

@Service
public class CellRendererFactory {

  private final ImageIconLoader imageLoader;

  public CellRendererFactory(final ImageIconLoader imageLoader) {
    this.imageLoader = imageLoader;
  }

  public IconWithTooltipCellRenderer iconWithTooltip(
      final ImageCollection image, final boolean displayIfEmpty) {
    return new IconWithTooltipCellRenderer(
        imageLoader.getImage(image, ImageSize.TOOLBAR).orElse(null), displayIfEmpty);
  }
}
