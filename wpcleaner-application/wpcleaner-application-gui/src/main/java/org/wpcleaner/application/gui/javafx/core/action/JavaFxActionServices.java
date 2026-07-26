package org.wpcleaner.application.gui.javafx.core.action;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import javafx.stage.Window;
import org.springframework.stereotype.Service;
import org.wpcleaner.application.gui.core.desktop.DesktopService;
import org.wpcleaner.application.gui.javafx.JavaFxInitializer;
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

  public void positionWindow(final Window window, final String name) {
    windowsSettings
        .getCurrentSettings()
        .getWindowSettings(name)
        .ifPresentOrElse(
            windowSettings -> {
              window.setX(windowSettings.x());
              window.setY(windowSettings.y());
              window.setWidth(windowSettings.width());
              window.setHeight(windowSettings.height());
            },
            window::sizeToScene);
  }
}
