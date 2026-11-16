package org.wpcleaner.lib.check.algorithm.a0xx.a00x.a009;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Optional;
import org.wpcleaner.api.analysis.PageAnalysis;
import org.wpcleaner.api.analysis.category.CategoryElement;
import org.wpcleaner.api.analysis.function.FunctionElement;

record LinePrefix(int begin, String text) {

  private static final String DEFAULT_SORT = "defaultsort";
  private static final char SPACE = ' ';
  private static final char LINE_END = '\n';
  private static final String LINE_BREAK = "\n";
  private static final String EMPTY_LINE = "\n\n";

  static LinePrefix none(final int begin) {
    return new LinePrefix(begin, "");
  }

  static LinePrefix of(final PageAnalysis analysis, final CategoryElement category) {
    final String text = analysis.getText();
    final int previousEnd = skipSpacesAndLinesBackward(text, category.begin());
    if (analysis.getTemplates().stream().anyMatch(template -> template.end() == previousEnd)) {
      return new LinePrefix(previousEnd, EMPTY_LINE);
    }
    final Optional<FunctionElement> previousFunction =
        analysis.getFunctions().stream()
            .filter(function -> function.end() == previousEnd)
            .findFirst();
    if (previousFunction.isPresent()) {
      return afterFunction(text, previousFunction.get());
    }
    if (analysis.getRedirect().isPresent() && text.lastIndexOf(LINE_END, category.begin()) < 0) {
      return new LinePrefix(previousEnd, EMPTY_LINE);
    }
    return none(category.begin());
  }

  private static LinePrefix afterFunction(final String text, final FunctionElement function) {
    if (!DEFAULT_SORT.equals(function.magicWord())) {
      return new LinePrefix(function.end(), LINE_BREAK);
    }
    final int begin = skipSpacesAndLinesBackward(text, function.begin());
    return new LinePrefix(
        begin,
        (begin > 0 ? EMPTY_LINE : "")
            + text.substring(function.begin(), function.end())
            + LINE_BREAK);
  }

  private static int skipSpacesAndLinesBackward(final String text, final int index) {
    int begin = index;
    while (begin > 0 && (text.charAt(begin - 1) == SPACE || text.charAt(begin - 1) == LINE_END)) {
      begin--;
    }
    return begin;
  }
}
