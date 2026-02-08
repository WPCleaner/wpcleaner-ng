package org.wpcleaner.application.gui.settings.windows;

/*
 * SPDX-FileCopyrightText: © 2024 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.springframework.stereotype.Service;
import org.wpcleaner.api.settings.SettingsPersistence;

@Service
public class WindowsSettingsManager {

  private final SettingsPersistence persistence;
  private WindowsSettings currentSettings;

  public WindowsSettingsManager(final SettingsPersistence persistence) {
    this.persistence = persistence;
    this.currentSettings = persistence.load(WindowsSettings.class).orElseGet(WindowsSettings::new);
  }

  public WindowsSettings getCurrentSettings() {
    return currentSettings;
  }

  public void updateSettings(final WindowsSettings settings) {
    this.currentSettings = settings;
    persistence.save(settings);
  }
}
