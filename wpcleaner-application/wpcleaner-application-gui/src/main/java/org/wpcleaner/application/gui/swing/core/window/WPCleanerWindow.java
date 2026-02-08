package org.wpcleaner.application.gui.swing.core.window;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.io.Serial;
import javax.swing.JFrame;
import org.wpcleaner.api.utils.StringUtils;

public abstract class WPCleanerWindow<S extends WPCleanerWindowServices> extends JFrame {

  @Serial private static final long serialVersionUID = 1L;

  protected final transient S services;

  protected WPCleanerWindow(final S services) {
    super("WPCleaner");
    this.services = services;
  }

  protected void initialize() {
    setName(computeName());
    services.swing().windows().register(this);
    services.swing().image().setIconImage(this);
    position();
  }

  private void position() {
    services
        .windowsSettings()
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
