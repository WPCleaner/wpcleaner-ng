package org.wpcleaner.application.gui.core.style;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import javafx.scene.paint.Color;

public record StyleProperties(
    boolean enabled,
    boolean foreground,
    Color foregroundColor,
    boolean background,
    Color backgroundColor,
    boolean italic,
    boolean bold,
    boolean underline,
    boolean strikeThrough) {

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {

    private boolean enabled = true;
    private boolean foreground;
    private Color foregroundColor = Color.BLACK;
    private boolean background;
    private Color backgroundColor = Color.WHITE;
    private boolean italic;
    private boolean bold;
    private boolean underline;
    private boolean strikeThrough;

    private Builder() {}

    public Builder withEnabled(final boolean enabled) {
      this.enabled = enabled;
      return this;
    }

    public Builder withForeground(final boolean foreground) {
      this.foreground = foreground;
      return this;
    }

    public Builder withForegroundColor(final Color foregroundColor) {
      this.foregroundColor = foregroundColor;
      return this;
    }

    public Builder withBackground(final boolean background) {
      this.background = background;
      return this;
    }

    public Builder withBackgroundColor(final Color backgroundColor) {
      this.backgroundColor = backgroundColor;
      return this;
    }

    public Builder withItalic(final boolean italic) {
      this.italic = italic;
      return this;
    }

    public Builder withBold(final boolean bold) {
      this.bold = bold;
      return this;
    }

    public Builder withUnderline(final boolean underline) {
      this.underline = underline;
      return this;
    }

    public Builder withStrikeThrough(final boolean strikeThrough) {
      this.strikeThrough = strikeThrough;
      return this;
    }

    public StyleProperties build() {
      return new StyleProperties(
          enabled,
          foreground,
          foregroundColor,
          background,
          backgroundColor,
          italic,
          bold,
          underline,
          strikeThrough);
    }
  }
}
