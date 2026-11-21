package org.wpcleaner.lib.check.algorithm.a0xx.a02x.a025;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import org.wpcleaner.api.analysis.PageAnalysis;
import org.wpcleaner.api.analysis.title.TitleElement;
import org.wpcleaner.lib.check.CheckAlgorithm;
import org.wpcleaner.lib.check.CheckError;
import org.wpcleaner.lib.check.Replacement;
import org.wpcleaner.lib.check.algorithm.TitleReplacement;

@Component
public class CheckAlgorithm025 implements CheckAlgorithm {

  private static final int ALGORITHM_NUMBER = 25;

  @Override
  public int number() {
    return ALGORITHM_NUMBER;
  }

  @Override
  public List<CheckError> analyze(final PageAnalysis analysis) {
    if (analysis.getPageTitle().namespace() % 2 != 0) {
      return List.of();
    }
    final List<TitleElement> titles = analysis.getTitles();
    final List<CheckError> errors = new ArrayList<>();
    for (int titleIndex = 1; titleIndex < titles.size(); titleIndex++) {
      final TitleElement title = titles.get(titleIndex);
      final int expectedLevel = titles.get(titleIndex - 1).level() + 1;
      if (title.level() > expectedLevel) {
        errors.add(
            new CheckError(
                ALGORITHM_NUMBER,
                title.begin(),
                title.end(),
                createReplacements(analysis, titles, titleIndex, expectedLevel)));
      }
    }
    return List.copyOf(errors);
  }

  private static List<Replacement> createReplacements(
      final PageAnalysis analysis,
      final List<TitleElement> titles,
      final int titleIndex,
      final int expectedLevel) {
    final boolean hasSubTitles =
        titleIndex + 1 < titles.size() && titles.get(titleIndex + 1).level() > expectedLevel;
    if (hasSubTitles) {
      return List.of();
    }
    return List.of(TitleReplacement.withLevel(analysis, titles.get(titleIndex), expectedLevel));
  }
}
