package org.wpcleaner.api.analysis.wiki;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;
import org.wpcleaner.api.analysis.Element;
import org.wpcleaner.api.analysis.TextBrowser;

final class CurlyBracketEnds {

  private final NavigableMap<Integer, Integer> ends;

  CurlyBracketEnds(final List<? extends Element> curlyBracketElements) {
    this.ends = new TreeMap<>();
    curlyBracketElements.forEach(element -> ends.merge(element.begin(), element.end(), Math::max));
  }

  void moveAfterUntil(final TextBrowser.Cursor cursor, final String unauthorized) {
    final int begin = cursor.getIndex();
    cursor.moveAfterUntil(unauthorized);
    int end = findEndAfter(begin, cursor.getIndex());
    while (end > cursor.getIndex()) {
      while (cursor.getIndex() < end) {
        cursor.moveNext();
      }
      cursor.moveAfterUntil(unauthorized);
      end = findEndAfter(begin, cursor.getIndex());
    }
  }

  private int findEndAfter(final int begin, final int index) {
    return ends.subMap(begin, true, index, false).values().stream()
        .mapToInt(Integer::intValue)
        .max()
        .orElse(index);
  }
}
