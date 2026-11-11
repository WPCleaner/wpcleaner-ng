package org.wpcleaner.launcher.install;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.launcher.platform.LauncherDirectories;
import org.wpcleaner.launcher.release.DownloadProgress;
import org.wpcleaner.launcher.release.ReleaseAsset;
import org.wpcleaner.launcher.release.ReleaseManifest;
import org.wpcleaner.launcher.release.ReleaseRepository;
import org.wpcleaner.launcher.utils.JsonFiles;

public final class ReleaseStore {

  private static final Logger LOGGER = Logger.getLogger(ReleaseStore.class.getName());
  private static final String JAR_NAME = "wpcleaner-ng.jar";

  private final LauncherDirectories directories;

  public ReleaseStore(final LauncherDirectories directories) {
    this.directories = directories;
  }

  public Optional<InstalledRelease> find(@Nullable final String version) {
    if (version == null) {
      return Optional.empty();
    }
    final Path jar = jar(version);
    final Path manifest = manifest(version);
    if (!Files.isRegularFile(jar) || !Files.isRegularFile(manifest)) {
      return Optional.empty();
    }
    try {
      return Optional.of(
          new InstalledRelease(JsonFiles.read(manifest, ReleaseManifest.class), jar));
    } catch (final IOException e) {
      LOGGER.log(Level.WARNING, "Unreadable manifest for version %s".formatted(version), e);
      return Optional.empty();
    }
  }

  public InstalledRelease install(
      final ReleaseRepository repository,
      final ReleaseManifest manifest,
      final ReleaseAsset asset,
      final DownloadProgress progress)
      throws IOException {
    final Path jar = jar(manifest.version());
    final Path download = jar.resolveSibling("%s.part".formatted(JAR_NAME));
    Files.createDirectories(jar.getParent());
    repository.download(manifest, asset, download, progress);
    Files.move(download, jar, StandardCopyOption.REPLACE_EXISTING);
    JsonFiles.write(manifest(manifest.version()), manifest);
    return new InstalledRelease(manifest, jar);
  }

  public void retainOnly(final Collection<String> versions) {
    for (final Path folder : versionFolders()) {
      if (!versions.contains(folder.getFileName().toString())) {
        delete(folder);
      }
    }
  }

  private List<Path> versionFolders() {
    if (!Files.isDirectory(directories.versions())) {
      return List.of();
    }
    try (Stream<Path> folders = Files.list(directories.versions())) {
      return folders.filter(Files::isDirectory).toList();
    } catch (final IOException e) {
      LOGGER.log(Level.WARNING, "Unable to list installed versions", e);
      return List.of();
    }
  }

  private static void delete(final Path folder) {
    try (Stream<Path> paths = Files.walk(folder)) {
      for (final Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
        Files.delete(path);
      }
    } catch (final IOException e) {
      LOGGER.log(Level.INFO, "Unable to delete %s, it may still be in use".formatted(folder), e);
    }
  }

  private Path jar(final String version) {
    return directories.versions().resolve(version).resolve(JAR_NAME);
  }

  private Path manifest(final String version) {
    return directories.versions().resolve(version).resolve(ReleaseRepository.MANIFEST_NAME);
  }
}
