package org.wpcleaner.api.repository;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Locale;
import java.util.Objects;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.api.utils.StringUtils;

public enum CaseType {
  FIRST_LETTER,
  CASE_SENSITIVE,
  CASE_INSENSITIVE;

  public String normalize(final String original) {
    return switch (this) {
      case FIRST_LETTER -> StringUtils.firstLetterUpperCase(original);
      case CASE_SENSITIVE -> original;
      case CASE_INSENSITIVE -> original.toLowerCase(Locale.ROOT);
    };
  }

  public boolean areEqual(final String first, final String second) {
    return Objects.equals(first, second) || Objects.equals(normalize(first), normalize(second));
  }

  public static CaseType fromValue(@Nullable final String value) {
    return switch (Objects.requireNonNullElse(value, "")) {
      case "case-sensitive" -> CASE_SENSITIVE;
      case "case-insensitive" -> CASE_INSENSITIVE;
      default -> FIRST_LETTER;
    };
  }
}
