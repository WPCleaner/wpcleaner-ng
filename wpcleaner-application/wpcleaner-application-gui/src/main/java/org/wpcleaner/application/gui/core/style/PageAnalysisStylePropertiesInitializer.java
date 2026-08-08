package org.wpcleaner.application.gui.core.style;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.awt.Color;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class PageAnalysisStylePropertiesInitializer implements StylePropertiesInitializer {

  public static final String COMMENT = "pageanalysis.comment";
  public static final String TAG = "pageanalysis.tag";

  @Override
  public Map<String, StyleProperties> getDefaultStyles() {
    return Map.ofEntries(
        Map.entry(
            COMMENT,
            StyleProperties.builder().withForeground(true).withForegroundColor(Color.GRAY).build()),
        Map.entry(
            TAG,
            StyleProperties.builder()
                .withForeground(true)
                .withForegroundColor(new Color(154, 0, 154))
                .build()));
  }
}
