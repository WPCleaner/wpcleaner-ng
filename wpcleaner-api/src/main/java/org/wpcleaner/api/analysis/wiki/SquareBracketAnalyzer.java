package org.wpcleaner.api.analysis.wiki;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.ArrayList;
import java.util.List;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.api.analysis.Element;
import org.wpcleaner.api.analysis.TextBrowser;
import org.wpcleaner.api.analysis.category.CategoryElement;
import org.wpcleaner.api.analysis.comment.CommentedText;
import org.wpcleaner.api.analysis.externallink.ExternalLinkElement;
import org.wpcleaner.api.analysis.internallink.InternalLinkElement;
import org.wpcleaner.api.analysis.interwikilink.InterwikiLinkElement;
import org.wpcleaner.api.analysis.languagelink.LanguageLinkElement;
import org.wpcleaner.api.repository.interwiki.Interwiki;
import org.wpcleaner.api.repository.namespace.Namespace;
import org.wpcleaner.api.repository.protocol.Protocol;

final class SquareBracketAnalyzer {

  static final char CLOSE = ']';
  static final char OPEN = '[';
  static final char PIPE = '|';
  private static final String AUTHORIZED_AFTER_1_OPEN = new String(new char[] {' ', CLOSE});
  private static final String AUTHORIZED_AFTER_2_OPEN = new String(new char[] {PIPE, CLOSE});
  private static final String UNAUTHORIZED_AFTER_1_OPEN =
      new String(new char[] {OPEN, ' ', CLOSE, '\n'});
  private static final String UNAUTHORIZED_AFTER_2_OPEN =
      new String(new char[] {OPEN, PIPE, CLOSE, '\n'});
  private static final char NEW_LINE = '\n';
  private static final char SPACE = ' ';
  private static final int TWO = 2;
  private static final int THREE = 3;
  private static final String DOUBLE_CLOSE = new String(new char[] {CLOSE, CLOSE});

  private final String text;
  private final CommentedText commentedText;
  private final List<Protocol> protocols;
  private final ImageAnalyzer imageAnalyzer;
  private final TextContainers containers;
  private final SquareBracketElementFactory elementFactory;
  private final List<ExternalLinkElement> externalLinks;

  SquareBracketAnalyzer(
      final String text,
      final CommentedText commentedText,
      final List<Interwiki> interwikis,
      final List<Namespace> namespaces,
      final List<Protocol> protocols,
      final ImageAnalyzer imageAnalyzer,
      final TextContainers containers) {
    this.text = text;
    this.commentedText = commentedText;
    this.protocols = protocols;
    this.imageAnalyzer = imageAnalyzer;
    this.containers = containers;
    this.elementFactory = new SquareBracketElementFactory(interwikis, namespaces);
    this.externalLinks = new ArrayList<>();
  }

  Element persistRedirectLink(
      final int begin, final int end, final String target, @Nullable final String value) {
    return elementFactory.persistRedirectLink(begin, end, target, value);
  }

  void analyzeSquareBracket(final TextBrowser.ReverseCursor reverseCursor) {
    final int lastOpeningBracket = reverseCursor.getIndex();
    final TextBrowser.ReverseCursor temporaryCursor = reverseCursor.copy();
    int firstOpeningBracket = lastOpeningBracket;
    int openingBracketsCount = 1;
    temporaryCursor.movePrevious();
    while (temporaryCursor.getIndex() >= 0
        && text.charAt(temporaryCursor.getIndex()) == OPEN
        && commentedText
            .withoutComments(temporaryCursor.getIndex() + 1, firstOpeningBracket)
            .isEmpty()) {
      firstOpeningBracket = temporaryCursor.getIndex();
      openingBracketsCount++;
      temporaryCursor.movePrevious();
    }
    if (openingBracketsCount == 1 || isProtocolAfter(reverseCursor)) {
      analyze1SquareBracket(lastOpeningBracket, reverseCursor);
    } else if (openingBracketsCount == TWO) {
      analyze2SquareBrackets(firstOpeningBracket, reverseCursor);
    }
  }

  private boolean isProtocolAfter(final TextBrowser.ReverseCursor reverseCursor) {
    final TextBrowser.Cursor cursor = reverseCursor.toCursor();
    cursor.moveNext();
    return isKnownProtocol(
        text, commentedText.skipWhitespaceAndComments(cursor.getIndex(), text.length()));
  }

  private void analyze1SquareBracket(
      final int firstOpeningBracket, final TextBrowser.ReverseCursor reverseCursor) {
    final TextBrowser.Cursor cursor = reverseCursor.toCursor();
    cursor.moveNext();
    final String target = extractTarget(cursor, UNAUTHORIZED_AFTER_1_OPEN, AUTHORIZED_AFTER_1_OPEN);
    if (target == null) {
      return;
    }
    final String value = extractExternalLinkText(cursor);
    if (cursor.getIndex() >= text.length()
        || text.charAt(cursor.getIndex()) != CLOSE
        || cursor.getIndex() >= containers.findContainer(firstOpeningBracket).end()) {
      return;
    }
    final int firstClosingBracket = cursor.getIndex();
    persist1SquareBracketResult(firstOpeningBracket, firstClosingBracket + 1, target, value);
    while (reverseCursor.getIndex() >= firstOpeningBracket) {
      reverseCursor.movePrevious();
    }
  }

  private void analyze2SquareBrackets(
      final int firstOpeningBracket, final TextBrowser.ReverseCursor reverseCursor) {
    final TextBrowser.Cursor cursor = reverseCursor.toCursor();
    cursor.moveNext();
    final String target = extractTarget(cursor, UNAUTHORIZED_AFTER_2_OPEN, AUTHORIZED_AFTER_2_OPEN);
    if (target == null) {
      return;
    }
    if (imageAnalyzer.analyze(firstOpeningBracket, target, cursor.getIndex())) {
      while (reverseCursor.getIndex() >= firstOpeningBracket) {
        reverseCursor.movePrevious();
      }
      return;
    }
    final String value = extractInternalLinkText(cursor);
    if (cursor.getIndex() >= text.length() || text.charAt(cursor.getIndex()) != CLOSE) {
      return;
    }
    final int firstClosingBracket = cursor.getIndex();
    final int closingBracketsCount = passClosingBrackets(cursor);
    if (closingBracketsCount < TWO) {
      return;
    }
    persist2SquareBracketsResult(
        firstOpeningBracket, firstClosingBracket, closingBracketsCount, target, value);
    while (reverseCursor.getIndex() >= firstOpeningBracket) {
      reverseCursor.movePrevious();
    }
  }

  private void persist2SquareBracketsResult(
      final int firstOpeningBracket,
      final int firstClosingBracket,
      final int closingBracketsCount,
      final String target,
      @Nullable final String value) {
    if (value != null && value.indexOf(OPEN) >= 0 && closingBracketsCount >= THREE) {
      elementFactory.persist(
          firstOpeningBracket, firstClosingBracket + THREE, target, value + CLOSE);
    } else {
      elementFactory.persist(firstOpeningBracket, firstClosingBracket + TWO, target, value);
    }
  }

  private int passClosingBrackets(final TextBrowser.Cursor cursor) {
    int closingBracketsCount = 0;
    do {
      closingBracketsCount++;
      cursor.moveNext();
    } while (cursor.samePart()
        && cursor.getIndex() < text.length()
        && text.charAt(cursor.getIndex()) == CLOSE);
    return closingBracketsCount;
  }

  private void persist1SquareBracketResult(
      final int begin, final int end, final String target, @Nullable final String value) {
    if (isKnownProtocol(target, 0)) {
      externalLinks.add(new ExternalLinkElement(begin, end, target, value, true));
    }
  }

  private boolean isKnownProtocol(final String value, final int index) {
    return protocols.stream()
        .anyMatch(
            protocol ->
                value.regionMatches(true, index, protocol.value(), 0, protocol.value().length()));
  }

  @Nullable
  private String extractTarget(
      final TextBrowser.Cursor cursor, final String unauthorized, final String authorized) {
    final int beginTarget = cursor.getIndex();
    cursor.moveAfterUntil(unauthorized);
    final int cursorIndex = cursor.getIndex();
    if (cursorIndex >= text.length()) {
      return null;
    }
    final char nextChar = text.charAt(cursorIndex);
    if (authorized.indexOf(nextChar) < 0) {
      return null;
    }
    return commentedText.withoutComments(beginTarget, cursorIndex);
  }

  @Nullable
  private String extractInternalLinkText(final TextBrowser.Cursor cursor) {
    if (!isAt(cursor, PIPE)) {
      return null;
    }
    cursor.moveNext();
    final int beginValue = cursor.getIndex();
    while (cursor.getIndex() < text.length()) {
      final int index = cursor.getIndex();
      if (isDoubled(index, OPEN)) {
        return null;
      }
      if (isDoubled(index, CLOSE)) {
        return text.substring(beginValue, index);
      }
      cursor.moveNext();
    }
    return null;
  }

  @Nullable
  private String extractExternalLinkText(final TextBrowser.Cursor cursor) {
    if (!isAt(cursor, SPACE)) {
      return null;
    }
    cursor.moveNext();
    final int beginValue = cursor.getIndex();
    while (cursor.getIndex() < text.length()) {
      final int index = cursor.getIndex();
      final char currentChar = text.charAt(index);
      if (currentChar == NEW_LINE) {
        return null;
      }
      if (currentChar == CLOSE) {
        return text.substring(beginValue, index);
      }
      if (isDoubled(index, OPEN)) {
        if (!passInternalLink(cursor, index)) {
          return null;
        }
      } else {
        cursor.moveNext();
      }
    }
    return null;
  }

  private boolean passInternalLink(final TextBrowser.Cursor cursor, final int linkBegin) {
    final int linkEnd = text.indexOf(DOUBLE_CLOSE, linkBegin + TWO);
    if (linkEnd < 0) {
      return false;
    }
    while (cursor.getIndex() < linkEnd + TWO) {
      cursor.moveNext();
    }
    return true;
  }

  private boolean isAt(final TextBrowser.Cursor cursor, final char expected) {
    return cursor.getIndex() < text.length() && text.charAt(cursor.getIndex()) == expected;
  }

  private boolean isDoubled(final int index, final char expected) {
    return index + 1 < text.length()
        && text.charAt(index) == expected
        && text.charAt(index + 1) == expected;
  }

  List<CategoryElement> getCategories() {
    return elementFactory.getCategories();
  }

  List<ExternalLinkElement> getExternalLinks() {
    return externalLinks;
  }

  List<InternalLinkElement> getInternalLinks() {
    return elementFactory.getInternalLinks();
  }

  List<InterwikiLinkElement> getInterwikiLinks() {
    return elementFactory.getInterwikiLinks();
  }

  List<LanguageLinkElement> getLanguageLinks() {
    return elementFactory.getLanguageLinks();
  }
}
