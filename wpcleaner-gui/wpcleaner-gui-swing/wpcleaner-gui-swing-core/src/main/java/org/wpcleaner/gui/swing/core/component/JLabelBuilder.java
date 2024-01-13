package org.wpcleaner.gui.swing.core.component;

/*
 * SPDX-FileCopyrightText: © 2024 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import jakarta.annotation.Nullable;
import java.awt.Component;
import java.util.Optional;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import org.wpcleaner.gui.swing.core.image.ImageIconLoader;
import org.wpcleaner.lib.image.ImageCollection;
import org.wpcleaner.lib.image.ImageSize;

public class JLabelBuilder {

  private final ImageIconLoader imageService;
  private final String message;
  private final boolean displayMessage;
  private int horizontalAlignment;
  @Nullable private ImageCollection image;
  private ImageSize iconSize = ImageSize.LABEL;
  @Nullable private Component component;

  JLabelBuilder(
      final ImageIconLoader imageService, final String message, final boolean displayMessage) {
    this.imageService = imageService;
    this.message = message;
    this.displayMessage = displayMessage;
    this.horizontalAlignment = SwingConstants.CENTER;
  }

  public JLabelBuilder withHorizontalAlignment(final int horizontalAlignment) {
    this.horizontalAlignment = horizontalAlignment;
    return this;
  }

  public JLabelBuilder withIcon(final ImageCollection image, final ImageSize imageSize) {
    this.image = image;
    this.iconSize = imageSize;
    return this;
  }

  public JLabelBuilder withComponent(final Component component) {
    this.component = component;
    return this;
  }

  public JLabel build() {
    final JLabel label =
        Optional.ofNullable(image)
            .flatMap(name -> imageService.getImage(name, iconSize))
            .map(
                icon ->
                    displayMessage
                        ? new JLabel(message, icon, horizontalAlignment)
                        : new JLabel(icon, horizontalAlignment))
            .orElseGet(() -> new JLabel(message, horizontalAlignment));
    label.setToolTipText(message);
    label.setLabelFor(component);
    return label;
  }
}
