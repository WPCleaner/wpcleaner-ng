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
  public static final String CHECK_ERROR = "pageAnalysis.checkError";
  public static final String COMMENT = "pageAnalysis.comment";
  public static final String EXTERNAL_LINK = "pageAnalysis.externalLink";
  public static final String IMAGE = "pageAnalysis.image";
  public static final String INTERNAL_LINK = "pageAnalysis.internalLink";
  public static final String INTERWIKI_LINK = "pageAnalysis.interwikiLink";
  public static final String LANGUAGE_LINK = "pageAnalysis.languageLink";
  public static final String LIST_ITEM = "pageAnalysis.listItem";
  public static final String PROGRAMMING = "pageAnalysis.programming";
  public static final String REFERENCE = "pageAnalysis.reference";
  public static final String TABLE = "pageAnalysis.table";
  public static final String TAG = "pageAnalysis.tag";
  public static final String TEMPLATE = "pageAnalysis.template";
  public static final String TITLE = "pageAnalysis.title";

  @Override
  public Map<String, StyleProperties> getDefaultStyles() {
    return Map.ofEntries(
        Map.entry(
            CATEGORY,
            StyleProperties.builder()
                .withBackground(true)
                .withBackgroundColor(new Color(153, 255, 153))
                .withItalic(true)
                .build()),
        Map.entry(
            CHECK_ERROR,
            StyleProperties.builder()
                .withForeground(true)
                .withForegroundColor(Color.WHITE)
                .withBackground(true)
                .withBackgroundColor(Color.RED)
                .build()),
        Map.entry(
            COMMENT,
            StyleProperties.builder()
                .withForeground(true)
                .withForegroundColor(Color.GRAY)
                .withItalic(true)
                .build()),
        Map.entry(
            EXTERNAL_LINK,
            StyleProperties.builder()
                .withBackground(true)
                .withBackgroundColor(new Color(214, 255, 255))
                .withItalic(true)
                .build()),
        Map.entry(
            IMAGE,
            StyleProperties.builder()
                .withBackground(true)
                .withBackgroundColor(new Color(153, 230, 230))
                .build()),
        Map.entry(
            INTERNAL_LINK,
            StyleProperties.builder()
                .withBackground(true)
                .withBackgroundColor(new Color(204, 255, 255))
                .build()),
        Map.entry(
            INTERWIKI_LINK,
            StyleProperties.builder()
                .withBackground(true)
                .withBackgroundColor(new Color(214, 255, 255))
                .build()),
        Map.entry(
            LANGUAGE_LINK,
            StyleProperties.builder()
                .withBackground(true)
                .withBackgroundColor(new Color(214, 214, 214))
                .withItalic(true)
                .build()),
        Map.entry(
            LIST_ITEM,
            StyleProperties.builder().withForeground(true).withForegroundColor(Color.BLUE).build()),
        Map.entry(
            PROGRAMMING,
            StyleProperties.builder()
                .withBackground(true)
                .withBackgroundColor(Color.ORANGE)
                .build()),
        Map.entry(
            REFERENCE,
            StyleProperties.builder()
                .withBackground(true)
                .withBackgroundColor(new Color(214, 214, 214))
                .build()),
        Map.entry(
            TABLE,
            StyleProperties.builder().withBackground(true).withBackgroundColor(Color.PINK).build()),
        Map.entry(
            TAG,
            StyleProperties.builder()
                .withForeground(true)
                .withForegroundColor(new Color(96, 0, 96))
                .withItalic(true)
                .build()),
        Map.entry(
            TEMPLATE,
            StyleProperties.builder()
                .withBackground(true)
                .withBackgroundColor(new Color(255, 255, 171))
                .build()),
        Map.entry(
            TITLE,
            StyleProperties.builder()
                .withBackground(true)
                .withBackgroundColor(new Color(255, 204, 204))
                .build()));
  }
}
