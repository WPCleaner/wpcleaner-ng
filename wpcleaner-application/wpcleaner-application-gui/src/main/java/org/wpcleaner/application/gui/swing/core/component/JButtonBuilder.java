package org.wpcleaner.application.gui.swing.core.component;

/*
 * SPDX-FileCopyrightText: © 2024 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Optional;
import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JToggleButton;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.application.gui.swing.core.action.ComponentAction;
import org.wpcleaner.application.gui.swing.core.action.ComponentActionListener;
import org.wpcleaner.application.gui.swing.core.image.ImageIconLoader;
import org.wpcleaner.lib.image.ImageCollection;
import org.wpcleaner.lib.image.ImageSize;

public class JButtonBuilder {

  private final ImageIconLoader imageService;
  private final String message;
  private final boolean displayMessage;
  private boolean toggle;
  @Nullable private ImageCollection image;
  @Nullable private ImageCollection selectedImage;
  private ImageSize iconSize = ImageSize.BUTTON;
  @Nullable private ComponentAction componentAction;

  JButtonBuilder(
      final ImageIconLoader imageService, final String message, final boolean displayMessage) {
    this.imageService = imageService;
    this.message = message;
    this.displayMessage = displayMessage;
  }

  public JButtonBuilder withToggle(final boolean toggle) {
    this.toggle = toggle;
    return this;
  }

  public JButtonBuilder withIcon(final ImageCollection image, final ImageSize imageSize) {
    this.image = image;
    this.iconSize = imageSize;
    return this;
  }

  public JButtonBuilder withSelectedIcon(final ImageCollection selectedImage) {
    this.selectedImage = selectedImage;
    return this;
  }

  public JButtonBuilder withAction(final Runnable action) {
    this.componentAction = _ -> action.run();
    return this;
  }

  public JButtonBuilder withAction(final ComponentAction action) {
    this.componentAction = action;
    return this;
  }

  public AbstractButton build() {
    final AbstractButton button =
        buildButton(
            message,
            Optional.ofNullable(image)
                .flatMap(name -> imageService.getImage(name, iconSize))
                .orElse(null));
    Optional.ofNullable(selectedImage)
        .flatMap(name -> imageService.getImage(name, iconSize))
        .ifPresent(button::setSelectedIcon);
    button.setToolTipText(message);
    if (componentAction != null) {
      button.addActionListener(new ComponentActionListener(componentAction, button));
    }
    return button;
  }

  private AbstractButton buildButton(final String message, @Nullable final ImageIcon icon) {
    if (icon == null) {
      if (toggle) {
        return new JToggleButton(message);
      }
      return new JButton(message);
    }
    if (displayMessage) {
      if (toggle) {
        return new JToggleButton(message, icon);
      }
      return new JButton(message, icon);
    }
    if (toggle) {
      return new JToggleButton(icon);
    }
    return new JButton(icon);
  }
}
