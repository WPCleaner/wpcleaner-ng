package org.wpcleaner.application.gui.swing.core.window;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.awt.Window;
import org.springframework.stereotype.Service;
import org.wpcleaner.application.gui.settings.windows.WindowSettings;
import org.wpcleaner.application.gui.settings.windows.WindowsSettings;
import org.wpcleaner.application.gui.settings.windows.WindowsSettingsManager;

@Service
public class WindowService {

  private final WindowsSettingsManager settingsManager;
  private final WindowsRegistry registry;

  public WindowService(
      final WindowsSettingsManager settingsManager, final WindowsRegistry registry) {
    this.settingsManager = settingsManager;
    this.registry = registry;
  }

  public void saveAllWindowsPosition() {
    WindowsSettings settings = settingsManager.getCurrentSettings();
    for (final Window window : registry.getVisibleWindows()) {
      settings =
          settings.withWindowSettings(
              window.getName(),
              new WindowSettings(
                  window.getX(), window.getY(), window.getWidth(), window.getHeight()));
    }
    settingsManager.updateSettings(settings);
  }
}
