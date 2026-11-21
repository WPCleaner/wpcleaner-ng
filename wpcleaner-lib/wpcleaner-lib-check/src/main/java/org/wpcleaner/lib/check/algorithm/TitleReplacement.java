package org.wpcleaner.lib.check.algorithm;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.wpcleaner.api.analysis.PageAnalysis;
import org.wpcleaner.api.analysis.title.TitleElement;
import org.wpcleaner.lib.check.Replacement;

public final class TitleReplacement {

  private static final char EQUAL = '=';

  private TitleReplacement() {}

  public static Replacement withLevel(
      final PageAnalysis analysis, final TitleElement title, final int level) {
    final String marker = String.valueOf(EQUAL).repeat(level);
    final String text = title.title().isEmpty() ? " " : " " + title.title() + " ";
    return new Replacement(marker + text + marker, isReliable(analysis, title));
  }

  public static boolean isReliable(final PageAnalysis analysis, final TitleElement title) {
    return isCoherent(analysis.getText(), title)
        && !title.isInsideOne(analysis.getTemplates())
        && analysis.getIncludeAreas().isEmpty();
  }

  private static boolean isCoherent(final String text, final TitleElement title) {
    final int length = title.end() - title.begin();
    int firstLevel = 0;
    while (firstLevel < length && text.charAt(title.begin() + firstLevel) == EQUAL) {
      firstLevel++;
    }
    int secondLevel = 0;
    while (secondLevel < length && text.charAt(title.end() - secondLevel - 1) == EQUAL) {
      secondLevel++;
    }
    return firstLevel == title.level() && secondLevel == title.level();
  }
}
