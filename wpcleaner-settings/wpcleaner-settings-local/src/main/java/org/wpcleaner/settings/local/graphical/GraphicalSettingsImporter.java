package org.wpcleaner.settings.local.graphical;

/*
 * SPDX-FileCopyrightText: © 2024 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import jakarta.annotation.Nullable;
import java.util.Optional;
import org.wpcleaner.settings.local.migration.OldSettings;

final class GraphicalSettingsImporter {

  private GraphicalSettingsImporter() {
    // Do nothing: utility class
  }

  static Optional<GraphicalSettings> convert(final OldSettings oldSettings) {
    if (oldSettings.isMissing()) {
      return Optional.empty();
    }
    return Optional.of(
        new GraphicalSettings(GraphicalSettings.LAST_VERSION, convertLookAndFeel(oldSettings)));
  }

  private static LookAndFeelSettings convertLookAndFeel(final OldSettings oldSettings) {
    final LookAndFeelType type =
        convertLookAndFeelType(oldSettings.getIntValue("LookAndFeelType").orElse(null));
    final String name = oldSettings.getStringValue("LookAndFeelName").orElse(null);
    return new LookAndFeelSettings(type, name);
  }

  @Nullable
  private static LookAndFeelType convertLookAndFeelType(@Nullable final Integer type) {
    if (type == null) {
      return null;
    }
    return switch (type) {
      case 0 -> LookAndFeelType.WPCLEANER;
      case 1 -> LookAndFeelType.SYSTEM;
      case 2 -> LookAndFeelType.USER;
      default -> null;
    };
  }
}
