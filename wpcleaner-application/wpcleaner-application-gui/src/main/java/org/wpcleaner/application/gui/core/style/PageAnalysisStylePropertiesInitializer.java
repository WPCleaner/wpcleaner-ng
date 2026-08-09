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

  public static final String CATEGORY = "pageAnalysis.category";
  public static final String COMMENT = "pageAnalysis.comment";
  public static final String INTERNAL_LINK = "pageAnalysis.internalLink";
  public static final String TAG = "pageAnalysis.tag";

  @Override
  public Map<String, StyleProperties> getDefaultStyles() {
    return Map.ofEntries(
        Map.entry(
            CATEGORY,
            StyleProperties.builder()
                .withBackground(true)
                .withBackgroundColor(new Color(153, 255, 153))
                .build()),
        Map.entry(
            COMMENT,
            StyleProperties.builder()
                .withForeground(true)
                .withForegroundColor(Color.GRAY)
                .withItalic(true)
                .build()),
        Map.entry(
            INTERNAL_LINK,
            StyleProperties.builder()
                .withBackground(true)
                .withBackgroundColor(new Color(204, 255, 255))
                .build()),
        Map.entry(
            TAG,
            StyleProperties.builder()
                .withForeground(true)
                .withForegroundColor(new Color(96, 0, 96))
                .withItalic(true)
                .build()));
  }
}
