package org.wpcleaner.launcher.shortcut;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class LinuxShortcutCreator implements ShortcutCreator {

  public static final String FILE_NAME = "wpcleaner-ng.desktop";

  private static final Logger LOGGER = Logger.getLogger(LinuxShortcutCreator.class.getName());

  private final Path executable;
  private final Path applicationsFolder;
  private final Optional<Path> desktopFolder;

  public LinuxShortcutCreator(
      final Path executable, final Path applicationsFolder, final Optional<Path> desktopFolder) {
    this.executable = executable;
    this.applicationsFolder = applicationsFolder;
    this.desktopFolder = desktopFolder;
  }

  @Override
  public void create() throws IOException {
    Files.createDirectories(applicationsFolder);
    write(applicationsFolder.resolve(FILE_NAME));
    if (desktopFolder.isPresent()) {
      final Path desktopFile = desktopFolder.get().resolve(FILE_NAME);
      write(desktopFile);
      trust(desktopFile);
    }
  }

  private void write(final Path file) throws IOException {
    Files.writeString(file, content());
    Files.setPosixFilePermissions(file, PosixFilePermissions.fromString("rwxr-xr-x"));
  }

  private String content() {
    return """
        [Desktop Entry]
        Type=Application
        Version=1.1
        Name=WPCleaner NG
        Comment=Maintenance tasks on MediaWiki wikis
        Exec=%s
        %sCategories=Utility;
        Terminal=false
        StartupNotify=false
        """
        .formatted(quote(executable), icon().map("Icon=%s\n"::formatted).orElse(""));
  }

  private Optional<Path> icon() {
    return Optional.ofNullable(executable.getParent())
        .map(Path::getParent)
        .map(image -> image.resolve("lib").resolve("%s.png".formatted(executable.getFileName())))
        .filter(Files::isRegularFile);
  }

  private static String quote(final Path path) {
    return "\"%s\"".formatted(path.toString().replaceAll("([\"`$\\\\])", "\\\\$1"));
  }

  private static void trust(final Path desktopFile) {
    try {
      SystemCommand.run(
          List.of("gio", "set", desktopFile.toString(), "metadata::trusted", "true"), Map.of());
    } catch (final IOException e) {
      LOGGER.log(Level.INFO, "Unable to mark %s as trusted".formatted(desktopFile), e);
    }
  }
}
