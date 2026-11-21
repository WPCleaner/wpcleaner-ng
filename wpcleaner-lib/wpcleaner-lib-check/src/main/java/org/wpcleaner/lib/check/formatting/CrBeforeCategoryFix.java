package org.wpcleaner.lib.check.formatting;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.wpcleaner.api.analysis.Element;
import org.wpcleaner.api.analysis.PageAnalysis;
import org.wpcleaner.api.analysis.title.TitleElement;
import org.wpcleaner.api.repository.configuration.WPCleanerConfigurationRepository;
import org.wpcleaner.api.repository.configuration.WPCleanerParameter;

@Component
public class CrBeforeCategoryFix implements FormattingFix {

  private final WPCleanerConfigurationRepository wpcleanerRepository;

  public CrBeforeCategoryFix(final WPCleanerConfigurationRepository wpcleanerRepository) {
    this.wpcleanerRepository = wpcleanerRepository;
  }

  @Override
  public String fix(final PageAnalysis analysis) {
    return CountOption.from(
            wpcleanerRepository.getConfiguration(analysis.getWiki()),
            WPCleanerParameter.AUTO_CR_BEFORE_CATEGORY)
        .flatMap(option -> fix(analysis, option))
        .map(change -> TextChange.apply(analysis.getText(), List.of(change)))
        .orElseGet(analysis::getText);
  }

  private static Optional<TextChange> fix(final PageAnalysis analysis, final CountOption option) {
    final String text = analysis.getText();
    final List<TitleElement> titles = analysis.getTitles();
    final int lastTitleEnd = titles.isEmpty() ? 0 : titles.getLast().end();
    return FormattingText.getDefaultSorts(analysis).stream()
        .findFirst()
        .<Element>map(defaultSort -> defaultSort)
        .or(() -> analysis.getCategories().stream().findFirst())
        .filter(element -> element.begin() >= lastTitleEnd)
        .flatMap(element -> fix(text, element.begin(), option));
  }

  private static Optional<TextChange> fix(
      final String text, final int elementBegin, final CountOption option) {
    final int whitespaceBegin = FormattingText.skipWhitespaceBefore(text, elementBegin);
    final int lineEnds = FormattingText.countLineEnds(text, whitespaceBegin, elementBegin);
    if (whitespaceBegin == 0 || lineEnds == 0 || option.accepts(lineEnds)) {
      return Optional.empty();
    }
    return Optional.of(
        new TextChange(
            whitespaceBegin,
            elementBegin,
            FormattingText.LINE_END.repeat(option.normalize(lineEnds))));
  }
}
