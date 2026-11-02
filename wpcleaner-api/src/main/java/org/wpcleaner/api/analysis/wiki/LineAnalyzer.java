package org.wpcleaner.api.analysis.wiki;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.wpcleaner.api.analysis.TextBrowser;
import org.wpcleaner.api.analysis.comment.CommentElement;
import org.wpcleaner.api.analysis.preprocessor.PreprocessorContainer;

final class LineAnalyzer {

  private static final char NEW_LINE = '\n';

  private final String text;
  private final TextBrowser textBrowser;
  private final NavigableMap<Integer, Integer> skippedElementEnds;
  private final Map<Integer, Integer> commentEnds;
  private final TextContainers containers;
  private final List<Line> lines;

  LineAnalyzer(
      final String text,
      final TextBrowser textBrowser,
      final PreprocessorContainer preprocessor,
      final TextContainers containers) {
    this.text = text;
    this.textBrowser = textBrowser;
    this.skippedElementEnds = new TreeMap<>();
    Stream.concat(
            preprocessor.getCurlyBracketElements().stream(),
            preprocessor.getCompleteTags().stream())
        .forEach(element -> skippedElementEnds.merge(element.begin(), element.end(), Math::max));
    this.commentEnds =
        preprocessor.getComments().stream()
            .collect(Collectors.toMap(CommentElement::begin, CommentElement::end, Math::max));
    this.containers = containers;
    this.lines = new ArrayList<>();
    analyze(new OpenSkippedElements());
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

  private void analyze(final OpenSkippedElements openSkippedElements) {
    int lineBegin = 0;
    while (lineBegin >= 0 && lineBegin < text.length()) {
      final Area container = containers.findContainer(lineBegin);
      if (!textBrowser.isInsideExclusion(lineBegin)
          && !openSkippedElements.isInsideSkippedElement(lineBegin, container)) {
        final int lineEnd = findLineEnd(lineBegin, container.end());
        lines.add(new Line(lineBegin, skipComments(lineBegin, lineEnd), lineEnd, container));
      }
      final int newLine = text.indexOf(NEW_LINE, lineBegin);
      lineBegin = newLine < 0 ? -1 : newLine + 1;
    }
  }

  private int skipComments(final int lineBegin, final int lineEnd) {
    int contentBegin = lineBegin;
    Integer commentEnd = commentEnds.get(contentBegin);
    while (commentEnd != null && commentEnd <= lineEnd) {
      contentBegin = commentEnd;
      commentEnd = commentEnds.get(contentBegin);
    }
    return contentBegin;
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
    final Integer firstSkippedElement = skippedElementEnds.ceilingKey(from);
    if (firstSkippedElement == null || firstSkippedElement > to) {
      return -1;
    }
    int result = -1;
    for (final int skippedElementEnd :
        skippedElementEnds.subMap(firstSkippedElement, true, to, true).values()) {
      if (skippedElementEnd <= limit) {
        result = Math.max(result, skippedElementEnd);
      }
    }
    return result;
  }

  private final class OpenSkippedElements {

    private final List<Area> skippedElements =
        skippedElementEnds.entrySet().stream()
            .map(skippedElement -> new Area(skippedElement.getKey(), skippedElement.getValue()))
            .toList();
    private final Queue<Area> openElements =
        new PriorityQueue<>(Comparator.comparingInt(Area::end));
    private int nextElement;

    private boolean isInsideSkippedElement(final int lineBegin, final Area container) {
      while (nextElement < skippedElements.size()
          && skippedElements.get(nextElement).begin() < lineBegin) {
        openElements.add(skippedElements.get(nextElement));
        nextElement++;
      }
      Area firstClosed = openElements.peek();
      while (firstClosed != null && firstClosed.end() <= lineBegin) {
        openElements.poll();
        firstClosed = openElements.peek();
      }
      return openElements.stream()
          .anyMatch(
              skippedElement ->
                  skippedElement.begin() >= container.begin()
                      && skippedElement.end() <= container.end());
    }
  }

  record Line(int begin, int contentBegin, int end, Area container) {}
}
