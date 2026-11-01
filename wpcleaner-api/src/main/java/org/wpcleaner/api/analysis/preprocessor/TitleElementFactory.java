package org.wpcleaner.api.analysis.preprocessor;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.ArrayList;
import java.util.List;
import org.wpcleaner.api.analysis.Element;
import org.wpcleaner.api.analysis.comment.CommentElement;
import org.wpcleaner.api.analysis.title.TitleElement;

final class TitleElementFactory {

  private static final String TRAILING_WHITESPACE = " \t";
  private static final int MIN_SINGLE_RUN_COUNT = 3;
  private static final int MAX_LEVEL = 6;

  private final String text;
  private final List<TitleElement> titles;

  TitleElementFactory(final String text) {
    this.text = text;
    this.titles = new ArrayList<>();
  }

  List<TitleElement> getTitles() {
    return titles.stream().sorted(Element.COMPARATOR).toList();
  }

  void persist(final TitleLine titleLine, final List<CommentElement> comments) {
    final int titleEnd = findTitleEnd(titleLine, comments);
    final int equalsCount = countEqualsBefore(titleLine.begin(), titleEnd);
    if (equalsCount == 0) {
      return;
    }
    final int level =
        titleEnd - equalsCount == titleLine.begin()
            ? computeSingleRunLevel(equalsCount)
            : Math.min(equalsCount, titleLine.count());
    if (level == 0) {
      return;
    }
    titles.add(
        new TitleElement(
            titleLine.begin(),
            titleEnd,
            level,
            text.substring(titleLine.begin() + level, titleEnd - level).trim()));
  }

  private int findTitleEnd(final TitleLine titleLine, final List<CommentElement> comments) {
    int titleEnd = skipWhitespaceBefore(titleLine.begin(), titleLine.end());
    boolean commentFound = true;
    while (commentFound) {
      commentFound = false;
      for (final CommentElement comment : comments) {
        if (comment.end() == titleEnd && comment.begin() > titleLine.begin()) {
          titleEnd = skipWhitespaceBefore(titleLine.begin(), comment.begin());
          commentFound = true;
        }
      }
    }
    return titleEnd;
  }

  private int skipWhitespaceBefore(final int begin, final int end) {
    int index = end;
    while (index > begin && TRAILING_WHITESPACE.indexOf(text.charAt(index - 1)) >= 0) {
      index--;
    }
    return index;
  }

  private int countEqualsBefore(final int begin, final int end) {
    int count = 0;
    while (end - count > begin && text.charAt(end - count - 1) == BracketStack.EQUAL) {
      count++;
    }
    return count;
  }

  private static int computeSingleRunLevel(final int equalsCount) {
    if (equalsCount < MIN_SINGLE_RUN_COUNT) {
      return 0;
    }
    return Math.min(MAX_LEVEL, (equalsCount - 1) / 2);
  }
}
