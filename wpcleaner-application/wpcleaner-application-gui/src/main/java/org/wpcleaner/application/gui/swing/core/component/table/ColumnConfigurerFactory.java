package org.wpcleaner.application.gui.swing.core.component.table;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.function.BiConsumer;
import javax.swing.JTable;
import javax.swing.table.TableColumn;
import org.springframework.stereotype.Component;
import org.wpcleaner.lib.image.ImageCollection;

@Component
public final class ColumnConfigurerFactory {

  private final CellRendererFactory cellRendererFactory;

  public ColumnConfigurerFactory(final CellRendererFactory cellRendererFactory) {
    this.cellRendererFactory = cellRendererFactory;
  }

  public BiConsumer<JTable, Integer> minWidth(final int min) {
    return (table, column) -> table.getColumnModel().getColumn(column).setMinWidth(min);
  }

  public BiConsumer<JTable, Integer> timeField() {
    return (table, column) -> {
      table.getColumnModel().getColumn(column).setMinWidth(60);
      table.getColumnModel().getColumn(column).setMaxWidth(60);
    };
  }

  public BiConsumer<JTable, Integer> iconWithTooltip(final ImageCollection image) {
    return (table, column) -> {
      final TableColumn columnModel = table.getColumnModel().getColumn(column);
      columnModel.setCellRenderer(cellRendererFactory.iconWithTooltip(image, false));
      columnModel.setMinWidth(22);
      columnModel.setMaxWidth(22);
      columnModel.setHeaderRenderer(cellRendererFactory.iconWithTooltip(image, true));
    };
  }
}
