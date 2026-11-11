package org.wpcleaner.launcher.platform;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

public record Platform(OperatingSystem operatingSystem, Architecture architecture) {

  public static Platform current() {
    return new Platform(
        OperatingSystem.fromName(System.getProperty("os.name")),
        Architecture.fromName(System.getProperty("os.arch")));
  }

  public String key() {
    return "%s-%s".formatted(operatingSystem.key(), architecture.key());
  }
}
