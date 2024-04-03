package org.wpcleaner.settings.local.migration;

/*
 * SPDX-FileCopyrightText: © 2024 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import jakarta.annotation.Nullable;
import java.util.Optional;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.springframework.stereotype.Service;

@Service
public final class OldSettings {

  private static final String ROOT_PATH = "org/wikipediacleaner";

  @Nullable private final Preferences rootPreferences;

  OldSettings() {
    this.rootPreferences = initRootPreferences();
  }

  @Nullable
  private static Preferences initRootPreferences() {
    try {
      if (!Preferences.userRoot().nodeExists(ROOT_PATH)) {
        return null;
      }
      return Preferences.userRoot().node(ROOT_PATH);
    } catch (BackingStoreException e) {
      return null;
    }
  }

  public boolean isMissing() {
    return rootPreferences == null;
  }

  public Optional<String> getStringValue(final String name) {
    return Optional.ofNullable(rootPreferences).map(preferences -> preferences.get(name, null));
  }

  public Optional<Integer> getIntValue(final String name) {
    return Optional.ofNullable(rootPreferences)
        .map(preferences -> preferences.get(name, null))
        .map(Integer::valueOf);
  }
}
