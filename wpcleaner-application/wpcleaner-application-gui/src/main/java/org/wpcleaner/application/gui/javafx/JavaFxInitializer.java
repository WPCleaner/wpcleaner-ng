/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wpcleaner.application.gui.javafx;

import java.util.concurrent.atomic.AtomicBoolean;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wpcleaner.application.gui.core.desktop.DesktopService;

public final class JavaFxInitializer {

  private static final Logger LOGGER = LoggerFactory.getLogger(JavaFxInitializer.class);
  private static final AtomicBoolean STARTED = new AtomicBoolean(false);

  private JavaFxInitializer() {
    // Utility class
  }

  public static void initialize() {
    if (STARTED.compareAndSet(false, true)) {
      try {
        Platform.startup(() -> {});
      } catch (final IllegalStateException e) {
        LOGGER.debug("JavaFX platform already started", e);
      }
    }
  }

  public static void browse(final DesktopService desktopService, final String url) {
    final Thread thread = new Thread(() -> desktopService.browse(url));
    thread.setDaemon(true);
    thread.start();
  }
}
