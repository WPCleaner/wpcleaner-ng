package org.wpcleaner.api.analysis.comment;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.ArrayList;
import java.util.List;

final class CommentAnalyzer {

  private final String text;

  CommentAnalyzer(final String text) {
    this.text = text;
  }

  List<CommentElement> analyze() {
    final List<CommentElement> result = new ArrayList<>();
    int begin = text.indexOf(CommentElement.TOKEN_START);
    while (begin >= 0) {
      int end = text.indexOf(CommentElement.TOKEN_END, begin + CommentElement.TOKEN_START.length());
      if (end > 0) {
        end += CommentElement.TOKEN_END.length();
        result.add(new CommentElement(begin, end));
        begin = text.indexOf(CommentElement.TOKEN_START, end);
      } else {
        begin = -1;
      }
    }
    return result;
  }
}
