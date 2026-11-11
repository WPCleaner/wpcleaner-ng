package org.wpcleaner.launcher.platform;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Locale;

public enum OperatingSystem {
  LINUX("linux"),
  MACOS("macos"),
  WINDOWS("windows");

  private final String value;

  OperatingSystem(final String key) {
    this.value = key;
  }

  public String key() {
    return value;
  }

  public static OperatingSystem fromName(final String name) {
    final String lowerCaseName = name.toLowerCase(Locale.ROOT);
    if (lowerCaseName.contains("mac")) {
      return MACOS;
    }
    if (lowerCaseName.contains("win")) {
      return WINDOWS;
    }
    return LINUX;
  }
}
