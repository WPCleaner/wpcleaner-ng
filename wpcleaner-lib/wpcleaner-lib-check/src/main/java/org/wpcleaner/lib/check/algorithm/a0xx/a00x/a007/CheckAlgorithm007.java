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
import org.wpcleaner.lib.check.algorithm.TitleReplacement;

@Component
public class CheckAlgorithm007 implements CheckAlgorithm {

  private static final int ALGORITHM_NUMBER = 7;
  private static final int EXPECTED_LEVEL = 2;

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
            titles.size() == 1
                ? List.of(TitleReplacement.withLevel(analysis, firstTitle, EXPECTED_LEVEL))
                : List.of()));
  }
}
