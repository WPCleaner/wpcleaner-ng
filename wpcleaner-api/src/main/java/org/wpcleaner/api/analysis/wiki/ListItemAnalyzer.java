package org.wpcleaner.api.analysis.wiki;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.ArrayList;
import java.util.List;
import org.wpcleaner.api.analysis.listitem.ListItemElement;

final class ListItemAnalyzer {

  private static final String PREFIX_CHARACTERS = "*#:;";

  private final String text;
  private final LineAnalyzer lineAnalyzer;
  private final List<ListItemElement> listItems;

  ListItemAnalyzer(final String text, final LineAnalyzer lineAnalyzer) {
    this.text = text;
    this.lineAnalyzer = lineAnalyzer;
    this.listItems = new ArrayList<>();
  }

  List<ListItemElement> getListItems() {
    return listItems;
  }

  void analyze() {
    lineAnalyzer.getLines().forEach(this::analyzeLine);
  }

  private void analyzeLine(final LineAnalyzer.Line line) {
    int prefixEnd = line.contentBegin();
    while (prefixEnd < line.end() && PREFIX_CHARACTERS.indexOf(text.charAt(prefixEnd)) >= 0) {
      prefixEnd++;
    }
    if (prefixEnd == line.contentBegin()) {
      return;
    }
    listItems.add(
        new ListItemElement(
            line.contentBegin(),
            line.end(),
            text.substring(line.contentBegin(), prefixEnd),
            text.substring(prefixEnd, line.end()).trim()));
  }
}
