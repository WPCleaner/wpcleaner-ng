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

  private final String text;
  private final TextBrowser textBrowser;

  TagAnalyzer(final String text, final TextBrowser textBrowser) {
    this.text = text;
    this.textBrowser = textBrowser;
  }

  List<TagElement> analyze() {
    final List<TagElement> result = new ArrayList<>();
    final TextBrowser.Cursor cursor = textBrowser.cursor();
    while (cursor.getIndex() < text.length()) {
      analyze(cursor).ifPresent(result::add);
    }
    return result;
  }

  private Optional<TagElement> analyze(final TextBrowser.Cursor cursor) {
    final int begin = cursor.getIndex();
    if (TagElement.TOKEN_START != text.charAt(begin)) {
      cursor.moveNext();
      return Optional.empty();
    }
    cursor.moveNext();
    passOptionalSlash(text, cursor);
    final String tagName = extractTagName(cursor);
    if (tagName == null) {
      return Optional.empty();
    }
    cursor.moveAfterWhitespace();
    passAttributes(cursor);
    passOptionalSlash(text, cursor);
    if (cursor.getIndex() >= text.length()
        || TagElement.TOKEN_END != text.charAt(cursor.getIndex())) {
      return Optional.empty();
    }
    final TagElement tag = new TagElement(begin, cursor.getIndex() + 1, tagName);
    cursor.moveNext();
    return Optional.of(tag);
  }

  @Nullable
  private String extractTagName(final TextBrowser.Cursor cursor) {
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

  private void passAttributes(final TextBrowser.Cursor cursor) {
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
    if (text.charAt(cursor.getIndex()) != TagElement.TOKEN_ATTRIBUTE_VALUE) {
      passAttributes(cursor);
      return;
    }
    cursor.moveNext();
    if (cursor.getIndex() >= text.length()) {
      return;
    }
    passAttributeValue(cursor);
    passAttributes(cursor);
  }

  private void passAttributeValue(final TextBrowser.Cursor cursor) {
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
