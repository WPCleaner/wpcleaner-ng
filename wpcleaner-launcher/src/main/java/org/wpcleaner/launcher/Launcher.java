package org.wpcleaner.launcher;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import org.wpcleaner.launcher.display.LauncherDisplay;
import org.wpcleaner.launcher.display.SwingDisplay;
import org.wpcleaner.launcher.install.ReleaseUpdater;
import org.wpcleaner.launcher.platform.LauncherDirectories;
import org.wpcleaner.launcher.platform.Platform;
import org.wpcleaner.launcher.release.ReleaseRepository;
import org.wpcleaner.launcher.runtime.JavaRuntimeFinder;
import org.wpcleaner.launcher.shortcut.ShortcutCreator;
import org.wpcleaner.launcher.shortcut.Shortcuts;
import org.wpcleaner.launcher.start.ReleaseStarter;
import org.wpcleaner.launcher.start.SystemProcessStarter;
import org.wpcleaner.launcher.utils.GT;

public final class Launcher {

  public static final String CREATE_SHORTCUT_OPTION = "--create-shortcut";

  private static final Logger LOGGER = Logger.getLogger(Launcher.class.getName());
  private static final int LOG_SIZE = 1_000_000;
  private static final int LOG_COUNT = 3;

  private Launcher() {}

  @SuppressWarnings("PMD.AvoidCatchingGenericException")
  static void main(final String... args) {
    System.setProperty("java.net.useSystemProxies", "true");
    final Platform platform = Platform.current();
    final LauncherDirectories directories =
        LauncherDirectories.forOperatingSystem(platform.operatingSystem());
    final LauncherDisplay display = new SwingDisplay();
    try {
      configureLogging(directories);
      final Optional<ShortcutCreator> shortcutCreator =
          Shortcuts.forOperatingSystem(platform.operatingSystem());
      if (List.of(args).contains(CREATE_SHORTCUT_OPTION)) {
        createShortcut(shortcutCreator, display);
      } else {
        final JavaRuntimeFinder javaRuntimeFinder =
            JavaRuntimeFinder.forSystem(platform.operatingSystem());
        new LauncherRunner(
                directories,
                new ReleaseUpdater(
                    directories,
                    platform,
                    ReleaseRepository.forSystem(),
                    javaRuntimeFinder,
                    display),
                new ReleaseStarter(
                    directories, javaRuntimeFinder, new SystemProcessStarter(), display),
                display,
                shortcutCreator)
            .run(List.of(args));
      }
    } catch (final InterruptedException e) {
      Thread.currentThread().interrupt();
    } catch (final IOException | RuntimeException e) {
      LOGGER.log(Level.SEVERE, "Launcher failure", e);
      display.error(GT._T("The launcher failed: %s", e));
    }
    System.exit(0);
  }

  private static void createShortcut(
      final Optional<ShortcutCreator> shortcutCreator, final LauncherDisplay display) {
    if (shortcutCreator.isEmpty()) {
      display.error(GT._T("Shortcuts can only be created by the installed launcher."));
      return;
    }
    LauncherRunner.createShortcut(shortcutCreator.get(), display);
  }

  private static void configureLogging(final LauncherDirectories directories) throws IOException {
    Files.createDirectories(directories.logs());
    final FileHandler handler =
        new FileHandler(
            directories.logs().resolve("launcher-%g.log").toString(), LOG_SIZE, LOG_COUNT, true);
    handler.setFormatter(new SimpleFormatter());
    Logger.getLogger("").addHandler(handler);
    LOGGER.info(
        "Launcher generation %d (%s) on %s %s (%s), Java %s in %s"
            .formatted(
                ReleaseUpdater.LAUNCHER_GENERATION,
                System.getProperty("jpackage.app-version", "development"),
                System.getProperty("os.name"),
                System.getProperty("os.version"),
                System.getProperty("os.arch"),
                Runtime.version(),
                System.getProperty("java.home")));
  }
}
