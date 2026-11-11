package org.wpcleaner.launcher.shortcut;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public final class WindowsShortcutCreator implements ShortcutCreator {

  private static final String TARGET_VARIABLE = "WPCLEANER_LAUNCHER";
  private static final String SCRIPT =
      """
      $shell = New-Object -ComObject WScript.Shell
      foreach ($folder in 'Desktop', 'Programs') {
        $link = Join-Path ([Environment]::GetFolderPath($folder)) 'WPCleaner NG.lnk'
        $shortcut = $shell.CreateShortcut($link)
        $shortcut.TargetPath = $env:%1$s
        $shortcut.WorkingDirectory = Split-Path $env:%1$s
        $shortcut.Save()
      }
      """
          .formatted(TARGET_VARIABLE);

  private final Path executable;

  public WindowsShortcutCreator(final Path executable) {
    this.executable = executable;
  }

  @Override
  public void create() throws IOException {
    SystemCommand.run(
        List.of("powershell.exe", "-NoProfile", "-NonInteractive", "-Command", SCRIPT),
        Map.of(TARGET_VARIABLE, executable.toString()));
  }
}
