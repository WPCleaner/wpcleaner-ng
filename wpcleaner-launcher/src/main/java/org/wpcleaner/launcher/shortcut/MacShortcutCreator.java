package org.wpcleaner.launcher.shortcut;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class MacShortcutCreator implements ShortcutCreator {

  private final Path applicationBundle;
  private final Path desktopFolder;

  public MacShortcutCreator(final Path applicationBundle, final Path desktopFolder) {
    this.applicationBundle = applicationBundle;
    this.desktopFolder = desktopFolder;
  }

  @Override
  public void create() throws IOException {
    final Path link = desktopFolder.resolve(applicationBundle.getFileName());
    if (Files.isSymbolicLink(link)) {
      Files.delete(link);
    }
    Files.createSymbolicLink(link, applicationBundle);
  }
}
