package org.wpcleaner.api.analysis.wiki;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.ArrayList;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.stream.Stream;
import org.wpcleaner.api.analysis.TextBrowser;
import org.wpcleaner.api.analysis.listitem.ListItemElement;
import org.wpcleaner.api.analysis.preprocessor.PreprocessorContainer;
import org.wpcleaner.api.analysis.tag.CompleteTagElement;
import org.wpcleaner.api.analysis.template.TemplateParameter;

final class ListItemAnalyzer {

  private static final String PREFIX_CHARACTERS = "*#:;";
  private static final char NEW_LINE = '\n';

  private final String text;
  private final TextBrowser textBrowser;
  private final NavigableMap<Integer, Integer> skippedElementEnds;
  private final List<Area> containers;
  private final List<ListItemElement> listItems;

  ListItemAnalyzer(
      final String text, final TextBrowser textBrowser, final PreprocessorContainer preprocessor) {
    this.text = text;
    this.textBrowser = textBrowser;
    this.skippedElementEnds = new TreeMap<>();
    Stream.concat(
            preprocessor.getCurlyBracketElements().stream(),
            preprocessor.getCompleteTags().stream())
        .forEach(element -> skippedElementEnds.merge(element.begin(), element.end(), Math::max));
    this.containers =
        Stream.of(
                preprocessor.getTemplates().stream()
                    .flatMap(template -> template.parameters().stream())
                    .map(ListItemAnalyzer::toArea),
                preprocessor.getFunctions().stream()
                    .flatMap(function -> function.parameters().stream())
                    .map(ListItemAnalyzer::toArea),
                preprocessor.getCompleteTags().stream().map(ListItemAnalyzer::toContentArea))
            .flatMap(areas -> areas)
            .toList();
    this.listItems = new ArrayList<>();
  }

  List<ListItemElement> getListItems() {
    return listItems;
  }

  void analyze() {
    int lineBegin = 0;
    while (lineBegin >= 0 && lineBegin < text.length()) {
      analyzeLine(lineBegin);
      final int newLine = text.indexOf(NEW_LINE, lineBegin);
      lineBegin = newLine < 0 ? -1 : newLine + 1;
    }
  }

  private void analyzeLine(final int lineBegin) {
    if (textBrowser.cursor(lineBegin).getIndex() != lineBegin) {
      return;
    }
    final int prefixEnd = findPrefixEnd(lineBegin);
    if (prefixEnd == lineBegin) {
      return;
    }
    final int lineEnd = findLineEnd(lineBegin, prefixEnd);
    listItems.add(
        new ListItemElement(
            lineBegin,
            lineEnd,
            text.substring(lineBegin, prefixEnd),
            text.substring(prefixEnd, lineEnd).trim()));
  }

  private int findPrefixEnd(final int lineBegin) {
    int prefixEnd = lineBegin;
    while (prefixEnd < text.length() && PREFIX_CHARACTERS.indexOf(text.charAt(prefixEnd)) >= 0) {
      prefixEnd++;
    }
    return prefixEnd;
  }

  private int findLineEnd(final int lineBegin, final int prefixEnd) {
    final int containerEnd =
        containers.stream()
            .filter(container -> container.begin() <= lineBegin && lineBegin < container.end())
            .mapToInt(Area::end)
            .min()
            .orElse(text.length());
    TextBrowser.Cursor cursor = textBrowser.cursor(prefixEnd);
    int scanned = prefixEnd;
    while (cursor.getIndex() < containerEnd) {
      final int index = cursor.getIndex();
      final int skippedElementEnd = findSkippedElementEnd(scanned, index, containerEnd);
      if (skippedElementEnd > index) {
        cursor = textBrowser.cursor(skippedElementEnd);
        scanned = skippedElementEnd;
      } else if (text.charAt(index) == NEW_LINE) {
        return index;
      } else {
        scanned = index + 1;
        cursor.moveNext();
      }
    }
    return Math.min(cursor.getIndex(), containerEnd);
  }

  private int findSkippedElementEnd(final int from, final int to, final int limit) {
    return skippedElementEnds.subMap(from, true, to, true).values().stream()
        .mapToInt(Integer::intValue)
        .filter(skippedElementEnd -> skippedElementEnd <= limit)
        .max()
        .orElse(-1);
  }

  private static Area toArea(final TemplateParameter parameter) {
    return new Area(parameter.begin(), parameter.end());
  }

  private static Area toContentArea(final CompleteTagElement completeTag) {
    final int contentBegin = completeTag.startTag().end();
    final int contentEnd = completeTag.endTag().begin();
    return new Area(contentBegin, contentEnd);
  }

  private record Area(int begin, int end) {}
}
