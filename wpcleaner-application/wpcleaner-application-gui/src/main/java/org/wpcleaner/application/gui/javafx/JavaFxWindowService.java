package org.wpcleaner.application.gui.javafx;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import javafx.stage.Window;
import org.springframework.stereotype.Service;
import org.wpcleaner.api.utils.StringUtils;
import org.wpcleaner.application.gui.settings.windows.WindowSettings;
import org.wpcleaner.application.gui.settings.windows.WindowsSettings;
import org.wpcleaner.application.gui.settings.windows.WindowsSettingsManager;

@Service
public class JavaFxWindowService {

  private final WindowsSettingsManager settingsManager;
  private final JavaFxWindowsRegistry registry;

  public JavaFxWindowService(
      final WindowsSettingsManager settingsManager, final JavaFxWindowsRegistry registry) {
    this.settingsManager = settingsManager;
    this.registry = registry;
  }

  public void saveAllWindowsPosition() {
    WindowsSettings settings = settingsManager.getCurrentSettings();
    for (final Window window : registry.getVisibleWindows()) {
      settings =
          settings.withWindowSettings(
              computeName(window),
              new WindowSettings(
                  (int) window.getX(),
                  (int) window.getY(),
                  (int) window.getWidth(),
                  (int) window.getHeight()));
    }
    settingsManager.updateSettings(settings);
  }

  private String computeName(final Window window) {
    return StringUtils.firstLetterLowerCase(
        StringUtils.removeSuffix(
            StringUtils.removePrefix(window.getClass().getSimpleName(), "JavaFx"), "Window"));
  }
}
