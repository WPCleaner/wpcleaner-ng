package org.wpcleaner.api.analysis.wiki;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.ArrayList;
import java.util.List;
import org.wpcleaner.api.analysis.TextBrowser;
import org.wpcleaner.api.analysis.internallink.InternalLinkElement;

final class WikiAnalyzer {

  private final String text;
  private final TextBrowser textBrowser;
  private final List<InternalLinkElement> internalLinks;

  WikiAnalyzer(final String text, final TextBrowser textBrowser) {
    this.text = text;
    this.textBrowser = textBrowser;
    this.internalLinks = new ArrayList<>();
  }

  void analyze() {
    final TextBrowser.ReverseCursor cursor = textBrowser.reverseCursor();
    while (cursor.getIndex() >= 0) {
      final int currentIndex = cursor.getIndex();
      final char currentChar = text.charAt(cursor.getIndex());
      switch (currentChar) {
        case '[' -> analyzeSquareBracket(cursor);
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

  private void analyzeSquareBracket(final TextBrowser.ReverseCursor reverseCursor) {
    final int lastOpeningBracket = reverseCursor.getIndex();
    final TextBrowser.ReverseCursor temporaryCursor = reverseCursor.copy();
    int firstOpeningBracket = lastOpeningBracket;
    while (temporaryCursor.getIndex() >= 0
        && text.charAt(temporaryCursor.getIndex()) == InternalLinkElement.TOKEN_START
        && temporaryCursor.samePart()) {
      firstOpeningBracket = temporaryCursor.getIndex();
      temporaryCursor.movePrevious();
    }
    if (lastOpeningBracket + 1 - firstOpeningBracket != InternalLinkElement.START_COUNT) {
      return;
    }
    final TextBrowser.Cursor cursor = reverseCursor.toCursor();
    cursor.moveAfterUntil("|]\n");
    if (text.charAt(cursor.getIndex()) == InternalLinkElement.TOKEN_SEPARATOR) {
      cursor.moveNext();
      cursor.moveAfterUntil("]");
    }
    if (text.charAt(cursor.getIndex()) != InternalLinkElement.TOKEN_END) {
      return;
    }
    final int firstClosingBracket = cursor.getIndex();
    cursor.moveAfterWhile("]");
    if (cursor.getIndex() - firstClosingBracket != InternalLinkElement.END_COUNT) {
      return;
    }
    internalLinks.add(new InternalLinkElement(firstOpeningBracket, cursor.getIndex()));
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
