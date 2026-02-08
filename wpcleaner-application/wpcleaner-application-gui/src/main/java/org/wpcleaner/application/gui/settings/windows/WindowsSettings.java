package org.wpcleaner.application.gui.settings.windows;

/*
 * SPDX-FileCopyrightText: © 2024 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.wpcleaner.api.settings.VersionedSettings;

public record WindowsSettings(int version, Map<String, WindowSettings> windows)
    implements VersionedSettings {

  public static final int LAST_VERSION = 1;

  public WindowsSettings() {
    this(0, Map.of());
  }

  public Optional<WindowSettings> getWindowSettings(final String name) {
    return Optional.ofNullable(windows.get(name));
  }

  public WindowsSettings withWindowSettings(
      final String name, final WindowSettings windowsSettings) {
    final Map<String, WindowSettings> newWindows = new HashMap<>(windows);
    newWindows.put(name, windowsSettings);
    return new WindowsSettings(version, newWindows);
  }

  @Override
  public int lastVersion() {
    return LAST_VERSION;
  }
}
