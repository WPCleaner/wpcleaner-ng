package org.wpcleaner.launcher.install;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.jspecify.annotations.Nullable;

@JsonIgnoreProperties(ignoreUnknown = true)
public record LauncherState(
    @Nullable String installedVersion,
    @Nullable String previousVersion,
    @Nullable String rejectedVersion,
    @Nullable String notifiedVersion,
    boolean shortcutProposed) {

  public static LauncherState empty() {
    return new LauncherState(null, null, null, null, false);
  }

  public LauncherState withInstalledVersion(final String version) {
    return new LauncherState(
        version, installedVersion, rejectedVersion, notifiedVersion, shortcutProposed);
  }

  public LauncherState withRollback() {
    return new LauncherState(
        previousVersion, null, installedVersion, notifiedVersion, shortcutProposed);
  }

  public LauncherState withNotifiedVersion(final String version) {
    return new LauncherState(
        installedVersion, previousVersion, rejectedVersion, version, shortcutProposed);
  }

  public LauncherState withShortcutProposed() {
    return new LauncherState(
        installedVersion, previousVersion, rejectedVersion, notifiedVersion, true);
  }
}
