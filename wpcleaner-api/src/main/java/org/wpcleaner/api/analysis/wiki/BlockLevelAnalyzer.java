package org.wpcleaner.api.analysis.wiki;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import org.wpcleaner.api.analysis.TextBrowser;
import org.wpcleaner.api.analysis.listitem.ListItemElement;
import org.wpcleaner.api.analysis.paragraph.ParagraphElement;
import org.wpcleaner.api.analysis.preformatted.PreformattedElement;
import org.wpcleaner.api.analysis.preprocessor.PreprocessorContainer;
import org.wpcleaner.api.analysis.table.TableElement;
import org.wpcleaner.api.analysis.tag.TagElement;

final class BlockLevelAnalyzer {

  private final ListItemAnalyzer listItemAnalyzer;
  private final TableAnalyzer tableAnalyzer;
  private final ParagraphAnalyzer paragraphAnalyzer;

  BlockLevelAnalyzer(
      final String text,
      final TextBrowser textBrowser,
      final PreprocessorContainer preprocessor,
      final TextContainers containers,
      final List<TagElement> tags,
      final int firstLineBegin) {
    final LineAnalyzer lineAnalyzer =
        new LineAnalyzer(text, textBrowser, preprocessor, containers, firstLineBegin);
    this.listItemAnalyzer = new ListItemAnalyzer(text, lineAnalyzer);
    this.tableAnalyzer = new TableAnalyzer(text, lineAnalyzer);
    this.paragraphAnalyzer =
        new ParagraphAnalyzer(
            text, lineAnalyzer, containers, new BlockTagMatcher(tags), preprocessor.getTitles());
  }

  void analyze() {
    listItemAnalyzer.analyze();
    tableAnalyzer.analyze();
    paragraphAnalyzer.analyze(tableAnalyzer.getTables());
  }

  List<ListItemElement> getListItems() {
    return listItemAnalyzer.getListItems();
  }

  List<ParagraphElement> getParagraphs() {
    return paragraphAnalyzer.getParagraphs();
  }

  List<PreformattedElement> getPreformattedBlocks() {
    return paragraphAnalyzer.getPreformattedBlocks();
  }

  List<TableElement> getTables() {
    return tableAnalyzer.getTables();
  }
}
