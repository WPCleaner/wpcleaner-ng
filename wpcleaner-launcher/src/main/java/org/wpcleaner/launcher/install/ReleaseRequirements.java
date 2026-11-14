package org.wpcleaner.launcher.install;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Optional;
import org.wpcleaner.launcher.release.ReleaseManifest;
import org.wpcleaner.launcher.release.ReleaseRepository;
import org.wpcleaner.launcher.runtime.JavaRuntimeFinder;
import org.wpcleaner.launcher.utils.GT;

public final class ReleaseRequirements {

  public static final int LAUNCHER_GENERATION = 1;

  private final ReleaseRepository repository;
  private final JavaRuntimeFinder javaRuntimeFinder;

  public ReleaseRequirements(
      final ReleaseRepository repository, final JavaRuntimeFinder javaRuntimeFinder) {
    this.repository = repository;
    this.javaRuntimeFinder = javaRuntimeFinder;
  }

  public Optional<String> obstacle(final ReleaseManifest manifest) {
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
    return Optional.empty();
  }
}
