package org.wpcleaner.launcher.shortcut;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.wpcleaner.launcher.platform.OperatingSystem;

public final class Shortcuts {

  public static final String EXECUTABLE_PROPERTY = "jpackage.app-path";

  private static final int MAC_BUNDLE_DEPTH = 3;

  private Shortcuts() {}

  public static Optional<ShortcutCreator> forOperatingSystem(
      final OperatingSystem operatingSystem) {
    return Optional.ofNullable(System.getProperty(EXECUTABLE_PROPERTY))
        .map(Paths::get)
        .flatMap(executable -> forExecutable(operatingSystem, executable));
  }

  private static Optional<ShortcutCreator> forExecutable(
      final OperatingSystem operatingSystem, final Path executable) {
    final Path userHome = Paths.get(System.getProperty("user.home"));
    return switch (operatingSystem) {
      case WINDOWS -> Optional.of(new WindowsShortcutCreator(executable));
      case LINUX ->
          Optional.of(
              new LinuxShortcutCreator(
                  executable,
                  userHome.resolve(".local").resolve("share").resolve("applications"),
                  linuxDesktopFolder(userHome)));
      case MACOS ->
          macApplicationBundle(executable)
              .map(bundle -> new MacShortcutCreator(bundle, userHome.resolve("Desktop")));
    };
  }

  private static Optional<Path> macApplicationBundle(final Path executable) {
    Path bundle = executable;
    for (int level = 0; level < MAC_BUNDLE_DEPTH && bundle != null; level++) {
      bundle = bundle.getParent();
    }
    return Optional.ofNullable(bundle);
  }

  private static Optional<Path> linuxDesktopFolder(final Path userHome) {
    return xdgDesktopFolder()
        .filter(folder -> !folder.equals(userHome))
        .or(() -> Optional.of(userHome.resolve("Desktop")))
        .filter(Files::isDirectory);
  }

  private static Optional<Path> xdgDesktopFolder() {
    try {
      return Optional.of(SystemCommand.run(List.of("xdg-user-dir", "DESKTOP"), Map.of()))
          .filter(folder -> !folder.isBlank())
          .map(Paths::get);
    } catch (final IOException e) {
      return Optional.empty();
    }
  }
}
