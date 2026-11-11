package org.wpcleaner.launcher.start;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.logging.Logger;
import org.wpcleaner.launcher.display.LauncherDisplay;
import org.wpcleaner.launcher.install.InstalledRelease;
import org.wpcleaner.launcher.install.LauncherState;
import org.wpcleaner.launcher.install.ReleaseStore;
import org.wpcleaner.launcher.install.StateStore;
import org.wpcleaner.launcher.platform.LauncherDirectories;
import org.wpcleaner.launcher.runtime.JavaRuntime;
import org.wpcleaner.launcher.runtime.JavaRuntimeFinder;
import org.wpcleaner.launcher.utils.GT;

public final class ReleaseStarter {

  public static final String APPLICATION_LOG = "wpcleaner-ng.log";

  private static final Logger LOGGER = Logger.getLogger(ReleaseStarter.class.getName());
  private static final Duration EARLY_EXIT = Duration.ofSeconds(10);

  private final LauncherDirectories directories;
  private final JavaRuntimeFinder javaRuntimeFinder;
  private final ProcessStarter processStarter;
  private final LauncherDisplay display;
  private final StateStore stateStore;
  private final ReleaseStore releaseStore;

  public ReleaseStarter(
      final LauncherDirectories directories,
      final JavaRuntimeFinder javaRuntimeFinder,
      final ProcessStarter processStarter,
      final LauncherDisplay display) {
    this.directories = directories;
    this.javaRuntimeFinder = javaRuntimeFinder;
    this.processStarter = processStarter;
    this.display = display;
    this.stateStore = new StateStore(directories);
    this.releaseStore = new ReleaseStore(directories);
  }

  public void start(final InstalledRelease release, final List<String> arguments)
      throws IOException, InterruptedException {
    final OptionalInt exitCode = startRelease(release, arguments);
    if (exitCode.isEmpty() || exitCode.getAsInt() == 0) {
      return;
    }
    final LauncherState state = stateStore.load();
    final Optional<InstalledRelease> previous = releaseStore.find(state.previousVersion());
    if (previous.isPresent()
        && display.confirm(
            GT._T(
                "WPCleaner NG %s stopped unexpectedly.\n"
                    + "Do you want to start the previous version (%s)?",
                release.manifest().version(), previous.get().manifest().version()))) {
      stateStore.save(state.withRollback());
      startRelease(previous.get(), arguments);
      return;
    }
    display.error(
        GT._T(
            "WPCleaner NG stopped unexpectedly (exit code %d).\nDetails are available in %s",
            exitCode.getAsInt(), applicationLog()));
  }

  private OptionalInt startRelease(final InstalledRelease release, final List<String> arguments)
      throws IOException, InterruptedException {
    final Optional<JavaRuntime> runtime = javaRuntimeFinder.find(release.manifest().javaVersion());
    if (runtime.isEmpty()) {
      display.error(
          GT._T(
              "WPCleaner NG %s requires Java %d or later.",
              release.manifest().version(), release.manifest().javaVersion()));
      return OptionalInt.empty();
    }
    final List<String> command = new ArrayList<>();
    command.add(runtime.get().executable().toString());
    command.addAll(release.manifest().jvmArguments());
    command.addAll(userJvmOptions());
    command.add("-jar");
    command.add(release.jar().toString());
    command.addAll(arguments);
    LOGGER.info("Starting %s".formatted(command));
    return processStarter.start(command, directories.root(), applicationLog()).exitCode(EARLY_EXIT);
  }

  private List<String> userJvmOptions() throws IOException {
    if (!Files.isRegularFile(directories.jvmOptionsFile())) {
      return List.of();
    }
    return Files.readAllLines(directories.jvmOptionsFile()).stream()
        .map(String::strip)
        .filter(line -> !line.isEmpty() && !line.startsWith("#"))
        .toList();
  }

  private Path applicationLog() {
    return directories.logs().resolve(APPLICATION_LOG);
  }
}
