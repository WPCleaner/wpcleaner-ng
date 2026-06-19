package org.wpcleaner.application.gui.swing.recentechanges;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.io.Serial;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.table.AbstractTableModel;
import org.wpcleaner.api.api.query.list.recentchanges.RecentChange;

public final class RecentChangesModel extends AbstractTableModel {

  private static final int COLUMN_TIMESTAMP = 0;
  private static final int COLUMN_TITLE = 1;
  private static final int COLUMN_USER = 2;
  private static final int COLUMN_TAGS = 3;

  @Serial private static final long serialVersionUID = 1L;
  private static final DateTimeFormatter FORMATTER =
      DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
          .withLocale(Locale.getDefault())
          .withZone(ZoneId.systemDefault());

  private final transient List<RecentChange> recentChanges;

  public RecentChangesModel(final List<RecentChange> recentChanges) {
    this.recentChanges = new ArrayList<>(recentChanges);
  }

  @Override
  public int getRowCount() {
    return recentChanges.size();
  }

  @Override
  public int getColumnCount() {
    return 4;
  }

  @Override
  public String getColumnName(final int column) {
    return switch (column) {
      case COLUMN_TIMESTAMP -> "Timestamp";
      case COLUMN_TITLE -> "Title";
      case COLUMN_USER -> "User";
      case COLUMN_TAGS -> "Tags";
      default -> throw new IllegalArgumentException("Unknown column: " + column);
    };
  }

  @Override
  public Object getValueAt(final int rowIndex, final int columnIndex) {
    final RecentChange recentChange = recentChanges.get(rowIndex);
    return switch (columnIndex) {
      case COLUMN_TIMESTAMP ->
          recentChange.timestamp() != null ? FORMATTER.format(recentChange.timestamp()) : "";
      case COLUMN_TITLE -> recentChange.title() != null ? recentChange.title() : "";
      case COLUMN_USER -> recentChange.user() != null ? recentChange.user() : "";
      case COLUMN_TAGS -> String.join(", ", recentChange.tags());
      default -> throw new IllegalArgumentException("Unknown column: " + columnIndex);
    };
  }
}
