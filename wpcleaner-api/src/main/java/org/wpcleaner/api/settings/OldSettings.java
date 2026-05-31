package org.wpcleaner.api.settings;

/*
 * SPDX-FileCopyrightText: © 2024 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import jakarta.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.springframework.stereotype.Service;
import org.wpcleaner.api.utils.AutoCatch;

@Service
public final class OldSettings {

  @SuppressWarnings("SpellCheckingInspection")
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
    return Optional.ofNullable(rootPreferences).map(prefs -> prefs.get(name, null));
  }

  public Optional<Integer> getIntValue(final String name) {
    return Optional.ofNullable(rootPreferences)
        .map(prefs -> prefs.get(name, null))
        .map(Integer::valueOf);
  }

  public List<String> getStringListValue(final String name) {
    final Preferences preferences =
        Optional.ofNullable(rootPreferences).map(prefs -> node(prefs, name)).orElse(null);
    if (preferences == null) {
      return List.of();
    }
    return keys(preferences).stream().map(key -> preferences.get(key, null)).toList();
  }

  private List<String> keys(final Preferences root) {
    return AutoCatch.run(() -> Arrays.asList(root.keys()));
  }

  @Nullable
  private Preferences node(final Preferences root, final String name) {
    return AutoCatch.run(() -> root.nodeExists(name)) ? root.node(name) : null;
  }
}
