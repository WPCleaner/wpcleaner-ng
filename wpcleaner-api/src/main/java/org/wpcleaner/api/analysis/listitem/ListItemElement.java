package org.wpcleaner.api.analysis.listitem;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.jspecify.annotations.Nullable;
import org.wpcleaner.api.analysis.Element;

public record ListItemElement(
    int begin, int end, String prefix, String text, @Nullable ListItemDefinition definition)
    implements Element {

  public ListItemElement(final int begin, final int end, final String prefix, final String text) {
    this(begin, end, prefix, text, null);
  }
}
