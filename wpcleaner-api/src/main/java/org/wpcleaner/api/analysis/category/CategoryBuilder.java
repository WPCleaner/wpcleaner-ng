package org.wpcleaner.api.analysis.category;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.jspecify.annotations.Nullable;

public record CategoryBuilder(String namespace, String name, @Nullable String sortKey) {

  private static final String LINK_START = "[[";
  private static final String LINK_END = "]]";
  private static final char COLON = ':';
  private static final char PIPE = '|';

  public static CategoryBuilder to(final String namespace, final String name) {
    return new CategoryBuilder(namespace, name, null);
  }

  public CategoryBuilder withSortKey(@Nullable final String newSortKey) {
    return new CategoryBuilder(namespace, name, newSortKey);
  }

  public String build() {
    final String target = LINK_START + namespace + COLON + name;
    if (sortKey == null) {
      return target + LINK_END;
    }
    return target + PIPE + sortKey + LINK_END;
  }
}
