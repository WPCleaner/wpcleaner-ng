package org.wpcleaner.api.analysis.wiki;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import org.wpcleaner.api.analysis.TextBrowser;
import org.wpcleaner.api.analysis.category.CategoryElement;
import org.wpcleaner.api.analysis.internallink.InternalLinkElement;
import org.wpcleaner.api.repository.namespace.Namespace;

final class WikiAnalyzer {

  private final String text;
  private final TextBrowser textBrowser;
  private final SquareBracketAnalyzer squareBracketAnalyzer;

  WikiAnalyzer(final String text, final TextBrowser textBrowser, final List<Namespace> namespaces) {
    this.text = text;
    this.textBrowser = textBrowser;
    this.squareBracketAnalyzer = new SquareBracketAnalyzer(text, namespaces);
  }

  void analyze() {
    final TextBrowser.ReverseCursor cursor = textBrowser.reverseCursor();
    while (cursor.getIndex() >= 0) {
      final int currentIndex = cursor.getIndex();
      final char currentChar = text.charAt(cursor.getIndex());
      switch (currentChar) {
        case SquareBracketAnalyzer.OPEN -> squareBracketAnalyzer.analyzeSquareBracket(cursor);
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
    return squareBracketAnalyzer.getInternalLinks();
  }

  List<CategoryElement> getCategories() {
    return squareBracketAnalyzer.getCategories();
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
