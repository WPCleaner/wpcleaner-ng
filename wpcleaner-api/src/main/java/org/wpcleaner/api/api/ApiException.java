package org.wpcleaner.api.api;

/*
 * SPDX-FileCopyrightText: © 2025 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.io.Serial;
import java.util.List;
import java.util.stream.Collectors;

public class ApiException extends RuntimeException {

  @Serial private static final long serialVersionUID = -6009714247794453133L;

  private final transient String details;

  public ApiException(final String message, final String details) {
    super(message);
    this.details = details;
  }

  public ApiException(final String message, final List<ApiError> errors) {
    super(message);
    this.details = buildDetails(errors);
  }

  private String buildDetails(final List<ApiError> errors) {
    if (errors.isEmpty()) {
      return "No explicit errors found";
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
