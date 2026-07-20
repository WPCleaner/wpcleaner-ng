package org.wpcleaner.api.api;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.net.URI;
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;
import org.springframework.web.util.UriBuilder;

public final class ApiUriBuilder {

  private final UriBuilder builder;

  public static ApiUriBuilder of(final UriBuilder uriBuilder, final ApiParameters.Action action) {
    final ApiUriBuilder builder = new ApiUriBuilder(uriBuilder);
    builder.queryParam(ApiParameters.ACTION.value, action.value);
    builder.queryParam(
        ApiParameters.ERROR_FORMAT.value, ApiParameters.ErrorFormat.PLAIN_TEXT.value);
    builder.queryParam(ApiParameters.FORMAT.value, ApiParameters.Format.JSON.value);
    builder.queryParam(ApiParameters.FORMAT_VERSION.value, ApiParameters.FORMAT_VERSION_VALUE);
    return builder;
  }

  private ApiUriBuilder(final UriBuilder builder) {
    this.builder = builder;
  }

  public URI build() {
    return builder.build();
  }

  public void queryParam(final String name, final boolean value) {
    if (value) {
      builder.queryParam(name, "");
    }
  }

  public void queryParam(final String name, @Nullable final Object value) {
    queryParam(name, value, Object::toString);
  }

  public <T> void queryParam(
      final String name, @Nullable final T value, final Function<T, @Nullable String> toString) {
    if (value == null) {
      return;
    }
    final String formatted = toString.apply(value);
    if (formatted == null) {
      return;
    }
    builder.queryParam(name, formatted);
  }

  public <T> void queryParamCollection(final String name, @Nullable final Collection<T> values) {
    queryParamCollection(name, values, Object::toString);
  }

  public <T> void queryParamCollection(
      final String name, @Nullable final Collection<T> values, final Function<T, String> toString) {
    if (values == null || values.isEmpty()) {
      return;
    }
    final String param = values.stream().map(toString).sorted().collect(Collectors.joining("|"));
    builder.queryParam(name, param);
  }
}
