package org.wpcleaner.api.utils;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Collection;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class StringUtils {

  private StringUtils() {
    // Utility class
  }

  public static String firstLetterLowerCase(final String str) {
    if (str.isEmpty()) {
      return str;
    }
    return str.substring(0, 1).toLowerCase(Locale.ROOT) + str.substring(1);
  }

  public static String joinWithEllipsis(final Collection<String> items, final int maxItems) {
    if (items.size() > maxItems) {
      return Stream.concat(items.stream().limit(maxItems), Stream.of("…"))
          .collect(Collectors.joining(", "));
    }
    return String.join(", ", items);
  }

  public static String removePrefix(final String str, final String prefix) {
    if (str.startsWith(prefix)) {
      return str.substring(prefix.length());
    }
    return str;
  }

  public static String removeSuffix(final String str, final String suffix) {
    if (str.endsWith(suffix)) {
      return str.substring(0, str.length() - suffix.length());
    }
    return str;
  }
}
