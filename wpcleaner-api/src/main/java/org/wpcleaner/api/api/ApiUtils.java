package org.wpcleaner.api.api;

/*
 * SPDX-FileCopyrightText: © 2025 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.MultiValueMap;

public final class ApiUtils {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private ApiUtils() {
    // Utility class
  }

  public static <I extends ApiResponse, T> T processApiResponse(
      @Nullable final I response, final Function<I, T> extractor) {
    if (response == null) {
      throw new ApiException("No response retrieved", "Unknown details");
    }
    if (!response.errors().isEmpty()) {
      LOGGER.error("Errors in API response: {}", response.errors());
      throw new ApiException("Error", response.errors());
    }
    if (!response.warnings().isEmpty()) {
      LOGGER.warn("Warnings in API response: {}", response.warnings());
    }
    return extractor.apply(response);
  }

  public static void addBodyParameter(
      final MultiValueMap<String, Object> body, final String key, @Nullable final Object value) {
    switch (value) {
      case null -> {}
      case Boolean _ -> {
        if (Boolean.TRUE.equals(value)) {
          body.add(key, "1");
        }
      }
      case List<?> list -> {
        if (!list.isEmpty()) {
          body.add(key, list.stream().map(Object::toString).collect(Collectors.joining("|")));
        }
      }
      default -> body.add(key, value.toString());
    }
  }
}
