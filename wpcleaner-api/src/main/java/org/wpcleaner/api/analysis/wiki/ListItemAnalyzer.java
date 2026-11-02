package org.wpcleaner.api.analysis.wiki;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.wpcleaner.api.analysis.Element;
import org.wpcleaner.api.analysis.listitem.ListItemDefinition;
import org.wpcleaner.api.analysis.listitem.ListItemElement;

final class ListItemAnalyzer {

  private static final String PREFIX_CHARACTERS = "*#:;";
  private static final char TERM = ';';
  private static final char DEFINITION = ':';

  private final String text;
  private final LineAnalyzer lineAnalyzer;
  private final List<ListItemElement> listItems;
  private final Set<ListItemElement> termItems;
  private final Map<Area, String> lastPrefixes;

  ListItemAnalyzer(final String text, final LineAnalyzer lineAnalyzer) {
    this.text = text;
    this.lineAnalyzer = lineAnalyzer;
    this.listItems = new ArrayList<>();
    this.termItems = new HashSet<>();
    this.lastPrefixes = new HashMap<>();
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
    final String prefix = text.substring(line.contentBegin(), prefixEnd);
    final String definitionPrefix = prefix.replace(TERM, DEFINITION);
    final String lastPrefix = lastPrefixes.put(line.container(), definitionPrefix);
    if (prefix.isEmpty()) {
      return;
    }
    final ListItemElement listItem =
        new ListItemElement(
            line.contentBegin(), line.end(), prefix, text.substring(prefixEnd, line.end()).trim());
    listItems.add(listItem);
    final boolean opensTerm =
        definitionPrefix.equals(lastPrefix)
            ? prefix.charAt(prefix.length() - 1) == TERM
            : prefix.indexOf(TERM) >= 0;
    if (opensTerm) {
      termItems.add(listItem);
    }
  }

  void analyzeDefinitions(final List<? extends Element> skippedElements) {
    final SortedAreas skippedAreas =
        new SortedAreas(
            skippedElements.stream()
                .map(element -> new Area(element.begin(), element.end()))
                .toList());
    listItems.replaceAll(
        listItem -> termItems.contains(listItem) ? splitTerm(listItem, skippedAreas) : listItem);
  }

  private ListItemElement splitTerm(
      final ListItemElement listItem, final SortedAreas skippedAreas) {
    final int termBegin = listItem.begin() + listItem.prefix().length();
    int colon = text.indexOf(DEFINITION, termBegin);
    while (colon >= 0 && colon < listItem.end() && skippedAreas.overlaps(colon, colon + 1)) {
      colon = text.indexOf(DEFINITION, colon + 1);
    }
    if (colon < 0 || colon >= listItem.end()) {
      return listItem;
    }
    return new ListItemElement(
        listItem.begin(),
        listItem.end(),
        listItem.prefix(),
        text.substring(termBegin, colon).trim(),
        new ListItemDefinition(
            colon, listItem.end(), text.substring(colon + 1, listItem.end()).trim()));
  }
}
