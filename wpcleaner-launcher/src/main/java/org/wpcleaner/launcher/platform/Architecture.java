package org.wpcleaner.launcher.platform;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Locale;

public enum Architecture {
  AARCH64("aarch64"),
  X64("x64");

  private final String value;

  Architecture(final String key) {
    this.value = key;
  }

  public String key() {
    return value;
  }

  public static Architecture fromName(final String name) {
    final String lowerCaseName = name.toLowerCase(Locale.ROOT);
    if (lowerCaseName.contains("aarch64") || lowerCaseName.contains("arm64")) {
      return AARCH64;
    }
    return X64;
  }
}
