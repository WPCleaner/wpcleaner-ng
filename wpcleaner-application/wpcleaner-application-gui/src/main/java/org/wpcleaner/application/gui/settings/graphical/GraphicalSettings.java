package org.wpcleaner.application.gui.settings.graphical;

/*
 * SPDX-FileCopyrightText: © 2024 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import jakarta.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.wpcleaner.api.settings.VersionedSettings;

public record GraphicalSettings(
    int version, Map<String, WindowSettings> windows, LookAndFeelSettings lookAndFeel)
    implements VersionedSettings {

  public static final int LAST_VERSION = 1;

  public GraphicalSettings(
      final int version,
      final Map<String, WindowSettings> windows,
      @Nullable final LookAndFeelSettings lookAndFeel) {
    this.version = version;
    this.windows = windows;
    this.lookAndFeel = Objects.requireNonNullElseGet(lookAndFeel, LookAndFeelSettings::new);
  }

  public GraphicalSettings() {
    this(0, Map.of(), null);
  }

  public Optional<WindowSettings> getWindowSettings(final String name) {
    return Optional.ofNullable(windows.get(name));
  }

  public GraphicalSettings withWindowSettings(
      final String name, final WindowSettings windowSettings) {
    final Map<String, WindowSettings> newWindows = new HashMap<>(windows);
    newWindows.put(name, windowSettings);
    return new GraphicalSettings(version, newWindows, lookAndFeel);
  }

  @Override
  public int lastVersion() {
    return LAST_VERSION;
  }
}
