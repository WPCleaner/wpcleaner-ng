package org.wpcleaner.launcher.platform;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public record LauncherDirectories(Path root) {

  public static final String HOME_PROPERTY = "wpcleaner.launcher.home";

  private static final String WINDOWS_MACOS_FOLDER = "WPCleanerNG";
  private static final String LINUX_FOLDER = "wpcleaner-ng";

  public static LauncherDirectories forOperatingSystem(final OperatingSystem operatingSystem) {
    final String home = System.getProperty(HOME_PROPERTY);
    if (home != null) {
      return new LauncherDirectories(Paths.get(home));
    }
    final Path userHome = Paths.get(System.getProperty("user.home"));
    return new LauncherDirectories(
        switch (operatingSystem) {
          case WINDOWS ->
              environmentFolder("LOCALAPPDATA")
                  .orElseGet(() -> userHome.resolve("AppData").resolve("Local"))
                  .resolve(WINDOWS_MACOS_FOLDER);
          case MACOS ->
              userHome
                  .resolve("Library")
                  .resolve("Application Support")
                  .resolve(WINDOWS_MACOS_FOLDER);
          case LINUX ->
              environmentFolder("XDG_DATA_HOME")
                  .orElseGet(() -> userHome.resolve(".local").resolve("share"))
                  .resolve(LINUX_FOLDER);
        });
  }

  private static Optional<Path> environmentFolder(final String variable) {
    return Optional.ofNullable(System.getenv(variable))
        .filter(value -> !value.isBlank())
        .map(Paths::get);
  }

  public Path versions() {
    return root.resolve("versions");
  }

  public Path logs() {
    return root.resolve("logs");
  }

  public Path stateFile() {
    return root.resolve("launcher.json");
  }

  public Path lockFile() {
    return root.resolve("launcher.lock");
  }

  public Path jvmOptionsFile() {
    return root.resolve("jvm-options.txt");
  }
}
