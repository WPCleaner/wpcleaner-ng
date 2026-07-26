package org.wpcleaner.application.gui.settings.recentchanges;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.springframework.stereotype.Service;
import org.wpcleaner.api.settings.SettingsPersistence;

@Service
public class RecentChangesSettingsManager {

  private final SettingsPersistence persistence;
  private RecentChangesSettings currentSettings;

  public RecentChangesSettingsManager(final SettingsPersistence persistence) {
    this.persistence = persistence;
    this.currentSettings =
        persistence.load(RecentChangesSettings.class).orElseGet(RecentChangesSettings::new);
  }

  public RecentChangesSettings getCurrentSettings() {
    return currentSettings;
  }

  public void updateSettings(final RecentChangesSettings settings) {
    this.currentSettings = settings;
    persistence.save(settings);
  }
}
