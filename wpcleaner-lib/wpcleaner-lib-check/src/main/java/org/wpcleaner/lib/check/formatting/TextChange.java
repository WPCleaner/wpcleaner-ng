package org.wpcleaner.lib.check.formatting;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import org.wpcleaner.api.analysis.Element;

record TextChange(int begin, int end, String replacement) implements Element {

  static String apply(final String text, final List<TextChange> changes) {
    if (changes.isEmpty()) {
      return text;
    }
    final StringBuilder result = new StringBuilder(text.length());
    int lastEnd = 0;
    for (final TextChange change : changes.stream().sorted(COMPARATOR).toList()) {
      if (change.begin() >= lastEnd) {
        result.append(text, lastEnd, change.begin()).append(change.replacement());
        lastEnd = change.end();
      }
    }
    return result.append(text, lastEnd, text.length()).toString();
  }
}
