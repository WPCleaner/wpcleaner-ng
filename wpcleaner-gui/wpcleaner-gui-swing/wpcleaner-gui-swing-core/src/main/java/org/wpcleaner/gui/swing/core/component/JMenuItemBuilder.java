package org.wpcleaner.gui.swing.core.component;

/*
 * SPDX-FileCopyrightText: © 2024 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import jakarta.annotation.Nullable;
import java.util.Optional;
import javax.swing.JMenuItem;
import org.wpcleaner.gui.core.action.SimpleAction;
import org.wpcleaner.gui.swing.core.action.ComponentAction;
import org.wpcleaner.gui.swing.core.action.ComponentActionListener;
import org.wpcleaner.gui.swing.core.action.SimpleToComponentAction;
import org.wpcleaner.gui.swing.core.image.ImageIconLoader;
import org.wpcleaner.lib.image.ImageCollection;
import org.wpcleaner.lib.image.ImageSize;

public class JMenuItemBuilder {

  private final ImageIconLoader imageService;
  private final String message;
  @Nullable private ImageCollection image;
  private ImageSize iconSize = ImageSize.MENU;
  @Nullable private ComponentAction componentAction;

  JMenuItemBuilder(final ImageIconLoader imageService, final String message) {
    this.imageService = imageService;
    this.message = message;
  }

  public JMenuItemBuilder withIcon(final ImageCollection image, final ImageSize imageSize) {
    this.image = image;
    this.iconSize = imageSize;
    return this;
  }

  public JMenuItemBuilder withAction(final SimpleAction action) {
    this.componentAction = new SimpleToComponentAction(action);
    return this;
  }

  public JMenuItem build() {
    final JMenuItem menuItem =
        Optional.ofNullable(image)
            .flatMap(name -> imageService.getImage(name, iconSize))
            .map(icon -> new JMenuItem(message, icon))
            .orElseGet(() -> new JMenuItem(message));
    menuItem.setToolTipText(message);
    if (componentAction != null) {
      menuItem.addActionListener(new ComponentActionListener(componentAction, menuItem));
    }
    return menuItem;
  }
}
