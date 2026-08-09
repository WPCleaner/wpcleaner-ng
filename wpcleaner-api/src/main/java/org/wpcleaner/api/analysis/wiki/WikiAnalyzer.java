package org.wpcleaner.api.analysis.wiki;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.ArrayList;
import java.util.List;
import org.wpcleaner.api.analysis.TextBrowser;
import org.wpcleaner.api.analysis.category.CategoryElement;
import org.wpcleaner.api.analysis.internallink.InternalLinkElement;

final class WikiAnalyzer {

  private static final char PIPE = '|';
  private static final char SQUARE_BRACKET_CLOSE = ']';
  private static final char SQUARE_BRACKET_OPEN = '[';
  private static final String UNAUTHORIZED_AFTER_OPEN_BRACKETS =
      new String(new char[] {SQUARE_BRACKET_OPEN, PIPE, SQUARE_BRACKET_CLOSE, '\n'});
  private static final String UNAUTHORIZED_AFTER_OPEN_BRACKETS_AND_PIPE =
      new String(new char[] {SQUARE_BRACKET_OPEN, SQUARE_BRACKET_CLOSE});
  private static final int TWO = 2;
  private static final String CATEGORY_IDENTIFIER = "Catégorie";

  private final String text;
  private final TextBrowser textBrowser;
  private final List<InternalLinkElement> internalLinks;
  private final List<CategoryElement> categories;

  WikiAnalyzer(final String text, final TextBrowser textBrowser) {
    this.text = text;
    this.textBrowser = textBrowser;
    this.internalLinks = new ArrayList<>();
    this.categories = new ArrayList<>();
  }

  void analyze() {
    final TextBrowser.ReverseCursor cursor = textBrowser.reverseCursor();
    while (cursor.getIndex() >= 0) {
      final int currentIndex = cursor.getIndex();
      final char currentChar = text.charAt(cursor.getIndex());
      switch (currentChar) {
        case SQUARE_BRACKET_OPEN -> analyzeSquareBracket(cursor);
        case '{' -> analyzeCurlyBracket(cursor);
        case '=' -> analyzeEqual(cursor);
        case '_' -> analyzeUnderscore(cursor);
        default -> {}
      }
      if (cursor.getIndex() >= currentIndex) {
        cursor.movePrevious();
      }
    }
  }

  List<InternalLinkElement> getInternalLinks() {
    return internalLinks;
  }

  List<CategoryElement> getCategories() {
    return categories;
  }

  private void analyzeSquareBracket(final TextBrowser.ReverseCursor reverseCursor) {
    final int lastOpeningBracket = reverseCursor.getIndex();
    final TextBrowser.ReverseCursor temporaryCursor = reverseCursor.copy();
    int firstOpeningBracket = lastOpeningBracket;
    while (temporaryCursor.getIndex() >= 0
        && text.charAt(temporaryCursor.getIndex()) == SQUARE_BRACKET_OPEN
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
    final int beginTarget = cursor.getIndex();
    cursor.moveAfterUntil(UNAUTHORIZED_AFTER_OPEN_BRACKETS);
    final String target = text.substring(beginTarget, cursor.getIndex());
    if (cursor.getIndex() < text.length() && text.charAt(cursor.getIndex()) == PIPE) {
      cursor.moveNext();
      cursor.moveAfterUntil(UNAUTHORIZED_AFTER_OPEN_BRACKETS_AND_PIPE);
    }
    if (cursor.getIndex() >= text.length()
        || text.charAt(cursor.getIndex()) != SQUARE_BRACKET_CLOSE) {
      return;
    }
    final int firstClosingBracket = cursor.getIndex();
    cursor.moveAfterWhile(SQUARE_BRACKET_CLOSE);
    if (cursor.getIndex() - firstClosingBracket != TWO) {
      return;
    }
    final int column = target.indexOf(':');
    if (column > 0) {
      if (CATEGORY_IDENTIFIER.equalsIgnoreCase(target.substring(0, column))) {
        categories.add(new CategoryElement(firstOpeningBracket, cursor.getIndex()));
      } else {
        internalLinks.add(new InternalLinkElement(firstOpeningBracket, cursor.getIndex()));
      }
    } else {
      internalLinks.add(new InternalLinkElement(firstOpeningBracket, cursor.getIndex()));
    }
    while (reverseCursor.getIndex() >= firstOpeningBracket) {
      reverseCursor.movePrevious();
    }
  }

  @SuppressWarnings({"unused", "UnusedVariable"})
  private void analyzeCurlyBracket(final TextBrowser.ReverseCursor cursor) {
    // TODO
  }

  @SuppressWarnings("unused")
  private void analyzeEqual(final TextBrowser.ReverseCursor cursor) {
    // TODO
  }

  @SuppressWarnings("unused")
  private void analyzeUnderscore(final TextBrowser.ReverseCursor cursor) {
    // TODO
  }
}
