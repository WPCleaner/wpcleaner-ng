/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wpcleaner.application.gui.javafx;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wpcleaner.application.gui.core.desktop.DesktopService;
import org.wpcleaner.application.gui.javafx.core.window.JavaFxWindow;

public final class JavaFxInitializer {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private static final AtomicBoolean STARTED = new AtomicBoolean(false);
  private static final CountDownLatch EXIT_LATCH = new CountDownLatch(1);

  private JavaFxInitializer() {
    // Utility class
  }

  public static void initialize() {
    if (STARTED.compareAndSet(false, true)) {
      try {
        Platform.startup(() -> Platform.setImplicitExit(false));
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

  public static void exit(final JavaFxWindowsRegistry windowsRegistry) {
    Platform.runLater(
        () -> {
          windowsRegistry.closeAllWindows();
          JavaFxWindow.shutdownExecutor();
          Platform.exit();
          EXIT_LATCH.countDown();
        });
  }

  public static void waitForExit() {
    try {
      EXIT_LATCH.await();
    } catch (final InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }
}
