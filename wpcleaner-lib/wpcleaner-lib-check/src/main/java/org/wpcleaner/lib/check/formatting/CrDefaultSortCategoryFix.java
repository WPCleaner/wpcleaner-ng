package org.wpcleaner.lib.check.formatting;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Optional;
import org.springframework.stereotype.Component;
import org.wpcleaner.api.analysis.PageAnalysis;
import org.wpcleaner.api.analysis.function.FunctionElement;
import org.wpcleaner.api.repository.configuration.WPCleanerConfigurationRepository;
import org.wpcleaner.api.repository.configuration.WPCleanerParameter;

@Component
public class CrDefaultSortCategoryFix implements FormattingFix {

  private final WPCleanerConfigurationRepository wpcleanerRepository;

  public CrDefaultSortCategoryFix(final WPCleanerConfigurationRepository wpcleanerRepository) {
    this.wpcleanerRepository = wpcleanerRepository;
  }

  @Override
  public String fix(final PageAnalysis analysis) {
    return CountOption.from(
            wpcleanerRepository.getConfiguration(analysis.getWiki()),
            WPCleanerParameter.AUTO_CR_DEFAULTSORT_CATEGORY)
        .map(
            option ->
                TextChange.apply(
                    analysis.getText(),
                    FormattingText.getDefaultSorts(analysis).stream()
                        .flatMap(defaultSort -> fix(analysis, defaultSort, option).stream())
                        .toList()))
        .orElseGet(analysis::getText);
  }

  private static Optional<TextChange> fix(
      final PageAnalysis analysis, final FunctionElement defaultSort, final CountOption option) {
    final String text = analysis.getText();
    final int whitespaceEnd = FormattingText.skipWhitespaceAfter(text, defaultSort.end());
    final int lineEnds = FormattingText.countLineEnds(text, defaultSort.end(), whitespaceEnd);
    if (option.accepts(lineEnds)
        || FormattingText.findCategoryAt(analysis, whitespaceEnd).isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(
        new TextChange(
            defaultSort.end(),
            whitespaceEnd,
            FormattingText.LINE_END.repeat(option.normalize(lineEnds))));
  }
}
