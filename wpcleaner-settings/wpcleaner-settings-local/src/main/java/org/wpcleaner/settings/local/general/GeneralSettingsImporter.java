package org.wpcleaner.settings.local.general;

/*
 * SPDX-FileCopyrightText: © 2024 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import jakarta.annotation.Nullable;
import java.util.Optional;
import org.wpcleaner.settings.local.migration.OldSettings;

final class GeneralSettingsImporter {

  private GeneralSettingsImporter() {
    // Do nothing: utility class
  }

  static Optional<GeneralSettings> convert(final OldSettings oldSettings) {
    if (oldSettings.isMissing()) {
      return Optional.empty();
    }
    return Optional.of(
        new GeneralSettings(GeneralSettings.LAST_VERSION, convertPreferredWiki(oldSettings)));
  }

  @Nullable
  private static String convertPreferredWiki(final OldSettings oldSettings) {
    return oldSettings.getStringValue("Wikipedia").orElse(null);
  }
}
