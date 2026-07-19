package org.wpcleaner.api.api;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;
import org.springframework.web.util.UriBuilder;

public final class UriBuilderUtils {

  private UriBuilderUtils() {
    // Utility class
  }

  public static void queryParam(final UriBuilder builder, final String name, final boolean value) {
    if (value) {
      builder.queryParam(name, "");
    }
  }

  public static void queryParam(
      final UriBuilder builder, final String name, @Nullable final Object value) {
    queryParam(builder, name, value, Object::toString);
  }

  public static <T> void queryParam(
      final UriBuilder builder,
      final String name,
      @Nullable final T value,
      final Function<T, @Nullable String> toString) {
    if (value == null) {
      return;
    }
    final String formatted = toString.apply(value);
    if (formatted == null) {
      return;
    }
    builder.queryParam(name, formatted);
  }

  public static <T> void queryParamCollection(
      final UriBuilder builder, final String name, @Nullable final Collection<T> values) {
    queryParamCollection(builder, name, values, Object::toString);
  }

  public static <T> void queryParamCollection(
      final UriBuilder builder,
      final String name,
      @Nullable final Collection<T> values,
      final Function<T, String> toString) {
    if (values == null || values.isEmpty()) {
      return;
    }
    final String param = values.stream().map(toString).sorted().collect(Collectors.joining("|"));
    builder.queryParam(name, param);
  }
}
