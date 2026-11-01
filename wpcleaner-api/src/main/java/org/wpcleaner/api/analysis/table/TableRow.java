package org.wpcleaner.api.analysis.table;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;

public record TableRow(int begin, int end, String attributes, List<TableCell> cells) {

  public TableRow {
    cells = List.copyOf(cells);
  }
}
