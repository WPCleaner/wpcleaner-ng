package org.wpcleaner.api.analysis.wiki;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.ArrayList;
import java.util.List;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.api.analysis.TextBrowser;
import org.wpcleaner.api.analysis.category.CategoryElement;
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
      new String(new char[] {OPEN, PIPE, ' ', CLOSE, '\n'});
  private static final String UNAUTHORIZED_AFTER_1_OPEN_AND_SEPARATOR =
      new String(new char[] {OPEN, CLOSE, '\n'});
  private static final String UNAUTHORIZED_AFTER_2_OPEN =
      new String(new char[] {OPEN, PIPE, CLOSE, '\n'});
  private static final String UNAUTHORIZED_AFTER_2_OPEN_AND_SEPARATOR =
      new String(new char[] {OPEN, CLOSE, '\n'});
  private static final int TWO = 2;

  private final String text;
  private final List<Protocol> protocols;
  private final ImageAnalyzer imageAnalyzer;
  private final SquareBracketElementFactory elementFactory;
  private final List<ExternalLinkElement> externalLinks;

  SquareBracketAnalyzer(
      final String text,
      final List<Interwiki> interwikis,
      final List<Namespace> namespaces,
      final List<Protocol> protocols,
      final ImageAnalyzer imageAnalyzer) {
    this.text = text;
    this.protocols = protocols;
    this.imageAnalyzer = imageAnalyzer;
    this.elementFactory = new SquareBracketElementFactory(interwikis, namespaces);
    this.externalLinks = new ArrayList<>();
  }

  void analyzeSquareBracket(final TextBrowser.ReverseCursor reverseCursor) {
    final int lastOpeningBracket = reverseCursor.getIndex();
    final TextBrowser.ReverseCursor temporaryCursor = reverseCursor.copy();
    int firstOpeningBracket = lastOpeningBracket;
    while (temporaryCursor.getIndex() >= 0
        && text.charAt(temporaryCursor.getIndex()) == OPEN
        && temporaryCursor.samePart()) {
      firstOpeningBracket = temporaryCursor.getIndex();
      temporaryCursor.movePrevious();
    }
    final int openingBracketsCount = lastOpeningBracket - firstOpeningBracket + 1;
    if (openingBracketsCount == TWO) {
      analyze2SquareBrackets(firstOpeningBracket, reverseCursor);
    } else if (openingBracketsCount == 1) {
      analyze1SquareBracket(firstOpeningBracket, reverseCursor);
    }
  }

  private void analyze1SquareBracket(
      final int firstOpeningBracket, final TextBrowser.ReverseCursor reverseCursor) {
    final TextBrowser.Cursor cursor = reverseCursor.toCursor();
    cursor.moveNext();
    final String target = extractTarget(cursor, UNAUTHORIZED_AFTER_1_OPEN, AUTHORIZED_AFTER_1_OPEN);
    if (target == null) {
      return;
    }
    final String value = extractValue(cursor, ' ', UNAUTHORIZED_AFTER_1_OPEN_AND_SEPARATOR);
    if (cursor.getIndex() >= text.length() || text.charAt(cursor.getIndex()) != CLOSE) {
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
    final String value = extractValue(cursor, PIPE, UNAUTHORIZED_AFTER_2_OPEN_AND_SEPARATOR);
    if (cursor.getIndex() >= text.length() || text.charAt(cursor.getIndex()) != CLOSE) {
      return;
    }
    final int firstClosingBracket = cursor.getIndex();
    if (passClosingBrackets(cursor) < TWO) {
      return;
    }
    elementFactory.persist(firstOpeningBracket, firstClosingBracket + TWO, target, value);
    while (reverseCursor.getIndex() >= firstOpeningBracket) {
      reverseCursor.movePrevious();
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
    final boolean knownProtocol =
        protocols.stream().anyMatch(protocol -> target.startsWith(protocol.value()));
    if (knownProtocol) {
      externalLinks.add(new ExternalLinkElement(begin, end, target, value, true));
    }
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
    return text.substring(beginTarget, cursor.getIndex());
  }

  @Nullable
  private String extractValue(
      final TextBrowser.Cursor cursor, final char separator, final String unauthorized) {
    if (cursor.getIndex() >= text.length() || text.charAt(cursor.getIndex()) != separator) {
      return null;
    }
    cursor.moveNext();
    final int beginValue = cursor.getIndex();
    cursor.moveAfterUntil(unauthorized);
    final int cursorIndex = cursor.getIndex();
    if (cursorIndex >= text.length()) {
      return null;
    }
    if (text.charAt(cursorIndex) != CLOSE) {
      return null;
    }
    return text.substring(beginValue, cursorIndex);
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
