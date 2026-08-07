package org.wpcleaner.api.analysis.comment;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.ArrayList;
import java.util.List;

final class CommentAnalyzer {

  private CommentAnalyzer() {
    // Utility class
  }

  static List<CommentElement> analyze(final String text) {
    final List<CommentElement> result = new ArrayList<>();
    int begin = text.indexOf(CommentElement.START);
    while (begin >= 0) {
      int end = text.indexOf(CommentElement.END, begin + CommentElement.START.length());
      if (end > 0) {
        end += CommentElement.END.length();
        result.add(new CommentElement(begin, end));
        begin = text.indexOf(CommentElement.START, end);
      } else {
        begin = -1;
      }
    }
    return result;
  }
}
