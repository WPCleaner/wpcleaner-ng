package org.wpcleaner.api.analysis.tag;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
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
    final String tagName = extractTagName(text, cursor);
    if (tagName == null) {
      return Optional.empty();
    }
    cursor.moveAfterWhitespace();
    passAttributes(text, cursor);
    passOptionalSlash(text, cursor);
    if (cursor.getIndex() >= text.length() || TagElement.END != text.charAt(cursor.getIndex())) {
      return Optional.empty();
    }
    final TagElement tag = new TagElement(begin, cursor.getIndex() + 1, tagName);
    cursor.moveNext();
    return Optional.of(tag);
  }

  @Nullable
  private static String extractTagName(final String text, final TextBrowser.Cursor cursor) {
    final int startName = cursor.getIndex();
    while (cursor.getIndex() < text.length()
        && Character.isLetterOrDigit(text.charAt(cursor.getIndex()))
        && cursor.samePart()) {
      cursor.moveNext();
    }
    if (!cursor.samePart() || cursor.getIndex() == startName) {
      return null;
    }
    return text.substring(startName, cursor.getIndex());
  }

  private static void passAttributes(final String text, final TextBrowser.Cursor cursor) {
    final int startAttribute = cursor.getIndex();
    final String excludedChar = " \n<>/";
    while (cursor.getIndex() < text.length()
        && excludedChar.indexOf(text.charAt(cursor.getIndex())) < 0
        && cursor.samePart()) {
      cursor.moveNext();
    }
    cursor.moveAfterWhitespace();
    if (cursor.getIndex() >= text.length() || cursor.getIndex() == startAttribute) {
      return;
    }
    if (text.charAt(cursor.getIndex()) != TagElement.ATTRIBUTE_VALUE) {
      passAttributes(text, cursor);
      return;
    }
    cursor.moveNext();
    if (cursor.getIndex() >= text.length()) {
      return;
    }
    passAttributeValue(text, cursor);
    passAttributes(text, cursor);
  }

  private static void passAttributeValue(final String text, final TextBrowser.Cursor cursor) {
    final char startValueChar = text.charAt(cursor.getIndex());
    if (startValueChar == '"' || startValueChar == '\'') {
      final String excludedChars = "<>" + startValueChar;
      do {
        cursor.moveNext();
      } while (cursor.getIndex() < text.length()
          && excludedChars.indexOf(text.charAt(cursor.getIndex())) < 0);
      if (cursor.getIndex() < text.length() && text.charAt(cursor.getIndex()) == startValueChar) {
        cursor.moveNext();
      }
    } else {
      final String excludedChars = " \n\"'<>/";
      while (cursor.getIndex() < text.length()
          && excludedChars.indexOf(text.charAt(cursor.getIndex())) < 0) {
        cursor.moveNext();
      }
    }
    cursor.moveAfterWhitespace();
  }

  private static void passOptionalSlash(final String text, final TextBrowser.Cursor cursor) {
    if (cursor.getIndex() < text.length() && '/' == text.charAt(cursor.getIndex())) {
      cursor.moveNext();
    }
  }
}
