package org.wpcleaner.application.gui.swing.core.window;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.io.Serial;
import javax.swing.JFrame;
import org.wpcleaner.api.utils.StringUtils;
import org.wpcleaner.application.gui.settings.graphical.GraphicalSettingsManager;
import org.wpcleaner.application.gui.swing.core.SwingCoreServices;

public abstract class WPCleanerWindow extends JFrame {

  @Serial private static final long serialVersionUID = 1L;

  protected final transient GraphicalSettingsManager settingsManager;
  protected final transient SwingCoreServices swingCore;

  protected WPCleanerWindow(
      final GraphicalSettingsManager settingsManager, final SwingCoreServices swingCore) {
    super("WPCleaner");
    this.settingsManager = settingsManager;
    this.swingCore = swingCore;
  }

  protected void initialize() {
    setName(computeName());
    swingCore.windows().register(this);
    swingCore.image().setIconImage(this);
    position();
  }

  private void position() {
    settingsManager
        .getCurrentSettings()
        .getWindowSettings(getName())
        .ifPresentOrElse(
            windowSettings -> {
              setLocation(windowSettings.x(), windowSettings.y());
              setSize(windowSettings.width(), windowSettings.height());
            },
            () -> setLocationRelativeTo(null));
  }

  private String computeName() {
    return StringUtils.firstLetterLowerCase(
        StringUtils.removeSuffix(
            StringUtils.removePrefix(getClass().getSimpleName(), "Swing"), "Window"));
  }
}
