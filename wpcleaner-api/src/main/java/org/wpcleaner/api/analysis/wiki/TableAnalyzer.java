package org.wpcleaner.api.analysis.wiki;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.api.analysis.Element;
import org.wpcleaner.api.analysis.table.TableElement;

final class TableAnalyzer {

  private static final String TABLE_START = "{|";
  private static final String TABLE_END = "|}";
  private static final String ROW = "|-";
  private static final String CAPTION = "|+";
  private static final String CELL_SEPARATOR = "||";
  private static final String HEADER_CELL_SEPARATOR = "!!";
  private static final String ATTRIBUTES_SEPARATOR = "|";
  private static final String LINK_START = "[[";
  private static final char CELL = '|';
  private static final char HEADER_CELL = '!';
  private static final char INDENTATION = ':';
  private static final char DASH = '-';
  private static final int TWO = 2;

  private final String text;
  private final LineAnalyzer lineAnalyzer;
  private final List<TableElement> tables;

  TableAnalyzer(final String text, final LineAnalyzer lineAnalyzer) {
    this.text = text;
    this.lineAnalyzer = lineAnalyzer;
    this.tables = new ArrayList<>();
  }

  List<TableElement> getTables() {
    return tables;
  }

  void analyze() {
    final Map<Area, List<LineAnalyzer.Line>> linesByContainer =
        lineAnalyzer.getLines().stream()
            .collect(
                Collectors.groupingBy(
                    LineAnalyzer.Line::container, LinkedHashMap::new, Collectors.toList()));
    linesByContainer.values().forEach(this::analyzeLines);
    tables.sort(Element.COMPARATOR);
  }

  private void analyzeLines(final List<LineAnalyzer.Line> lines) {
    final Deque<TableBuilder> openTables = new ArrayDeque<>();
    for (final LineAnalyzer.Line line : lines) {
      analyzeLine(line, openTables);
    }
    final int end = lines.getLast().end();
    while (!openTables.isEmpty()) {
      tables.add(openTables.pop().build(end));
    }
  }

  private void analyzeLine(final LineAnalyzer.Line line, final Deque<TableBuilder> openTables) {
    final int lineStart = skipWhitespace(line.contentBegin(), line.end());
    final int tableStart = findTableStart(lineStart, line.end());
    final TableBuilder table = openTables.peek();
    if (tableStart >= 0) {
      extendCurrentItem(table, line.end());
      openTables.push(
          new TableBuilder(
              text,
              tableStart,
              text.substring(tableStart + TABLE_START.length(), line.end()).trim()));
    } else if (table != null && lineStart < line.end()) {
      if (startsWith(TABLE_END, lineStart, line.end())) {
        tables.add(openTables.pop().build(lineStart + TABLE_END.length()));
        extendCurrentItem(openTables.peek(), line.end());
      } else {
        analyzeLineInsideTable(table, lineStart, line.end());
      }
    }
  }

  private void analyzeLineInsideTable(
      final TableBuilder table, final int lineStart, final int lineEnd) {
    if (startsWith(ROW, lineStart, lineEnd)) {
      int attributesBegin = lineStart + 1;
      while (attributesBegin < lineEnd && text.charAt(attributesBegin) == DASH) {
        attributesBegin++;
      }
      table.addRow(lineStart, lineEnd, text.substring(attributesBegin, lineEnd).trim());
    } else if (text.charAt(lineStart) == CELL || text.charAt(lineStart) == HEADER_CELL) {
      analyzeCells(table, lineStart, lineEnd);
    } else {
      table.extendCurrentItem(lineEnd);
    }
  }

  private static void extendCurrentItem(@Nullable final TableBuilder table, final int end) {
    if (table != null) {
      table.extendCurrentItem(end);
    }
  }

  private void analyzeCells(final TableBuilder table, final int lineStart, final int lineEnd) {
    if (startsWith(CAPTION, lineStart, lineEnd)) {
      table.addCaption(createCell(table, lineStart, lineStart + CAPTION.length(), lineEnd, false));
      return;
    }
    final boolean header = text.charAt(lineStart) == HEADER_CELL;
    int cellBegin = lineStart;
    int segmentBegin = lineStart + 1;
    int separator = findCellSeparator(segmentBegin, lineEnd, header);
    while (separator >= 0) {
      table.addCell(createCell(table, cellBegin, segmentBegin, separator, header));
      cellBegin = separator;
      segmentBegin = separator + TWO;
      separator = findCellSeparator(segmentBegin, lineEnd, header);
    }
    table.addCell(createCell(table, cellBegin, segmentBegin, lineEnd, header));
  }

  private int findCellSeparator(final int begin, final int end, final boolean header) {
    final int separator = lineAnalyzer.indexOf(CELL_SEPARATOR, begin, end);
    if (!header) {
      return separator;
    }
    final int headerSeparator = lineAnalyzer.indexOf(HEADER_CELL_SEPARATOR, begin, end);
    if (separator < 0 || (headerSeparator >= 0 && headerSeparator < separator)) {
      return headerSeparator;
    }
    return separator;
  }

  private TableBuilder.CellBuilder createCell(
      final TableBuilder table,
      final int begin,
      final int segmentBegin,
      final int segmentEnd,
      final boolean header) {
    final int attributesEnd = lineAnalyzer.indexOf(ATTRIBUTES_SEPARATOR, segmentBegin, segmentEnd);
    if (attributesEnd < 0 || text.substring(segmentBegin, attributesEnd).contains(LINK_START)) {
      return table.newCell(begin, segmentBegin, segmentEnd, header, null);
    }
    return table.newCell(
        begin,
        attributesEnd + 1,
        segmentEnd,
        header,
        text.substring(segmentBegin, attributesEnd).trim());
  }

  private int findTableStart(final int lineStart, final int lineEnd) {
    int index = lineStart;
    while (index < lineEnd && text.charAt(index) == INDENTATION) {
      index++;
    }
    index = skipWhitespace(index, lineEnd);
    return startsWith(TABLE_START, index, lineEnd) ? index : -1;
  }

  private boolean startsWith(final String token, final int index, final int end) {
    return index + token.length() <= end && text.startsWith(token, index);
  }

  private int skipWhitespace(final int begin, final int end) {
    int index = begin;
    while (index < end && Character.isWhitespace(text.charAt(index))) {
      index++;
    }
    return index;
  }
}
