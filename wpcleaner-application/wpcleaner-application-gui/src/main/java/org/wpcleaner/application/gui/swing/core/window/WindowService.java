package org.wpcleaner.application.gui.swing.core.window;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.awt.Window;
import org.springframework.stereotype.Service;
import org.wpcleaner.settings.local.graphical.GraphicalSettings;
import org.wpcleaner.settings.local.graphical.GraphicalSettingsManager;
import org.wpcleaner.settings.local.graphical.WindowSettings;

@Service
public class WindowService {

  private final GraphicalSettingsManager settingsManager;
  private final WindowsRegistry registry;

  public WindowService(
      final GraphicalSettingsManager settingsManager, final WindowsRegistry registry) {
    this.settingsManager = settingsManager;
    this.registry = registry;
  }

  public void saveAllWindowsPosition() {
    GraphicalSettings settings = settingsManager.getCurrentSettings();
    for (final Window window : registry.getVisibleWindows()) {
      settings =
          settings.withWindowSettings(
              window.getName(),
              new WindowSettings(
                  window.getX(), window.getX(), window.getWidth(), window.getHeight()));
    }
    settingsManager.updateSettings(settings);
  }
}
