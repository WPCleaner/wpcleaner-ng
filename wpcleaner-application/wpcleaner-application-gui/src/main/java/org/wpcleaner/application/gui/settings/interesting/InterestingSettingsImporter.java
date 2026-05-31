package org.wpcleaner.application.gui.settings.interesting;

/*
 * SPDX-FileCopyrightText: © 2024 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.wpcleaner.api.settings.OldSettings;

final class InterestingSettingsImporter {

  private InterestingSettingsImporter() {
    // Do nothing: utility class
  }

  static Optional<InterestingSettings> convert(final OldSettings oldSettings) {
    if (oldSettings.isMissing()) {
      return Optional.empty();
    }
    return Optional.of(
        new InterestingSettings(
            InterestingSettings.LAST_VERSION, convertPreferredWiki(oldSettings)));
  }

  private static Map<String, InterestingByWikiSettings> convertPreferredWiki(
      final OldSettings oldSettings) {
    final Map<String, InterestingByWikiSettings> result = new HashMap<>();
    oldSettings
        .getStringValue("Wikipedia")
        .ifPresent(
            code ->
                result.put(
                    code,
                    new InterestingByWikiSettings(
                        oldSettings.getStringListValue("InterestingPages"))));
    return result;
  }
}
