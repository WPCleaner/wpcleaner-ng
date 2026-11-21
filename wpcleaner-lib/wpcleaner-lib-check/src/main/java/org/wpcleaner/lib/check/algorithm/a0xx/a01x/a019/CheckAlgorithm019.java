package org.wpcleaner.lib.check.algorithm.a0xx.a01x.a019;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.wpcleaner.api.analysis.PageAnalysis;
import org.wpcleaner.api.analysis.pagetitle.PageTitle;
import org.wpcleaner.api.analysis.title.TitleElement;
import org.wpcleaner.api.repository.namespace.CommonNamespaces;
import org.wpcleaner.lib.check.CheckAlgorithm;
import org.wpcleaner.lib.check.CheckError;
import org.wpcleaner.lib.check.Replacement;
import org.wpcleaner.lib.check.algorithm.TitleReplacement;

@Component
public class CheckAlgorithm019 implements CheckAlgorithm {

  private static final int ALGORITHM_NUMBER = 19;
  private static final int REPORTED_LEVEL = 1;
  private static final int EXPECTED_LEVEL = 2;

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
    for (int titleIndex = 0; titleIndex < titles.size(); titleIndex++) {
      final TitleElement title = titles.get(titleIndex);
      if (title.level() == REPORTED_LEVEL) {
        errors.add(
            new CheckError(
                ALGORITHM_NUMBER,
                title.begin(),
                title.end(),
                errors.isEmpty() ? createReplacements(analysis, titles, titleIndex) : List.of()));
      }
    }
    return List.copyOf(errors);
  }

  private static List<Replacement> createReplacements(
      final PageAnalysis analysis, final List<TitleElement> titles, final int titleIndex) {
    final TitleElement title = titles.get(titleIndex);
    if (titleIndex == titles.size() - 1) {
      return List.of(TitleReplacement.withLevel(analysis, title, EXPECTED_LEVEL));
    }
    if (titleIndex == 0 && isPageTitle(analysis, title)) {
      return List.of(new Replacement("", TitleReplacement.isReliable(analysis, title)));
    }
    return List.of();
  }

  private static boolean isPageTitle(final PageAnalysis analysis, final TitleElement title) {
    final Optional<PageTitle> normalized =
        analysis.normalizeTitle(title.title(), CommonNamespaces.MAIN.id);
    return normalized.filter(analysis.getPageTitle()::equals).isPresent();
  }
}
