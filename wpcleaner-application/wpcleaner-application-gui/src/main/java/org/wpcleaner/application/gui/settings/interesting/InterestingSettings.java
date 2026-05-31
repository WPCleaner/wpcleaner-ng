package org.wpcleaner.application.gui.settings.interesting;

/*
 * SPDX-FileCopyrightText: © 2024 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.wpcleaner.api.settings.VersionedSettings;
import org.wpcleaner.api.wiki.definition.WikiDefinition;

public record InterestingSettings(int version, Map<String, InterestingByWikiSettings> wikis)
    implements VersionedSettings {

  public static final int LAST_VERSION = 1;

  public InterestingSettings() {
    this(0, Map.of());
  }

  public Optional<InterestingByWikiSettings> getByWikiSettings(final WikiDefinition wiki) {
    return Optional.ofNullable(wikis.get(wiki.code()));
  }

  public InterestingSettings withByWikiSettings(
      final WikiDefinition wiki, final InterestingByWikiSettings settings) {
    final Map<String, InterestingByWikiSettings> newSettings = new HashMap<>(wikis);
    newSettings.put(wiki.code(), settings);
    return new InterestingSettings(version, newSettings);
  }

  @Override
  public int lastVersion() {
    return LAST_VERSION;
  }
}
