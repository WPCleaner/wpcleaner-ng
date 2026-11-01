package org.wpcleaner.api.analysis.wiki;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.ArrayList;
import java.util.List;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.api.analysis.table.TableCaption;
import org.wpcleaner.api.analysis.table.TableCell;
import org.wpcleaner.api.analysis.table.TableElement;
import org.wpcleaner.api.analysis.table.TableRow;

final class TableBuilder {

  private final String text;
  private final int begin;
  private final String attributes;
  private final List<RowBuilder> rows;
  @Nullable private CellBuilder caption;
  @Nullable private CellBuilder currentItem;
  private boolean currentItemExtendable;

  TableBuilder(final String text, final int begin, final String attributes) {
    this.text = text;
    this.begin = begin;
    this.attributes = attributes;
    this.rows = new ArrayList<>();
  }

  void addRow(final int rowBegin, final int rowEnd, final String rowAttributes) {
    rows.add(new RowBuilder(rowBegin, rowEnd, rowAttributes));
    currentItemExtendable = false;
  }

  void addCaption(final CellBuilder newCaption) {
    caption = newCaption;
    currentItem = newCaption;
    currentItemExtendable = true;
  }

  void addCell(final CellBuilder cell) {
    if (rows.isEmpty()) {
      rows.add(new RowBuilder(cell.begin, cell.begin, ""));
    }
    rows.getLast().cells.add(cell);
    currentItem = cell;
    currentItemExtendable = true;
  }

  CellBuilder newCell(
      final int cellBegin,
      final int contentBegin,
      final int cellEnd,
      final boolean header,
      @Nullable final String cellAttributes) {
    return new CellBuilder(cellBegin, contentBegin, cellEnd, header, cellAttributes);
  }

  void extendCurrentItem(final int end) {
    if (currentItem != null && currentItemExtendable) {
      currentItem.end = end;
    }
  }

  TableElement build(final int end) {
    final CellBuilder currentCaption = caption;
    return new TableElement(
        begin,
        end,
        attributes,
        currentCaption == null ? null : currentCaption.buildCaption(),
        rows.stream().map(RowBuilder::build).toList());
  }

  private static final class RowBuilder {

    private final int begin;
    private final int end;
    private final String attributes;
    private final List<CellBuilder> cells;

    private RowBuilder(final int begin, final int end, final String attributes) {
      this.begin = begin;
      this.end = end;
      this.attributes = attributes;
      this.cells = new ArrayList<>();
    }

    private TableRow build() {
      final int rowEnd = cells.isEmpty() ? end : Math.max(end, cells.getLast().end);
      return new TableRow(
          begin, rowEnd, attributes, cells.stream().map(CellBuilder::build).toList());
    }
  }

  final class CellBuilder {

    private final int begin;
    private final int contentBegin;
    private final boolean header;
    @Nullable private final String attributes;
    private int end;

    private CellBuilder(
        final int begin,
        final int contentBegin,
        final int end,
        final boolean header,
        @Nullable final String attributes) {
      this.begin = begin;
      this.contentBegin = contentBegin;
      this.end = end;
      this.header = header;
      this.attributes = attributes;
    }

    private TableCell build() {
      return new TableCell(begin, end, header, attributes, content());
    }

    private TableCaption buildCaption() {
      return new TableCaption(begin, end, attributes, content());
    }

    private String content() {
      return text.substring(contentBegin, end).trim();
    }
  }
}
