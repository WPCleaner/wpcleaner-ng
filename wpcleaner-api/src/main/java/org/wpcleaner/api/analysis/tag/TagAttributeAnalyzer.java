package org.wpcleaner.api.analysis.tag;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.wpcleaner.api.analysis.TextBrowser;

public final class TagAttributeAnalyzer {

  private static final String ATTRIBUTE_NAME_END = " \n\t\"'<>/=";
  private static final String UNQUOTED_ATTRIBUTE_VALUE_END = " \n\t\"'<>";

  private final String text;

  public TagAttributeAnalyzer(final String text) {
    this.text = text;
  }

  public List<TagAttribute> extractAttributes(final TextBrowser.Cursor cursor) {
    final List<TagAttribute> attributes = new ArrayList<>();
    Optional<TagAttribute> attribute = extractAttribute(cursor);
    while (attribute.isPresent()) {
      attributes.add(attribute.get());
      attribute = extractAttribute(cursor);
    }
    return attributes;
  }

  private Optional<TagAttribute> extractAttribute(final TextBrowser.Cursor cursor) {
    final int beginName = cursor.getIndex();
    cursor.moveAfterUntil(ATTRIBUTE_NAME_END);
    if (cursor.getIndex() == beginName) {
      return Optional.empty();
    }
    final String name = text.substring(beginName, cursor.getIndex());
    cursor.moveAfterWhitespace();
    if (cursor.getIndex() >= text.length()
        || TagElement.TOKEN_ATTRIBUTE_VALUE != text.charAt(cursor.getIndex())) {
      return Optional.of(new TagAttribute(name, null));
    }
    cursor.moveNext();
    cursor.moveAfterWhitespace();
    final String value = extractAttributeValue(cursor);
    cursor.moveAfterWhitespace();
    return Optional.of(new TagAttribute(name, value));
  }

  private String extractAttributeValue(final TextBrowser.Cursor cursor) {
    final int beginValue = cursor.getIndex();
    if (beginValue >= text.length()) {
      return "";
    }
    final char quote = text.charAt(beginValue);
    if (quote == '"' || quote == '\'') {
      cursor.moveNext();
      final int beginQuotedValue = cursor.getIndex();
      cursor.moveAfterUntil("<>" + quote);
      final int endQuotedValue = cursor.getIndex();
      if (endQuotedValue < text.length() && text.charAt(endQuotedValue) == quote) {
        cursor.moveNext();
      }
      return text.substring(beginQuotedValue, endQuotedValue);
    }
    while (cursor.getIndex() < text.length()
        && UNQUOTED_ATTRIBUTE_VALUE_END.indexOf(text.charAt(cursor.getIndex())) < 0
        && !isSelfClosingEnd(cursor.getIndex())) {
      cursor.moveNext();
    }
    return text.substring(beginValue, cursor.getIndex());
  }

  private boolean isSelfClosingEnd(final int index) {
    return text.charAt(index) == TagElement.TOKEN_SLASH
        && index + 1 < text.length()
        && text.charAt(index + 1) == TagElement.TOKEN_END;
  }
}
