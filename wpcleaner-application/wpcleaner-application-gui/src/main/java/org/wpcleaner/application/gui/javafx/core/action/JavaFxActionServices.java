package org.wpcleaner.application.gui.javafx.core.action;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import javafx.stage.Stage;
import org.springframework.stereotype.Service;
import org.wpcleaner.application.gui.core.desktop.DesktopService;
import org.wpcleaner.application.gui.javafx.JavaFxInitializer;
import org.wpcleaner.application.gui.javafx.core.window.JavaFxWindow;
import org.wpcleaner.application.gui.settings.windows.WindowSettings;
import org.wpcleaner.application.gui.settings.windows.WindowsSettingsManager;

@Service
public record JavaFxActionServices(
    DesktopService desktopService,
    JavaFxNotImplementedAction notImplemented,
    JavaFxSaveWindowsPositionAction saveWindowsPosition,
    WindowsSettingsManager windowsSettings) {

  public void browse(final String url) {
    JavaFxInitializer.browse(desktopService, url);
  }

  public void positionWindow(final JavaFxWindow<?> window) {
    windowsSettings
        .getCurrentSettings()
        .getWindowSettings(window.getName())
        .ifPresentOrElse(
            windowSettings -> positionWindow(window, windowSettings),
            () -> window.getStage().sizeToScene());
  }

  public void positionWindow(final JavaFxWindow<?> window, final WindowSettings settings) {
    final Stage stage = window.getStage();
    stage.setX(settings.x());
    stage.setY(settings.y());
    stage.setWidth(settings.width());
    stage.setHeight(settings.height());
  }
}
