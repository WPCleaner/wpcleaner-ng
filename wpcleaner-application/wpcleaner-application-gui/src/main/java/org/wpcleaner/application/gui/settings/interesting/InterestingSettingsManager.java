package org.wpcleaner.application.gui.settings.interesting;

/*
 * SPDX-FileCopyrightText: © 2024 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.springframework.stereotype.Service;
import org.wpcleaner.api.settings.OldSettings;
import org.wpcleaner.api.settings.SettingsPersistence;

@Service
public class InterestingSettingsManager {

  private final InterestingSettings currentSettings;

  public InterestingSettingsManager(
      final SettingsPersistence persistence, final OldSettings oldSettings) {
    this.currentSettings =
        persistence
            .load(InterestingSettings.class)
            .or(() -> InterestingSettingsImporter.convert(oldSettings))
            .orElseGet(InterestingSettings::new);
  }

  public InterestingSettings getCurrentSettings() {
    return currentSettings;
  }
}
