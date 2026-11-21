package org.wpcleaner.lib.check.formatting;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import org.springframework.stereotype.Component;
import org.wpcleaner.api.analysis.PageAnalysis;
import org.wpcleaner.api.analysis.category.CategoryElement;
import org.wpcleaner.api.repository.configuration.WPCleanerConfigurationRepository;
import org.wpcleaner.api.repository.configuration.WPCleanerParameter;

@Component
public class CrBetweenCategoryFix implements FormattingFix {

  private final WPCleanerConfigurationRepository wpcleanerRepository;

  public CrBetweenCategoryFix(final WPCleanerConfigurationRepository wpcleanerRepository) {
    this.wpcleanerRepository = wpcleanerRepository;
  }

  @Override
  public String fix(final PageAnalysis analysis) {
    return CountOption.from(
            wpcleanerRepository.getConfiguration(analysis.getWiki()),
            WPCleanerParameter.AUTO_CR_BETWEEN_CATEGORY)
        .map(option -> fix(analysis, option))
        .orElseGet(analysis::getText);
  }

  private static String fix(final PageAnalysis analysis, final CountOption option) {
    final String text = analysis.getText();
    final List<CategoryElement> categories = analysis.getCategories();
    return TextChange.apply(
        text,
        IntStream.range(1, categories.size())
            .mapToObj(index -> fix(text, categories.get(index - 1), categories.get(index), option))
            .flatMap(Optional::stream)
            .toList());
  }

  private static Optional<TextChange> fix(
      final String text,
      final CategoryElement previous,
      final CategoryElement category,
      final CountOption option) {
    if (!FormattingText.isOnlyWhitespaceBetween(text, previous, category)) {
      return Optional.empty();
    }
    final int begin = previous.end();
    final int end = category.begin();
    final int lineEnds = FormattingText.countLineEnds(text, begin, end);
    if (option.accepts(lineEnds)) {
      return Optional.empty();
    }
    return Optional.of(
        new TextChange(begin, end, FormattingText.LINE_END.repeat(option.normalize(lineEnds))));
  }
}
