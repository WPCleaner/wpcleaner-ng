package org.wpcleaner.api.analysis.comment;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

public record CommentBuilder(String comment) {

  private static final String ESCAPED_START = "< !--";
  private static final String ESCAPED_END = "-- >";
  private static final char SPACE = ' ';

  public static CommentBuilder from(final String comment) {
    return new CommentBuilder(comment);
  }

  public String build() {
    final String escaped =
        comment
            .strip()
            .replace(CommentElement.TOKEN_START, ESCAPED_START)
            .replace(CommentElement.TOKEN_END, ESCAPED_END);
    if (escaped.isEmpty()) {
      return CommentElement.TOKEN_START + SPACE + CommentElement.TOKEN_END;
    }
    return CommentElement.TOKEN_START + SPACE + escaped + SPACE + CommentElement.TOKEN_END;
  }
}
