package org.wpcleaner.application.gui.swing.core.window;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.io.Serial;
import javax.swing.JFrame;
import org.wpcleaner.api.utils.StringUtils;
import org.wpcleaner.application.gui.swing.core.SwingCoreServices;

public abstract class WPCleanerWindow extends JFrame {

  @Serial private static final long serialVersionUID = 1L;

  protected final transient SwingCoreServices swingCore;

  protected WPCleanerWindow(final SwingCoreServices swingCore) {
    super("WPCleaner");
    this.swingCore = swingCore;
  }

  protected void initialize() {
    setName(computeName());
    swingCore.windows().register(this);
    swingCore.image().setIconImage(this);
  }

  private String computeName() {
    return StringUtils.firstLetterLowerCase(
        StringUtils.removeSuffix(
            StringUtils.removePrefix(getClass().getSimpleName(), "Swing"), "Window"));
  }
}
