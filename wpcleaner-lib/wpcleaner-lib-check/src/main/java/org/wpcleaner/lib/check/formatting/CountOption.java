package org.wpcleaner.lib.check.formatting;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Optional;
import org.wpcleaner.api.repository.configuration.WPCleanerConfiguration;
import org.wpcleaner.api.repository.configuration.WPCleanerParameter;

record CountOption(int min, int max) {

  private static final char RANGE_SEPARATOR = '-';

  static Optional<CountOption> from(
      final WPCleanerConfiguration configuration, final WPCleanerParameter parameter) {
    return configuration.getString(parameter).map(String::trim).flatMap(CountOption::parse);
  }

  private static Optional<CountOption> parse(final String option) {
    final int separator = option.indexOf(RANGE_SEPARATOR);
    try {
      final int min = Integer.parseInt(separator < 0 ? option : option.substring(0, separator));
      final int max = separator < 0 ? min : Integer.parseInt(option.substring(separator + 1));
      return min >= 0 && max >= min ? Optional.of(new CountOption(min, max)) : Optional.empty();
    } catch (final NumberFormatException _) {
      return Optional.empty();
    }
  }

  boolean accepts(final int count) {
    return min <= count && count <= max;
  }

  int normalize(final int count) {
    return Math.clamp(count, min, max);
  }
}
