package org.wpcleaner.application.base.utils.string;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class StringUtils {

  private StringUtils() {
    // Utility class
  }

  public static String joinWithEllipsis(final Collection<String> items, final int maxItems) {
    if (items.size() > maxItems) {
      return Stream.concat(items.stream().limit(maxItems), Stream.of("…"))
          .collect(Collectors.joining(", "));
    }
    return String.join(", ", items);
  }
}
