package org.wpcleaner.api.analysis.table;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.api.analysis.Element;

public record TableElement(
    int begin, int end, String attributes, @Nullable TableCaption caption, List<TableRow> rows)
    implements Element {

  public TableElement {
    rows = List.copyOf(rows);
  }
}
