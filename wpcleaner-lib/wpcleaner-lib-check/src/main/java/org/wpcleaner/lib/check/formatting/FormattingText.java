package org.wpcleaner.lib.check.formatting;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Optional;
import org.wpcleaner.api.analysis.Element;
import org.wpcleaner.api.analysis.PageAnalysis;
import org.wpcleaner.api.analysis.category.CategoryElement;
import org.wpcleaner.api.analysis.function.FunctionElement;

final class FormattingText {

  static final String LINE_END = "\n";

  private static final char LINE_END_CHARACTER = '\n';
  private static final char SPACE = ' ';
  private static final String DEFAULT_SORT = "defaultsort";

  private FormattingText() {}

  static boolean isWhitespace(final char character) {
    return character == SPACE || character == LINE_END_CHARACTER;
  }

  static boolean isLineEnd(final char character) {
    return character == LINE_END_CHARACTER;
  }

  static int skipWhitespaceAfter(final String text, final int begin) {
    int index = begin;
    while (index < text.length() && isWhitespace(text.charAt(index))) {
      index++;
    }
    return index;
  }

  static int skipWhitespaceBefore(final String text, final int end) {
    int index = end;
    while (index > 0 && isWhitespace(text.charAt(index - 1))) {
      index--;
    }
    return index;
  }

  static boolean isOnlyWhitespace(final String text, final int from, final int to) {
    return from <= to && skipWhitespaceAfter(text, from) >= to;
  }

  static boolean isOnlyWhitespaceBetween(
      final String text, final Element previous, final Element next) {
    return isOnlyWhitespace(text, previous.end(), next.begin());
  }

  static int countLineEnds(final String text, final int begin, final int end) {
    return (int) text.substring(begin, end).chars().filter(c -> c == LINE_END_CHARACTER).count();
  }

  static List<FunctionElement> getDefaultSorts(final PageAnalysis analysis) {
    return analysis.getFunctions().stream()
        .filter(function -> DEFAULT_SORT.equals(function.magicWord()))
        .toList();
  }

  static Optional<CategoryElement> findCategoryAt(final PageAnalysis analysis, final int begin) {
    return analysis.getCategories().stream()
        .filter(category -> category.begin() == begin)
        .findFirst();
  }

  static Optional<Element> findCategoryOrDefaultSortAt(
      final PageAnalysis analysis, final int begin) {
    return findCategoryAt(analysis, begin)
        .<Element>map(category -> category)
        .or(
            () ->
                getDefaultSorts(analysis).stream()
                    .filter(defaultSort -> defaultSort.begin() == begin)
                    .findFirst());
  }
}
