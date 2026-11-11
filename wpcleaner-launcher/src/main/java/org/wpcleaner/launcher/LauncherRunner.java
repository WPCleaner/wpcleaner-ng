package org.wpcleaner.launcher;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.wpcleaner.launcher.display.LauncherDisplay;
import org.wpcleaner.launcher.install.LauncherState;
import org.wpcleaner.launcher.install.ReleaseUpdater;
import org.wpcleaner.launcher.install.StateStore;
import org.wpcleaner.launcher.install.UpdateResult;
import org.wpcleaner.launcher.platform.LauncherDirectories;
import org.wpcleaner.launcher.shortcut.ShortcutCreator;
import org.wpcleaner.launcher.start.ReleaseStarter;
import org.wpcleaner.launcher.utils.GT;

public final class LauncherRunner {

  private static final Logger LOGGER = Logger.getLogger(LauncherRunner.class.getName());

  private final LauncherDirectories directories;
  private final ReleaseUpdater releaseUpdater;
  private final ReleaseStarter releaseStarter;
  private final LauncherDisplay display;
  private final Optional<ShortcutCreator> shortcutCreator;
  private final StateStore stateStore;

  public LauncherRunner(
      final LauncherDirectories directories,
      final ReleaseUpdater releaseUpdater,
      final ReleaseStarter releaseStarter,
      final LauncherDisplay display,
      final Optional<ShortcutCreator> shortcutCreator) {
    this.directories = directories;
    this.releaseUpdater = releaseUpdater;
    this.releaseStarter = releaseStarter;
    this.display = display;
    this.shortcutCreator = shortcutCreator;
    this.stateStore = new StateStore(directories);
  }

  public void run(final List<String> arguments) throws IOException, InterruptedException {
    Files.createDirectories(directories.logs());
    UpdateResult result = releaseUpdater.update();
    while (result.release().isEmpty()) {
      if (!display.confirm(
          GT._T(
              "WPCleaner NG could not be installed.\n%s\n\nDo you want to try again?",
              result.problem()))) {
        return;
      }
      result = releaseUpdater.update();
    }
    proposeShortcut();
    releaseStarter.start(result.release().get(), arguments);
  }

  public static void createShortcut(
      final ShortcutCreator shortcutCreator, final LauncherDisplay display) {
    try {
      shortcutCreator.create();
    } catch (final IOException e) {
      LOGGER.log(Level.WARNING, "Unable to create the shortcut", e);
      display.error(GT._T("The shortcut could not be created: %s", e.getMessage()));
    }
  }

  private void proposeShortcut() throws IOException {
    final LauncherState state = stateStore.load();
    if (shortcutCreator.isEmpty() || state.shortcutProposed()) {
      return;
    }
    stateStore.save(state.withShortcutProposed());
    if (display.confirm(GT._T("Do you want to add a shortcut to WPCleaner NG on your desktop?"))) {
      createShortcut(shortcutCreator.get(), display);
    }
  }
}
