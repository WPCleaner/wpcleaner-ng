package org.wpcleaner.launcher.shortcut;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;

@DisabledOnOs(OS.WINDOWS)
class LinuxShortcutCreatorTest {

  @TempDir private Path temporaryFolder;

  @DisplayName("Create the menu entry and the desktop shortcut")
  @Test
  void createMenuEntryAndDesktopShortcut() throws IOException {
    // GIVEN
    final Path image = temporaryFolder.resolve("My apps").resolve("WPCleanerNG");
    final Path executable = image.resolve("bin").resolve("WPCleanerNG");
    final Path icon = image.resolve("lib").resolve("WPCleanerNG.png");
    Files.createDirectories(icon.getParent());
    Files.createFile(icon);
    final Path applications = temporaryFolder.resolve("applications");
    final Path desktop = Files.createDirectory(temporaryFolder.resolve("Desktop"));

    // WHEN
    new LinuxShortcutCreator(executable, applications, Optional.of(desktop)).create();

    // THEN
    final Path menuEntry = applications.resolve(LinuxShortcutCreator.FILE_NAME);
    Assertions.assertThat(menuEntry)
        .content()
        .contains("Exec=\"%s\"\n".formatted(executable))
        .contains("Icon=%s\n".formatted(icon))
        .contains("Name=WPCleaner NG\n");
    Assertions.assertThat(desktop.resolve(LinuxShortcutCreator.FILE_NAME))
        .isExecutable()
        .hasSameTextualContentAs(menuEntry);
  }
}
