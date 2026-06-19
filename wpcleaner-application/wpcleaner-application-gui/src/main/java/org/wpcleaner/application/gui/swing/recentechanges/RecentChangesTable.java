package org.wpcleaner.application.gui.swing.recentechanges;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.io.Serial;
import javax.swing.JTable;

public final class RecentChangesTable extends JTable {

  @Serial private static final long serialVersionUID = 1L;

  public RecentChangesTable(final RecentChangesModel model) {
    super(model);
  }
}
