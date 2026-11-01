package org.wpcleaner.api.analysis.wiki;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import org.wpcleaner.api.analysis.TextBrowser;
import org.wpcleaner.api.analysis.behaviorswitch.BehaviorSwitchElement;
import org.wpcleaner.api.analysis.category.CategoryElement;
import org.wpcleaner.api.analysis.externallink.ExternalLinkElement;
import org.wpcleaner.api.analysis.image.ImageElement;
import org.wpcleaner.api.analysis.internallink.InternalLinkElement;
import org.wpcleaner.api.analysis.interwikilink.InterwikiLinkElement;
import org.wpcleaner.api.analysis.languagelink.LanguageLinkElement;
import org.wpcleaner.api.analysis.listitem.ListItemElement;
import org.wpcleaner.api.repository.interwiki.Interwiki;
import org.wpcleaner.api.repository.namespace.Namespace;
import org.wpcleaner.api.repository.protocol.Protocol;

final class WikiAnalyzer {

  private final String text;
  private final TextBrowser textBrowser;
  private final SquareBracketAnalyzer squareBracketAnalyzer;
  private final ImageAnalyzer imageAnalyzer;
  private final BehaviorSwitchAnalyzer behaviorSwitchAnalyzer;
  private final ListItemAnalyzer listItemAnalyzer;

  WikiAnalyzer(
      final String text,
      final TextBrowser textBrowser,
      final List<Interwiki> interwikis,
      final List<Namespace> namespaces,
      final List<Protocol> protocols,
      final ImageAnalyzer imageAnalyzer,
      final BehaviorSwitchAnalyzer behaviorSwitchAnalyzer,
      final ListItemAnalyzer listItemAnalyzer) {
    this.text = text;
    this.textBrowser = textBrowser;
    this.imageAnalyzer = imageAnalyzer;
    this.behaviorSwitchAnalyzer = behaviorSwitchAnalyzer;
    this.listItemAnalyzer = listItemAnalyzer;
    this.squareBracketAnalyzer =
        new SquareBracketAnalyzer(text, interwikis, namespaces, protocols, imageAnalyzer);
  }

  void analyze() {
    behaviorSwitchAnalyzer.analyze();
    listItemAnalyzer.analyze();
    final TextBrowser.ReverseCursor cursor = textBrowser.reverseCursor();
    while (cursor.getIndex() >= 0) {
      final int currentIndex = cursor.getIndex();
      if (text.charAt(currentIndex) == SquareBracketAnalyzer.OPEN) {
        squareBracketAnalyzer.analyzeSquareBracket(cursor);
      }
      if (cursor.getIndex() >= currentIndex) {
        cursor.movePrevious();
      }
    }
  }

  List<BehaviorSwitchElement> getBehaviorSwitches() {
    return behaviorSwitchAnalyzer.getBehaviorSwitches();
  }

  List<CategoryElement> getCategories() {
    return squareBracketAnalyzer.getCategories();
  }

  List<ExternalLinkElement> getExternalLinks() {
    return squareBracketAnalyzer.getExternalLinks();
  }

  List<ImageElement> getImages() {
    return imageAnalyzer.getImages();
  }

  List<ListItemElement> getListItems() {
    return listItemAnalyzer.getListItems();
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
}
