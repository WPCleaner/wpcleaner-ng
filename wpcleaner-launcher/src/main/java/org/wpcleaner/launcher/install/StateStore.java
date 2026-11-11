package org.wpcleaner.launcher.install;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.io.IOException;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.wpcleaner.launcher.platform.LauncherDirectories;
import org.wpcleaner.launcher.utils.JsonFiles;

public final class StateStore {

  private static final Logger LOGGER = Logger.getLogger(StateStore.class.getName());

  private final LauncherDirectories directories;

  public StateStore(final LauncherDirectories directories) {
    this.directories = directories;
  }

  public LauncherState load() {
    if (!Files.isRegularFile(directories.stateFile())) {
      return LauncherState.empty();
    }
    try {
      return JsonFiles.read(directories.stateFile(), LauncherState.class);
    } catch (final IOException e) {
      LOGGER.log(Level.WARNING, "Unreadable launcher state, starting from an empty one", e);
      return LauncherState.empty();
    }
  }

  public void save(final LauncherState state) throws IOException {
    JsonFiles.write(directories.stateFile(), state);
  }
}
