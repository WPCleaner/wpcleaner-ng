package org.wpcleaner.settings.local.general;

/*
 * SPDX-FileCopyrightText: © 2024 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.springframework.stereotype.Service;
import org.wpcleaner.settings.local.migration.OldSettings;

@Service
public class GeneralSettingsManager {

  private final GeneralSettings currentSettings;

  public GeneralSettingsManager(final OldSettings oldSettings) {
    this.currentSettings =
        GeneralSettingsImporter.convert(oldSettings).orElseGet(GeneralSettings::new);
  }

  public GeneralSettings getCurrentSettings() {
    return currentSettings;
  }
}
