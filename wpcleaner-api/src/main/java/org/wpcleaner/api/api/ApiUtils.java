package org.wpcleaner.api.api;

/*
 * SPDX-FileCopyrightText: © 2025 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import jakarta.annotation.Nullable;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ApiUtils {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private ApiUtils() {
    // Utility class
  }

  public static <I extends ApiResponse, T> T processApiResponse(
      @Nullable final I response, final Function<I, T> extractor) {
    if (response == null) {
      throw new ApiException("No response retrieved", List.of());
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
