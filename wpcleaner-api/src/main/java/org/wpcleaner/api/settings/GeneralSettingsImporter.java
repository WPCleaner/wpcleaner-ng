package org.wpcleaner.api.settings;

/*
 * SPDX-FileCopyrightText: © 2024 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Optional;
import org.jspecify.annotations.Nullable;

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
