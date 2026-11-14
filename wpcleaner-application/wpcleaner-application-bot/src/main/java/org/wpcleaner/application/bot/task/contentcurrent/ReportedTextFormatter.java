package org.wpcleaner.application.bot.task.contentcurrent;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

public final class ReportedTextFormatter {

  private static final String NOWIKI_START = "<nowiki>";
  private static final String NOWIKI_END = "</nowiki>";
  private static final char NEW_LINE_SYMBOL = '↵';

  private ReportedTextFormatter() {}

  public static String format(final String text) {
    final StringBuilder result = new StringBuilder(text.length() + NOWIKI_START.length() * 2);
    result.append(NOWIKI_START);
    for (int index = 0; index < text.length(); index++) {
      final char character = text.charAt(index);
      switch (character) {
        case '&' -> result.append("&amp;");
        case '<' -> result.append("&lt;");
        case '\n' -> result.append(NEW_LINE_SYMBOL);
        case '\r' -> {
          if (index + 1 >= text.length() || text.charAt(index + 1) != '\n') {
            result.append(NEW_LINE_SYMBOL);
          }
        }
        default -> result.append(character);
      }
    }
    return result.append(NOWIKI_END).toString();
  }
}
