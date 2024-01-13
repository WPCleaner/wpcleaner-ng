package org.wpcleaner.gui.swing.core.component;

/*
 * SPDX-FileCopyrightText: © 2024 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import jakarta.annotation.Nullable;
import java.util.Optional;
import javax.swing.JButton;
import org.wpcleaner.gui.core.action.SimpleAction;
import org.wpcleaner.gui.swing.core.action.ComponentAction;
import org.wpcleaner.gui.swing.core.action.ComponentActionListener;
import org.wpcleaner.gui.swing.core.action.SimpleToComponentAction;
import org.wpcleaner.gui.swing.core.image.ImageIconLoader;
import org.wpcleaner.lib.image.ImageCollection;
import org.wpcleaner.lib.image.ImageSize;

public class JButtonBuilder {

  private final ImageIconLoader imageService;
  private final String message;
  private final boolean displayMessage;
  @Nullable private ImageCollection image;
  private ImageSize iconSize = ImageSize.BUTTON;
  @Nullable private ComponentAction componentAction;

  JButtonBuilder(
      final ImageIconLoader imageService, final String message, final boolean displayMessage) {
    this.imageService = imageService;
    this.message = message;
    this.displayMessage = displayMessage;
  }

  public JButtonBuilder withIcon(final ImageCollection image, final ImageSize imageSize) {
    this.image = image;
    this.iconSize = imageSize;
    return this;
  }

  public JButtonBuilder withAction(final SimpleAction action) {
    this.componentAction = new SimpleToComponentAction(action);
    return this;
  }

  public JButtonBuilder withAction(final ComponentAction action) {
    this.componentAction = action;
    return this;
  }

  public JButton build() {
    final JButton button =
        Optional.ofNullable(image)
            .flatMap(name -> imageService.getImage(name, iconSize))
            .map(icon -> displayMessage ? new JButton(message, icon) : new JButton(icon))
            .orElseGet(() -> new JButton(message));
    button.setToolTipText(message);
    if (componentAction != null) {
      button.addActionListener(new ComponentActionListener(componentAction, button));
    }
    return button;
  }
}
