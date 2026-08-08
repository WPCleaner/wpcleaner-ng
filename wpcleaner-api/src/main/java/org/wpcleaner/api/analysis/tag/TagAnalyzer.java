package org.wpcleaner.api.analysis.tag;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.wpcleaner.api.analysis.TextBrowser;

final class TagAnalyzer {

  private TagAnalyzer() {
    // Utility class
  }

  static List<TagElement> analyze(final String text, final TextBrowser textBrowser) {
    final List<TagElement> result = new ArrayList<>();
    final TextBrowser.Cursor cursor = textBrowser.cursor();
    while (cursor.getIndex() < text.length()) {
      analyze(text, cursor).ifPresent(result::add);
    }
    return result;
  }

  private static Optional<TagElement> analyze(final String text, final TextBrowser.Cursor cursor) {
    final int begin = cursor.getIndex();
    if (TagElement.START != text.charAt(begin)) {
      cursor.moveNext();
      return Optional.empty();
    }
    cursor.moveNext();
    passOptionalSlash(text, cursor);
    final int startName = cursor.getIndex();
    passTagName(text, cursor);
    if (!cursor.samePart() || cursor.getIndex() == startName) {
      return Optional.empty();
    }
    final int endName = cursor.getIndex();
    cursor.moveAfterWhitespace();
    passOptionalSlash(text, cursor);
    if (cursor.getIndex() >= text.length() || TagElement.END != text.charAt(cursor.getIndex())) {
      return Optional.empty();
    }
    final TagElement tag =
        new TagElement(begin, cursor.getIndex() + 1, text.substring(startName, endName));
    cursor.moveNext();
    return Optional.of(tag);
  }

  private static void passTagName(final String text, final TextBrowser.Cursor cursor) {
    while (cursor.getIndex() < text.length()
        && Character.isLetterOrDigit(text.charAt(cursor.getIndex()))
        && cursor.samePart()) {
      cursor.moveNext();
    }
  }

  private static void passOptionalSlash(final String text, final TextBrowser.Cursor cursor) {
    if (cursor.getIndex() < text.length() && '/' == text.charAt(cursor.getIndex())) {
      cursor.moveNext();
    }
  }
}
