package org.wpcleaner.settings.local.graphical;

/*
 * SPDX-FileCopyrightText: © 2024 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.springframework.stereotype.Service;
import org.wpcleaner.settings.local.migration.OldSettings;

@Service
public class GraphicalSettingsManager {

  private final GraphicalSettings currentSettings;

  public GraphicalSettingsManager(final OldSettings oldSettings) {
    this.currentSettings =
        GraphicalSettingsImporter.convert(oldSettings).orElseGet(GraphicalSettings::new);
  }

  public GraphicalSettings getCurrentSettings() {
    return currentSettings;
  }
}
