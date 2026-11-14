package org.wpcleaner.launcher.install;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import org.wpcleaner.launcher.display.LauncherDisplay;
import org.wpcleaner.launcher.platform.LauncherDirectories;
import org.wpcleaner.launcher.release.ReleaseAsset;
import org.wpcleaner.launcher.release.ReleaseManifest;
import org.wpcleaner.launcher.release.ReleaseRepository;
import org.wpcleaner.launcher.runtime.JavaRuntime;
import org.wpcleaner.launcher.runtime.JavaRuntimeFinder;
import org.wpcleaner.launcher.start.BotStartScript;
import org.wpcleaner.launcher.utils.GT;

public final class BotInstaller {

  private static final Logger LOGGER = Logger.getLogger(BotInstaller.class.getName());

  private final LauncherDirectories directories;
  private final ReleaseRepository repository;
  private final JavaRuntimeFinder javaRuntimeFinder;
  private final LauncherDisplay display;
  private final ReleaseRequirements requirements;
  private final ReleaseStore releaseStore;
  private final BotStartScript startScript;

  public BotInstaller(
      final LauncherDirectories directories,
      final ReleaseRepository repository,
      final JavaRuntimeFinder javaRuntimeFinder,
      final LauncherDisplay display,
      final BotStartScript startScript) {
    this.directories = directories;
    this.repository = repository;
    this.javaRuntimeFinder = javaRuntimeFinder;
    this.display = display;
    this.requirements = new ReleaseRequirements(repository, javaRuntimeFinder);
    this.releaseStore = ReleaseStore.forBot(directories);
    this.startScript = startScript;
  }

  public boolean install() throws IOException {
    Files.createDirectories(directories.root());
    try (FileChannel channel =
            FileChannel.open(
                directories.lockFile(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        FileLock _ = channel.lock()) {
      final ReleaseManifest manifest = repository.latestManifest();
      final Optional<String> obstacle = requirements.obstacle(manifest);
      if (obstacle.isPresent()) {
        display.error(obstacle.get());
        return false;
      }
      final Optional<ReleaseAsset> asset = manifest.botAsset();
      final Optional<JavaRuntime> runtime = javaRuntimeFinder.find(manifest.javaVersion());
      if (asset.isEmpty() || runtime.isEmpty()) {
        display.error(GT._T("The bot is not available in WPCleaner NG %s.", manifest.version()));
        return false;
      }
      final InstalledRelease release = installRelease(manifest, asset.get());
      releaseStore.retainOnly(List.of(manifest.version()));
      final Path script = startScript.write(runtime.get(), release);
      LOGGER.info("Bot %s installed with %s".formatted(manifest.version(), script));
      display.inform(
          GT._T(
              "WPCleaner NG bot %s is installed.\nYou can start it with %s",
              manifest.version(), script));
      return true;
    }
  }

  private InstalledRelease installRelease(final ReleaseManifest manifest, final ReleaseAsset asset)
      throws IOException {
    final Optional<InstalledRelease> installed = releaseStore.find(manifest.version());
    if (installed.isPresent()) {
      return installed.get();
    }
    final String status = GT._T("Downloading WPCleaner NG bot %s", manifest.version());
    display.progress(status, 0, -1);
    try {
      return releaseStore.install(
          repository,
          manifest,
          asset,
          (current, total) -> display.progress(status, current, total));
    } finally {
      display.closeProgress();
    }
  }
}
