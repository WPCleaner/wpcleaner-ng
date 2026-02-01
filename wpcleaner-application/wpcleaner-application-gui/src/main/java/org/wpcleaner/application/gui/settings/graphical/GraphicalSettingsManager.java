package org.wpcleaner.application.gui.settings.graphical;

/*
 * SPDX-FileCopyrightText: © 2024 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.springframework.stereotype.Service;
import org.wpcleaner.api.settings.migration.OldSettings;

@Service
public class GraphicalSettingsManager {

  private GraphicalSettings currentSettings;

  public GraphicalSettingsManager(final OldSettings oldSettings) {
    this.currentSettings =
        GraphicalSettingsImporter.convert(oldSettings).orElseGet(GraphicalSettings::new);
  }

  public GraphicalSettings getCurrentSettings() {
    return currentSettings;
  }

  public void updateSettings(final GraphicalSettings settings) {
    this.currentSettings = settings;
  }
}
