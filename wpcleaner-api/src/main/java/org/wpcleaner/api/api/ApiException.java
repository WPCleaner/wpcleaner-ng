package org.wpcleaner.api.api;

/*
 * SPDX-FileCopyrightText: © 2025 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.io.Serial;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApiException extends RuntimeException {

  @Serial private static final long serialVersionUID = -6009714247794453133L;
  private static final String DEFAULT_DETAILS = "No explicit errors found";
  private static final Logger LOGGER =
      LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final transient String details;

  public ApiException(final String message, final List<ApiError> errors) {
    this(message, buildDetails(errors));
  }

  public ApiException(final String message, final String details) {
    super(message);
    this.details = details;
  }

  public ApiException(final String message, final Throwable cause) {
    this(message, cause, DEFAULT_DETAILS);
  }

  public ApiException(final String message, final Throwable cause, final String details) {
    super(message, cause);
    this.details = details;
  }

  public static ApiException of(final Throwable e, final String message) {
    LOGGER.error(message, e);
    if (e instanceof ApiException apiException) {
      if (Objects.equals(message, apiException.getMessage())) {
        return apiException;
      }
      return new ApiException(message, apiException, apiException.getDetails());
    }
    if ((e instanceof InterruptedException || e instanceof ExecutionException)
        && e.getCause() != null) {
      return of(e.getCause(), message);
    }
    return new ApiException(message, e);
  }

  private static String buildDetails(final List<ApiError> errors) {
    if (errors.isEmpty()) {
      return DEFAULT_DETAILS;
    }
    if (errors.size() == 1) {
      return errors.getFirst().toSimpleString();
    }
    return errors.stream()
        .map(ApiError::toSimpleString)
        .collect(Collectors.joining("</li>\n<li>", "<ul><li>", "</li></ul>"));
  }

  public String getDetails() {
    return details;
  }
}
