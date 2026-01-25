package org.wpcleaner.api.api;

/*
 * SPDX-FileCopyrightText: © 2025 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import jakarta.annotation.Nullable;
import java.lang.invoke.MethodHandles;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.UriBuilder;

public final class ApiUtils {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private ApiUtils() {
    // Utility class
  }

  public static UriBuilder configure(
      final UriBuilder uriBuilder, final ApiParameters.Action action) {
    return uriBuilder
        .queryParam(ApiParameters.ACTION.value, action.value)
        .queryParam(ApiParameters.ERROR_FORMAT.value, ApiParameters.ErrorFormat.PLAIN_TEXT.value)
        .queryParam(ApiParameters.FORMAT.value, ApiParameters.Format.JSON.value)
        .queryParam(ApiParameters.FORMAT_VERSION.value, ApiParameters.FORMAT_VERSION_VALUE);
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
}
