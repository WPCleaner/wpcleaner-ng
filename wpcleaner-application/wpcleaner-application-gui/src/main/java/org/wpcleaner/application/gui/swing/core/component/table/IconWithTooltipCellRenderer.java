package org.wpcleaner.application.gui.swing.core.component.table;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.awt.Component;
import java.io.Serial;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import org.jspecify.annotations.Nullable;

public class IconWithTooltipCellRenderer extends DefaultTableCellRenderer {

  @Serial private static final long serialVersionUID = 1L;

  @Nullable private final ImageIcon icon;
  private final boolean displayIfEmpty;

  public IconWithTooltipCellRenderer(@Nullable final ImageIcon icon, final boolean displayIfEmpty) {
    super();
    this.icon = icon;
    this.displayIfEmpty = displayIfEmpty;
  }

  @Override
  public Component getTableCellRendererComponent(
      final JTable table,
      @Nullable final Object value,
      final boolean isSelected,
      final boolean hasFocus,
      final int row,
      final int column) {
    if (icon != null) {
      final String textValue = value != null ? value.toString() : "";
      if (displayIfEmpty || !textValue.isEmpty()) {
        final JLabel result = new JLabel(icon);
        result.setToolTipText(textValue);
        return result;
      }
    }
    return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
  }
}
