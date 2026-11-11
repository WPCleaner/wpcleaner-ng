package org.wpcleaner.launcher.release;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.launcher.platform.Platform;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ReleaseManifest(
    String version,
    int javaVersion,
    int launcherGeneration,
    List<String> jvmArguments,
    Map<String, ReleaseAsset> assets) {

  public ReleaseManifest(
      final String version,
      final int javaVersion,
      final int launcherGeneration,
      @Nullable final List<String> jvmArguments,
      @Nullable final Map<String, ReleaseAsset> assets) {
    this.version = ReleaseVersion.parse(version).toString();
    this.javaVersion = javaVersion;
    this.launcherGeneration = launcherGeneration;
    this.jvmArguments = jvmArguments == null ? List.of() : List.copyOf(jvmArguments);
    this.assets = assets == null ? Map.of() : Map.copyOf(assets);
  }

  @JsonIgnore
  public ReleaseVersion releaseVersion() {
    return ReleaseVersion.parse(version);
  }

  public Optional<ReleaseAsset> asset(final Platform platform) {
    return Optional.ofNullable(assets.get(platform.key()));
  }
}
