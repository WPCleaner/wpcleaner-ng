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
import org.wpcleaner.api.analysis.internallink.InternalLinkElement;
import org.wpcleaner.api.repository.namespace.CommonNamespaces;
import org.wpcleaner.api.repository.namespace.Namespace;

final class SquareBracketAnalyzer {

  static final char CLOSE = ']';
  static final char OPEN = '[';
  static final char PIPE = '|';
  private static final String UNAUTHORIZED_AFTER_OPEN_AND_PIPE =
      new String(new char[] {OPEN, CLOSE});
  private static final String UNAUTHORIZED_AFTER_OPEN =
      new String(new char[] {OPEN, PIPE, CLOSE, '\n'});
  private static final int TWO = 2;

  private final String text;
  @Nullable private final Namespace categoryNamespace;
  private final List<InternalLinkElement> internalLinks;
  private final List<CategoryElement> categories;

  SquareBracketAnalyzer(final String text, final List<Namespace> namespaces) {
    this.text = text;
    this.categoryNamespace =
        Namespace.findNamespace(namespaces, CommonNamespaces.CATEGORY.id).orElse(null);
    this.internalLinks = new ArrayList<>();
    this.categories = new ArrayList<>();
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
    }
  }

  private void analyze2SquareBrackets(
      final int firstOpeningBracket, final TextBrowser.ReverseCursor reverseCursor) {
    final TextBrowser.Cursor cursor = reverseCursor.toCursor();
    cursor.moveNext();
    final String target = extractTarget(cursor);
    if (target == null) {
      return;
    }
    extractValue(cursor);
    if (cursor.getIndex() >= text.length() || text.charAt(cursor.getIndex()) != CLOSE) {
      return;
    }
    final int firstClosingBracket = cursor.getIndex();
    cursor.moveAfterWhile(CLOSE);
    if (cursor.getIndex() - firstClosingBracket != TWO) {
      return;
    }
    persistResult(firstOpeningBracket, cursor.getIndex(), target);
    while (reverseCursor.getIndex() >= firstOpeningBracket) {
      reverseCursor.movePrevious();
    }
  }

  private void persistResult(final int begin, final int end, final String target) {
    final int column = target.indexOf(':');
    if (column <= 0) {
      internalLinks.add(new InternalLinkElement(begin, end));
      return;
    }
    final String beforeColumn = target.substring(0, column);
    if (categoryNamespace != null && categoryNamespace.isPossibleName(beforeColumn)) {
      categories.add(new CategoryElement(begin, end));
    } else {
      internalLinks.add(new InternalLinkElement(begin, end));
    }
  }

  @Nullable
  private String extractTarget(final TextBrowser.Cursor cursor) {
    final int beginTarget = cursor.getIndex();
    cursor.moveAfterUntil(UNAUTHORIZED_AFTER_OPEN);
    final int cursorIndex = cursor.getIndex();
    if (cursorIndex >= text.length()) {
      return null;
    }
    final char nextChar = text.charAt(cursorIndex);
    if (nextChar != PIPE && nextChar != CLOSE) {
      return null;
    }
    return text.substring(beginTarget, cursor.getIndex());
  }

  @Nullable
  private String extractValue(final TextBrowser.Cursor cursor) {
    if (cursor.getIndex() >= text.length() || text.charAt(cursor.getIndex()) != PIPE) {
      return null;
    }
    cursor.moveNext();
    final int beginValue = cursor.getIndex();
    cursor.moveAfterUntil(UNAUTHORIZED_AFTER_OPEN_AND_PIPE);
    final int cursorIndex = cursor.getIndex();
    if (cursorIndex >= text.length()) {
      return null;
    }
    if (text.charAt(cursorIndex) != CLOSE) {
      return null;
    }
    return text.substring(beginValue, cursorIndex);
  }

  List<InternalLinkElement> getInternalLinks() {
    return internalLinks;
  }

  List<CategoryElement> getCategories() {
    return categories;
  }
}
