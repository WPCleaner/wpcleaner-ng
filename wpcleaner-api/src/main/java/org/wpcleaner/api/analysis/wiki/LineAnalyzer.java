package org.wpcleaner.api.analysis.wiki;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.stream.Stream;
import org.wpcleaner.api.analysis.TextBrowser;
import org.wpcleaner.api.analysis.preprocessor.PreprocessorContainer;
import org.wpcleaner.api.analysis.tag.CompleteTagElement;
import org.wpcleaner.api.analysis.template.TemplateParameter;

final class LineAnalyzer {

  private static final char NEW_LINE = '\n';

  private final String text;
  private final TextBrowser textBrowser;
  private final NavigableMap<Integer, Integer> skippedElementEnds;
  private final List<Area> containers;
  private final List<Line> lines;

  LineAnalyzer(
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
                    .map(LineAnalyzer::toArea),
                preprocessor.getFunctions().stream()
                    .flatMap(function -> function.parameters().stream())
                    .map(LineAnalyzer::toArea),
                preprocessor.getCompleteTags().stream().map(LineAnalyzer::toContentArea))
            .flatMap(areas -> areas)
            .toList();
    this.lines = new ArrayList<>();
    analyze();
  }

  List<Line> getLines() {
    return lines;
  }

  int indexOf(final String token, final int begin, final int end) {
    TextBrowser.Cursor cursor = textBrowser.cursor(begin);
    int scanned = begin;
    while (cursor.getIndex() + token.length() <= end) {
      final int index = cursor.getIndex();
      final int skippedElementEnd = findSkippedElementEnd(scanned, index, end);
      if (skippedElementEnd > index) {
        cursor = textBrowser.cursor(skippedElementEnd);
        scanned = skippedElementEnd;
      } else if (text.startsWith(token, index) && textBrowser.isContinuous(index, token.length())) {
        return index;
      } else {
        scanned = index + 1;
        cursor.moveNext();
      }
    }
    return -1;
  }

  private void analyze() {
    final Area wholeText = new Area(0, text.length());
    int lineBegin = 0;
    while (lineBegin >= 0 && lineBegin < text.length()) {
      final Area container = findContainer(lineBegin, wholeText);
      if (textBrowser.cursor(lineBegin).getIndex() == lineBegin
          && !isInsideSkippedElement(lineBegin, container)) {
        lines.add(new Line(lineBegin, findLineEnd(lineBegin, container.end()), container));
      }
      final int newLine = text.indexOf(NEW_LINE, lineBegin);
      lineBegin = newLine < 0 ? -1 : newLine + 1;
    }
  }

  private Area findContainer(final int lineBegin, final Area wholeText) {
    return containers.stream()
        .filter(container -> container.begin() <= lineBegin && lineBegin < container.end())
        .min(Comparator.comparingInt(Area::end))
        .orElse(wholeText);
  }

  private boolean isInsideSkippedElement(final int lineBegin, final Area container) {
    return skippedElementEnds.subMap(container.begin(), true, lineBegin, false).values().stream()
        .anyMatch(
            skippedElementEnd ->
                skippedElementEnd > lineBegin && skippedElementEnd <= container.end());
  }

  private int findLineEnd(final int lineBegin, final int containerEnd) {
    TextBrowser.Cursor cursor = textBrowser.cursor(lineBegin);
    int scanned = lineBegin;
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

  record Area(int begin, int end) {}

  record Line(int begin, int end, Area container) {}
}
