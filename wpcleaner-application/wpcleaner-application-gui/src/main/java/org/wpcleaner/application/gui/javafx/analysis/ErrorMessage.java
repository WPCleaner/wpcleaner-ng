package org.wpcleaner.application.gui.javafx.analysis;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.wpcleaner.api.api.ApiException;

final class ErrorMessage {

  private ErrorMessage() {}

  static String of(final Exception e) {
    final String message = String.valueOf(e.getMessage());
    return e instanceof ApiException apiException
        ? "%s%n%s".formatted(message, apiException.getDetails())
        : message;
  }
}
