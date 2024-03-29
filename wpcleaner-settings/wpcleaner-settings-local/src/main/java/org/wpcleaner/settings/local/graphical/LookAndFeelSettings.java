package org.wpcleaner.settings.local.graphical;

/*
 * SPDX-FileCopyrightText: © 2024 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import jakarta.annotation.Nullable;
import java.util.Objects;

public record LookAndFeelSettings(LookAndFeelType type, String name) {

  public static final String DEFAULT_NAME = "Nimbus";

  public LookAndFeelSettings(@Nullable final LookAndFeelType type, @Nullable final String name) {
    this.type = Objects.requireNonNullElse(type, LookAndFeelType.WPCLEANER);
    this.name = Objects.requireNonNullElse(name, DEFAULT_NAME);
  }

  public LookAndFeelSettings() {
    this(null, null);
  }

  public String name() {
    return name.isBlank() ? DEFAULT_NAME : name;
  }
}
