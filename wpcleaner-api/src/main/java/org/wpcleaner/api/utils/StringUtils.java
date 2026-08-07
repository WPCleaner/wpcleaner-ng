package org.wpcleaner.api.utils;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Collection;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;

public final class StringUtils {

  private StringUtils() {
    // Utility class
  }

  public static String firstLetterLowerCase(final String original) {
    if (original.isEmpty()) {
      return original;
    }
    return original.substring(0, 1).toLowerCase(Locale.ROOT) + original.substring(1);
  }

  public static String joinWithEllipsis(final Collection<String> items, final int maxItems) {
    if (items.size() > maxItems) {
      return Stream.concat(items.stream().limit(maxItems), Stream.of("…"))
          .collect(Collectors.joining(", "));
    }
    return String.join(", ", items);
  }

  public static String removePrefix(final String original, final String prefix) {
    if (original.startsWith(prefix)) {
      return original.substring(prefix.length());
    }
    return original;
  }

  public static String removeSuffix(final String original, final String suffix) {
    if (original.endsWith(suffix)) {
      return original.substring(0, original.length() - suffix.length());
    }
    return original;
  }

  public static String trim(@Nullable final String original) {
    if (Objects.isNull(original) || original.isBlank()) {
      return "";
    }
    return original.trim();
  }
}
