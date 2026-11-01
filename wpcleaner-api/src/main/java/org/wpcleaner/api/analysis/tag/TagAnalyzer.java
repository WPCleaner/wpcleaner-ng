package org.wpcleaner.api.analysis.tag;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.api.analysis.Element;
import org.wpcleaner.api.analysis.TextBrowser;

final class TagAnalyzer {

  private final String text;
  private final TextBrowser textBrowser;
  private final TagAttributeAnalyzer attributeAnalyzer;
  private final List<TagElement> tags;
  private final List<CompleteTagElement> completeTags;
  private final Map<String, Deque<TagElement>> unmatchedStartTags;

  TagAnalyzer(final String text, final TextBrowser textBrowser) {
    this.text = text;
    this.textBrowser = textBrowser;
    this.attributeAnalyzer = new TagAttributeAnalyzer(text);
    this.tags = new ArrayList<>();
    this.completeTags = new ArrayList<>();
    this.unmatchedStartTags = new HashMap<>();
  }

  void analyze() {
    final TextBrowser.Cursor cursor = textBrowser.cursor();
    while (cursor.getIndex() < text.length()) {
      analyze(cursor).ifPresent(this::persistTag);
    }
    completeTags.sort(Element.COMPARATOR);
  }

  List<TagElement> getTags() {
    return tags;
  }

  List<CompleteTagElement> getCompleteTags() {
    return completeTags;
  }

  private void persistTag(final TagElement tag) {
    tags.add(tag);
    switch (tag.kind()) {
      case START -> persistStartTag(tag);
      case END -> persistEndTag(tag);
      case SELF_CLOSING -> {}
    }
  }

  private void persistStartTag(final TagElement startTag) {
    if (startTag.type().map(TagType::isVoid).orElse(false)) {
      return;
    }
    unmatchedStartTags
        .computeIfAbsent(startTag.normalizedName(), name -> new ArrayDeque<>())
        .push(startTag);
  }

  private void persistEndTag(final TagElement endTag) {
    final Deque<TagElement> startTags = unmatchedStartTags.get(endTag.normalizedName());
    if (startTags != null && !startTags.isEmpty()) {
      completeTags.add(new CompleteTagElement(startTags.pop(), endTag));
    }
  }

  private Optional<TagElement> analyze(final TextBrowser.Cursor cursor) {
    final int begin = cursor.getIndex();
    if (TagElement.TOKEN_START != text.charAt(begin)) {
      cursor.moveNext();
      return Optional.empty();
    }
    cursor.moveNext();
    final boolean endTag = passOptionalSlash(cursor);
    final String tagName = extractTagName(cursor);
    if (tagName == null) {
      return Optional.empty();
    }
    cursor.moveAfterWhitespace();
    final List<TagAttribute> attributes = attributeAnalyzer.extractAttributes(cursor);
    final boolean selfClosing = passOptionalSlash(cursor);
    if (cursor.getIndex() >= text.length()
        || TagElement.TOKEN_END != text.charAt(cursor.getIndex())) {
      return Optional.empty();
    }
    final TagElement tag =
        new TagElement(
            begin, cursor.getIndex() + 1, tagName, computeKind(endTag, selfClosing), attributes);
    cursor.moveNext();
    return Optional.of(tag);
  }

  private static TagKind computeKind(final boolean endTag, final boolean selfClosing) {
    if (endTag) {
      return TagKind.END;
    }
    if (selfClosing) {
      return TagKind.SELF_CLOSING;
    }
    return TagKind.START;
  }

  @Nullable
  private String extractTagName(final TextBrowser.Cursor cursor) {
    final int startName = cursor.getIndex();
    if (startName >= text.length() || !Character.isLetter(text.charAt(startName))) {
      return null;
    }
    while (cursor.getIndex() < text.length()
        && Character.isLetterOrDigit(text.charAt(cursor.getIndex()))
        && cursor.samePart()) {
      cursor.moveNext();
    }
    if (!cursor.samePart()) {
      return null;
    }
    return text.substring(startName, cursor.getIndex());
  }

  private boolean passOptionalSlash(final TextBrowser.Cursor cursor) {
    if (cursor.getIndex() < text.length()
        && TagElement.TOKEN_SLASH == text.charAt(cursor.getIndex())) {
      cursor.moveNext();
      return true;
    }
    return false;
  }
}
