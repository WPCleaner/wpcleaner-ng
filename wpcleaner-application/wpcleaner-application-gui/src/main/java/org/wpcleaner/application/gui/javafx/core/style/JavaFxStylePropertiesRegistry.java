package org.wpcleaner.application.gui.javafx.core.style;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.awt.Color;
import org.springframework.stereotype.Service;
import org.wpcleaner.application.gui.core.style.StyleProperties;
import org.wpcleaner.application.gui.core.style.StylePropertiesRegistry;

@Service
public record JavaFxStylePropertiesRegistry(StylePropertiesRegistry registry) {

  public String getStyle(final String name) {
    final StyleProperties properties = registry.getStyle(name);
    if (!properties.enabled()) {
      return "";
    }
    final StringBuilder css = new StringBuilder(256);
    if (properties.background()) {
      css.append("-rtfx-background-color: ")
          .append(toCssColor(properties.backgroundColor()))
          .append(';');
    }
    if (properties.foreground()) {
      css.append("-fx-fill: ").append(toCssColor(properties.foregroundColor())).append(';');
    }
    if (properties.italic()) {
      css.append("-fx-font-style: italic;");
    }
    if (properties.bold()) {
      css.append("-fx-font-weight: bold;");
    }
    if (properties.underline()) {
      css.append("-rtfx-underline-color: ")
          .append(toCssColor(properties.foregroundColor()))
          .append(";-fx-underline: true;");
    }
    if (properties.strikeThrough()) {
      css.append("-fx-strikethrough: true;");
    }
    return css.toString();
  }

  private String toCssColor(final Color color) {
    return String.format("#%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue());
  }
}
