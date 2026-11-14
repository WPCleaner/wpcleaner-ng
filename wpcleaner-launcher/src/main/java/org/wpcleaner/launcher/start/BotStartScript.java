package org.wpcleaner.launcher.start;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.wpcleaner.launcher.install.InstalledRelease;
import org.wpcleaner.launcher.platform.LauncherDirectories;
import org.wpcleaner.launcher.platform.OperatingSystem;
import org.wpcleaner.launcher.runtime.JavaRuntime;

public final class BotStartScript {

  public static final String SCRIPT_NAME = "wpcleaner-ng-bot";

  private static final String WINDOWS_JAVA = "java.exe";
  private static final String POSIX_ATTRIBUTES = "posix";

  private final LauncherDirectories directories;
  private final OperatingSystem operatingSystem;

  public BotStartScript(
      final LauncherDirectories directories, final OperatingSystem operatingSystem) {
    this.directories = directories;
    this.operatingSystem = operatingSystem;
  }

  public Path write(final JavaRuntime runtime, final InstalledRelease release) throws IOException {
    final List<String> command = new ArrayList<>();
    command.add(javaExecutable(runtime).toString());
    command.addAll(release.manifest().jvmArguments());
    command.add("-jar");
    command.add(release.jar().toString());
    final Path script = script();
    Files.createDirectories(script.getParent());
    if (operatingSystem == OperatingSystem.WINDOWS) {
      Files.writeString(
          script,
          "@echo off\r\n%s %%*\r\n"
              .formatted(
                  command.stream()
                      .map(BotStartScript::quoteForWindows)
                      .collect(Collectors.joining(" "))),
          StandardCharsets.UTF_8);
    } else {
      Files.writeString(
          script,
          "#!/bin/sh\nexec %s \"$@\"\n"
              .formatted(
                  command.stream()
                      .map(BotStartScript::quoteForShell)
                      .collect(Collectors.joining(" "))),
          StandardCharsets.UTF_8);
      if (script.getFileSystem().supportedFileAttributeViews().contains(POSIX_ATTRIBUTES)) {
        Files.setPosixFilePermissions(script, PosixFilePermissions.fromString("rwxr-xr-x"));
      }
    }
    return script;
  }

  public Path script() {
    return directories
        .bot()
        .resolve(SCRIPT_NAME + (operatingSystem == OperatingSystem.WINDOWS ? ".cmd" : ".sh"));
  }

  private Path javaExecutable(final JavaRuntime runtime) {
    if (operatingSystem != OperatingSystem.WINDOWS) {
      return runtime.executable();
    }
    final Path consoleJava = runtime.executable().resolveSibling(WINDOWS_JAVA);
    return Files.isRegularFile(consoleJava) ? consoleJava : runtime.executable();
  }

  private static String quoteForShell(final String value) {
    return "'" + value.replace("'", "'\\''") + "'";
  }

  private static String quoteForWindows(final String value) {
    return "\"" + value.replace("\"", "\"\"") + "\"";
  }
}
