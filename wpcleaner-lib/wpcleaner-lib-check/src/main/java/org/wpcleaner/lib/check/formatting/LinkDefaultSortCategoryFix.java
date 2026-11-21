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
import org.wpcleaner.api.analysis.category.CategoryElement;
import org.wpcleaner.api.analysis.function.FunctionElement;
import org.wpcleaner.api.repository.configuration.WPCleanerConfigurationRepository;
import org.wpcleaner.api.repository.configuration.WPCleanerParameter;

@Component
public class LinkDefaultSortCategoryFix implements FormattingFix {

  private final WPCleanerConfigurationRepository wpcleanerRepository;

  public LinkDefaultSortCategoryFix(final WPCleanerConfigurationRepository wpcleanerRepository) {
    this.wpcleanerRepository = wpcleanerRepository;
  }

  @Override
  public String fix(final PageAnalysis analysis) {
    if (!wpcleanerRepository
        .getConfiguration(analysis.getWiki())
        .getBoolean(WPCleanerParameter.AUTO_LINK_DEFAULTSORT_CATEGORY)) {
      return analysis.getText();
    }
    final Optional<FunctionElement> defaultSort =
        FormattingText.getDefaultSorts(analysis).stream().findFirst();
    final Optional<CategoryElement> category = analysis.getCategories().stream().findFirst();
    if (defaultSort.isEmpty() || category.isEmpty()) {
      return analysis.getText();
    }
    return fix(analysis, defaultSort.get(), category.get())
        .map(change -> TextChange.apply(analysis.getText(), List.of(change)))
        .orElseGet(analysis::getText);
  }

  private static Optional<TextChange> fix(
      final PageAnalysis analysis,
      final FunctionElement defaultSort,
      final CategoryElement category) {
    final String text = analysis.getText();
    final String movedDefaultSort =
        text.substring(defaultSort.begin(), defaultSort.end()) + FormattingText.LINE_END;
    final int removedEnd = defaultSort.end() + countRemovedLineEnds(text, defaultSort);
    if (defaultSort.begin() < category.begin()) {
      if (FormattingText.isOnlyWhitespaceBetween(text, defaultSort, category)
          || !isLineBegin(text, category.begin())) {
        return Optional.empty();
      }
      return Optional.of(
          new TextChange(
              defaultSort.begin(),
              category.begin(),
              text.substring(removedEnd, category.begin()) + movedDefaultSort));
    }
    if (!isOnlyCategoriesBetween(analysis, category, defaultSort)) {
      return Optional.empty();
    }
    return Optional.of(
        new TextChange(
            category.begin(),
            removedEnd,
            movedDefaultSort + text.substring(category.begin(), defaultSort.begin())));
  }

  private static int countRemovedLineEnds(final String text, final FunctionElement defaultSort) {
    if (!isLineEndAt(text, defaultSort.begin() - 1) || !isLineEndAt(text, defaultSort.end())) {
      return 0;
    }
    if (!isLineEndAt(text, defaultSort.begin() - 2) || !isLineEndAt(text, defaultSort.end() + 1)) {
      return 1;
    }
    return 2;
  }

  private static boolean isLineEndAt(final String text, final int index) {
    return index < 0 || (index < text.length() && FormattingText.isLineEnd(text.charAt(index)));
  }

  private static boolean isLineBegin(final String text, final int begin) {
    int index = begin;
    while (index > 0 && text.charAt(index - 1) == ' ') {
      index--;
    }
    return index == 0 || FormattingText.isLineEnd(text.charAt(index - 1));
  }

  private static boolean isOnlyCategoriesBetween(
      final PageAnalysis analysis, final Element previous, final Element next) {
    final String text = analysis.getText();
    int index = FormattingText.skipWhitespaceAfter(text, previous.end());
    while (index < next.begin()) {
      final Optional<CategoryElement> category = FormattingText.findCategoryAt(analysis, index);
      if (category.isEmpty()) {
        return false;
      }
      index = FormattingText.skipWhitespaceAfter(text, category.get().end());
    }
    return true;
  }
}
