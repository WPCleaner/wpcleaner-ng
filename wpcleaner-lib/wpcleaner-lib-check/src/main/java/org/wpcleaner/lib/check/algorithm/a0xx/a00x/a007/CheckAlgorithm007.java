package org.wpcleaner.lib.check.algorithm.a0xx.a00x.a007;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import org.springframework.stereotype.Component;
import org.wpcleaner.api.analysis.PageAnalysis;
import org.wpcleaner.api.analysis.title.TitleElement;
import org.wpcleaner.lib.check.CheckAlgorithm;
import org.wpcleaner.lib.check.CheckError;
import org.wpcleaner.lib.check.Replacement;

@Component
public class CheckAlgorithm007 implements CheckAlgorithm {

  private static final int ALGORITHM_NUMBER = 7;
  private static final int EXPECTED_LEVEL = 2;
  private static final char EQUAL = '=';

  @Override
  public int number() {
    return ALGORITHM_NUMBER;
  }

  @Override
  public List<CheckError> analyze(final PageAnalysis analysis) {
    final List<TitleElement> titles = analysis.getTitles();
    if (titles.isEmpty() || titles.stream().anyMatch(title -> title.level() <= EXPECTED_LEVEL)) {
      return List.of();
    }
    final TitleElement firstTitle = titles.getFirst();
    return List.of(
        new CheckError(
            ALGORITHM_NUMBER,
            firstTitle.begin(),
            firstTitle.end(),
            titles.size() == 1 ? createReplacements(analysis, firstTitle) : List.of()));
  }

  private static List<Replacement> createReplacements(
      final PageAnalysis analysis, final TitleElement title) {
    final String marker = String.valueOf(EQUAL).repeat(EXPECTED_LEVEL);
    final String text = title.title().isEmpty() ? " " : " " + title.title() + " ";
    return List.of(new Replacement(marker + text + marker, !isUnreliable(analysis, title)));
  }

  private static boolean isUnreliable(final PageAnalysis analysis, final TitleElement title) {
    return !isCoherent(analysis.getText(), title)
        || title.isInsideOne(analysis.getTemplates())
        || !analysis.getIncludeAreas().isEmpty();
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
