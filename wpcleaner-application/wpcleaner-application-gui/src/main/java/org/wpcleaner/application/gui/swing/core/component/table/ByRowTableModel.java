package org.wpcleaner.application.gui.swing.core.component.table;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.io.Serial;
import java.util.List;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

public class ByRowTableModel<C> extends AbstractTableModel {

  @Serial private static final long serialVersionUID = -7839693368518467542L;

  private final transient List<ColumnInformation<C, ?>> columns;
  private final transient List<C> values;

  protected ByRowTableModel(final List<ColumnInformation<C, ?>> columns, final List<C> values) {
    this.columns = columns;
    this.values = values;
  }

  @Override
  public int getRowCount() {
    return values.size();
  }

  @Override
  public int getColumnCount() {
    return columns.size();
  }

  public void configureColumns(final JTable table) {
    for (int columnIndex = 0; columnIndex < columns.size(); columnIndex++) {
      final int currentIndex = columnIndex;
      columns
          .get(currentIndex)
          .configurers()
          .forEach(configurer -> configurer.accept(table, currentIndex));
    }
  }

  @Override
  public String getColumnName(final int columnIndex) {
    return getColumn(columnIndex).title();
  }

  @Override
  public Object getValueAt(final int rowIndex, final int columnIndex) {
    return getColumn(columnIndex).format(getValue(rowIndex));
  }

  private ColumnInformation<C, ?> getColumn(final int columnIndex) {
    if (columnIndex < 0 || columnIndex >= columns.size()) {
      throw new IllegalArgumentException(
          "Invalid column index %s/%s".formatted(columnIndex, columns.size()));
    }
    return columns.get(columnIndex);
  }

  private C getValue(final int rowIndex) {
    if (rowIndex < 0 || rowIndex >= values.size()) {
      throw new IllegalArgumentException(
          "Invalid row index %s/%s".formatted(rowIndex, values.size()));
    }
    return values.get(rowIndex);
  }
}
