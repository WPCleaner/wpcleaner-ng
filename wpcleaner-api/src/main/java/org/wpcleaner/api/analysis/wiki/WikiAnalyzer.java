package org.wpcleaner.api.analysis.wiki;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import org.wpcleaner.api.analysis.TextBrowser;
import org.wpcleaner.api.analysis.category.CategoryElement;
import org.wpcleaner.api.analysis.externallink.ExternalLinkElement;
import org.wpcleaner.api.analysis.internallink.InternalLinkElement;
import org.wpcleaner.api.analysis.interwikilink.InterwikiLinkElement;
import org.wpcleaner.api.analysis.languagelink.LanguageLinkElement;
import org.wpcleaner.api.repository.interwiki.Interwiki;
import org.wpcleaner.api.repository.namespace.Namespace;
import org.wpcleaner.api.repository.protocol.Protocol;

final class WikiAnalyzer {

  private final String text;
  private final TextBrowser textBrowser;
  private final SquareBracketAnalyzer squareBracketAnalyzer;

  WikiAnalyzer(
      final String text,
      final TextBrowser textBrowser,
      final List<Interwiki> interwikis,
      final List<Namespace> namespaces,
      final List<Protocol> protocols) {
    this.text = text;
    this.textBrowser = textBrowser;
    this.squareBracketAnalyzer = new SquareBracketAnalyzer(text, interwikis, namespaces, protocols);
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

  List<CategoryElement> getCategories() {
    return squareBracketAnalyzer.getCategories();
  }

  List<ExternalLinkElement> getExternalLinks() {
    return squareBracketAnalyzer.getExternalLinks();
  }

  List<InternalLinkElement> getInternalLinks() {
    return squareBracketAnalyzer.getInternalLinks();
  }

  List<InterwikiLinkElement> getInterwikiLinks() {
    return squareBracketAnalyzer.getInterwikiLinks();
  }

  List<LanguageLinkElement> getLanguageLinks() {
    return squareBracketAnalyzer.getLanguageLinks();
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
