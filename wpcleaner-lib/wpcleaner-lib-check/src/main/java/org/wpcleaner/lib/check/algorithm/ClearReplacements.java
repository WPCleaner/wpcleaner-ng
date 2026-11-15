package org.wpcleaner.lib.check.algorithm;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.api.analysis.tag.TagAttribute;
import org.wpcleaner.api.analysis.tag.TagElement;

record ClearReplacements(
    @Nullable String clearAll, @Nullable String clearLeft, @Nullable String clearRight) {

  private static final String CLEAR = "clear";
  private static final String STYLE = "style";
  private static final String BREAK = "break";
  private static final String STYLE_PREFIX = "clear:";
  private static final Pattern TRAILING_SEPARATORS = Pattern.compile("[;\\s]+$");

  static Optional<String> findValue(final TagElement tag) {
    return findAttribute(tag, CLEAR)
        .or(() -> findAttribute(tag, STYLE).flatMap(ClearReplacements::findStyleValue))
        .or(() -> findAttribute(tag, BREAK));
  }

  Optional<String> findReplacement(final String value) {
    return Optional.ofNullable(
        switch (value.toLowerCase(Locale.ROOT)) {
          case "all", "both" -> clearAll;
          case "left" -> clearLeft;
          case "right" -> clearRight;
          default -> null;
        });
  }

  private static Optional<String> findAttribute(final TagElement tag, final String name) {
    return tag.attributes().stream()
        .filter(attribute -> name.equalsIgnoreCase(attribute.name()))
        .findFirst()
        .map(TagAttribute::value)
        .map(String::trim);
  }

  private static Optional<String> findStyleValue(final String style) {
    if (!style.startsWith(STYLE_PREFIX)) {
      return Optional.empty();
    }
    return Optional.of(
        TRAILING_SEPARATORS
            .matcher(style.substring(STYLE_PREFIX.length()))
            .replaceFirst("")
            .trim());
  }
}
