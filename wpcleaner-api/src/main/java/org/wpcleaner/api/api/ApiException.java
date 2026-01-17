package org.wpcleaner.api.api;

/*
 * SPDX-FileCopyrightText: © 2025 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.io.Serial;
import java.util.List;

public class ApiException extends RuntimeException {

  @Serial private static final long serialVersionUID = -6009714247794453133L;

  private final transient List<ApiError> errors;

  public ApiException(final String message, final List<ApiError> errors) {
    super(message);
    this.errors = errors;
  }

  public List<ApiError> getErrors() {
    return errors;
  }
}
