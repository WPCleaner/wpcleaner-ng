package org.wpcleaner.application.gui.core.style;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Map;
import javafx.scene.paint.Color;
import org.springframework.stereotype.Service;

@Service
public class PageAnalysisStylePropertiesInitializer implements StylePropertiesInitializer {

  public static final String COMMENT = "pageanalysis.comment";

  @Override
  public Map<String, StyleProperties> getDefaultStyles() {
    return Map.ofEntries(
        Map.entry(
            COMMENT,
            StyleProperties.builder()
                .withForeground(true)
                .withForegroundColor(Color.GRAY)
                .build()));
  }
}
