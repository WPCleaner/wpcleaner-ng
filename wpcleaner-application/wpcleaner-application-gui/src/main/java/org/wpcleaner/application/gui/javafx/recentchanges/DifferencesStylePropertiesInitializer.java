package org.wpcleaner.application.gui.javafx.recentchanges;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Map;
import javafx.scene.paint.Color;
import org.springframework.stereotype.Service;
import org.wpcleaner.application.gui.core.style.StyleProperties;
import org.wpcleaner.application.gui.core.style.StylePropertiesInitializer;

@Service
public class DifferencesStylePropertiesInitializer implements StylePropertiesInitializer {

  public static final String REMOVED = "differences.removed";
  public static final String ADDED = "differences.added";

  @Override
  public Map<String, StyleProperties> getDefaultStyles() {
    return Map.ofEntries(
        Map.entry(
            REMOVED,
            StyleProperties.builder()
                .withBackground(true)
                .withBackgroundColor(Color.rgb(0xFF, 0xCC, 0xCC))
                .build()),
        Map.entry(
            ADDED,
            StyleProperties.builder()
                .withBackground(true)
                .withBackgroundColor(Color.rgb(0xCC, 0xFF, 0xCC))
                .build()));
  }
}
