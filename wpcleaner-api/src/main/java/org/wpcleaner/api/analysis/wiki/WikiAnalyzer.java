package org.wpcleaner.api.analysis.wiki;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import org.wpcleaner.api.analysis.Element;
import org.wpcleaner.api.analysis.TextBrowser;
import org.wpcleaner.api.analysis.behaviorswitch.BehaviorSwitchElement;
import org.wpcleaner.api.analysis.category.CategoryElement;
import org.wpcleaner.api.analysis.externallink.ExternalLinkElement;
import org.wpcleaner.api.analysis.image.ImageElement;
import org.wpcleaner.api.analysis.internallink.InternalLinkElement;
import org.wpcleaner.api.analysis.interwikilink.InterwikiLinkElement;
import org.wpcleaner.api.analysis.languagelink.LanguageLinkElement;
import org.wpcleaner.api.analysis.listitem.ListItemElement;
import org.wpcleaner.api.analysis.paragraph.ParagraphElement;
import org.wpcleaner.api.analysis.preformatted.PreformattedElement;
import org.wpcleaner.api.analysis.table.TableElement;

final class WikiAnalyzer {

  private final String text;
  private final TextBrowser textBrowser;
  private final SquareBracketAnalyzer squareBracketAnalyzer;
  private final ImageAnalyzer imageAnalyzer;
  private final BehaviorSwitchAnalyzer behaviorSwitchAnalyzer;
  private final BlockLevelAnalyzer blockLevelAnalyzer;

  WikiAnalyzer(
      final String text,
      final TextBrowser textBrowser,
      final SquareBracketAnalyzer squareBracketAnalyzer,
      final ImageAnalyzer imageAnalyzer,
      final BehaviorSwitchAnalyzer behaviorSwitchAnalyzer,
      final BlockLevelAnalyzer blockLevelAnalyzer) {
    this.text = text;
    this.textBrowser = textBrowser;
    this.imageAnalyzer = imageAnalyzer;
    this.behaviorSwitchAnalyzer = behaviorSwitchAnalyzer;
    this.blockLevelAnalyzer = blockLevelAnalyzer;
    this.squareBracketAnalyzer = squareBracketAnalyzer;
  }

  void analyze() {
    behaviorSwitchAnalyzer.analyze();
    blockLevelAnalyzer.analyze();
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

  void analyzeDefinitions(final List<? extends Element> skippedElements) {
    blockLevelAnalyzer.analyzeDefinitions(skippedElements);
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
    return blockLevelAnalyzer.getListItems();
  }

  List<PreformattedElement> getPreformattedBlocks() {
    return blockLevelAnalyzer.getPreformattedBlocks();
  }

  List<ParagraphElement> getParagraphs() {
    return blockLevelAnalyzer.getParagraphs();
  }

  List<TableElement> getTables() {
    return blockLevelAnalyzer.getTables();
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
