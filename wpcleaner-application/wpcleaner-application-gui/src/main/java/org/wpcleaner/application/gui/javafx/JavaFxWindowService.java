package org.wpcleaner.application.gui.javafx;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import javafx.stage.Stage;
import org.springframework.stereotype.Service;
import org.wpcleaner.application.gui.javafx.core.window.JavaFxWindow;
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
    for (final JavaFxWindow<?> window : registry.getVisibleWindows()) {
      settings = settings.withWindowSettings(window.getName(), createWindowSettings(window));
    }
    settingsManager.updateSettings(settings);
  }

  private WindowSettings createWindowSettings(final JavaFxWindow<?> window) {
    final Stage stage = window.getStage();
    return new WindowSettings(
        (int) stage.getX(), (int) stage.getY(), (int) stage.getWidth(), (int) stage.getHeight());
  }
}
