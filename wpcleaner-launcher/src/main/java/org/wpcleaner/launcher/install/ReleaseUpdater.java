package org.wpcleaner.launcher.install;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.StandardOpenOption;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import org.wpcleaner.launcher.display.LauncherDisplay;
import org.wpcleaner.launcher.platform.LauncherDirectories;
import org.wpcleaner.launcher.platform.Platform;
import org.wpcleaner.launcher.release.ReleaseAsset;
import org.wpcleaner.launcher.release.ReleaseManifest;
import org.wpcleaner.launcher.release.ReleaseRepository;
import org.wpcleaner.launcher.runtime.JavaRuntimeFinder;
import org.wpcleaner.launcher.utils.GT;

public final class ReleaseUpdater {

  public static final int LAUNCHER_GENERATION = 1;

  private static final Logger LOGGER = Logger.getLogger(ReleaseUpdater.class.getName());

  private final LauncherDirectories directories;
  private final Platform platform;
  private final ReleaseRepository repository;
  private final JavaRuntimeFinder javaRuntimeFinder;
  private final LauncherDisplay display;
  private final StateStore stateStore;
  private final ReleaseStore releaseStore;

  public ReleaseUpdater(
      final LauncherDirectories directories,
      final Platform platform,
      final ReleaseRepository repository,
      final JavaRuntimeFinder javaRuntimeFinder,
      final LauncherDisplay display) {
    this.directories = directories;
    this.platform = platform;
    this.repository = repository;
    this.javaRuntimeFinder = javaRuntimeFinder;
    this.display = display;
    this.stateStore = new StateStore(directories);
    this.releaseStore = new ReleaseStore(directories);
  }

  public UpdateResult update() throws IOException {
    try (FileChannel channel =
            FileChannel.open(
                directories.lockFile(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        FileLock _ = channel.lock()) {
      final LauncherState state = stateStore.load();
      final Optional<InstalledRelease> installed = releaseStore.find(state.installedVersion());
      final ReleaseManifest manifest;
      try {
        manifest = repository.latestManifest();
      } catch (final IOException e) {
        LOGGER.log(Level.WARNING, "Unable to check the latest release", e);
        return new UpdateResult(
            installed, GT._T("The latest release could not be checked: %s", e.getMessage()));
      }
      if (!isUpdate(manifest, state, installed)) {
        return new UpdateResult(installed, null);
      }
      final Optional<ReleaseAsset> asset = manifest.asset(platform);
      final Optional<String> obstacle = obstacle(manifest, asset.isPresent());
      if (obstacle.isPresent() || asset.isEmpty()) {
        notifyObstacle(manifest, state, installed.isPresent(), obstacle.orElseThrow());
        return new UpdateResult(installed, obstacle.orElseThrow());
      }
      return install(manifest, asset.get(), state, installed);
    }
  }

  private static boolean isUpdate(
      final ReleaseManifest manifest,
      final LauncherState state,
      final Optional<InstalledRelease> installed) {
    return installed.isEmpty()
        || (manifest.releaseVersion().isNewerThan(installed.get().manifest().releaseVersion())
            && !manifest.version().equals(state.rejectedVersion()));
  }

  private Optional<String> obstacle(final ReleaseManifest manifest, final boolean assetAvailable) {
    if (manifest.launcherGeneration() > LAUNCHER_GENERATION) {
      return Optional.of(
          GT._T(
              "WPCleaner NG %s requires a more recent launcher.\nYou can download it from %s",
              manifest.version(), repository.releasesUrl()));
    }
    if (javaRuntimeFinder.find(manifest.javaVersion()).isEmpty()) {
      return Optional.of(
          GT._T(
              "WPCleaner NG %s requires Java %d or later.\n"
                  + "You can install it, or download the latest launcher from %s",
              manifest.version(), manifest.javaVersion(), repository.releasesUrl()));
    }
    if (!assetAvailable) {
      return Optional.of(
          GT._T(
              "WPCleaner NG %s is not available for your system (%s).",
              manifest.version(), platform.key()));
    }
    return Optional.empty();
  }

  private void notifyObstacle(
      final ReleaseManifest manifest,
      final LauncherState state,
      final boolean installed,
      final String obstacle)
      throws IOException {
    LOGGER.warning(obstacle);
    if (installed && !manifest.version().equals(state.notifiedVersion())) {
      display.inform(obstacle);
      stateStore.save(state.withNotifiedVersion(manifest.version()));
    }
  }

  private UpdateResult install(
      final ReleaseManifest manifest,
      final ReleaseAsset asset,
      final LauncherState state,
      final Optional<InstalledRelease> installed)
      throws IOException {
    final String status = GT._T("Downloading WPCleaner NG %s", manifest.version());
    display.progress(status, 0, -1);
    final InstalledRelease release;
    try {
      release =
          releaseStore.install(
              repository,
              manifest,
              asset,
              (current, total) -> display.progress(status, current, total));
    } catch (final IOException e) {
      LOGGER.log(Level.WARNING, "Unable to download version %s".formatted(manifest.version()), e);
      return new UpdateResult(installed, GT._T("The download failed: %s", e.getMessage()));
    } finally {
      display.closeProgress();
    }
    final LauncherState newState = state.withInstalledVersion(manifest.version());
    stateStore.save(newState);
    releaseStore.retainOnly(
        Stream.of(newState.installedVersion(), newState.previousVersion())
            .flatMap(Stream::ofNullable)
            .toList());
    LOGGER.info("Installed version %s".formatted(manifest.version()));
    return new UpdateResult(Optional.of(release), null);
  }
}
