package org.wpcleaner.api.utils;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import com.google.errorprone.annotations.FormatMethod;
import com.google.errorprone.annotations.FormatString;

/* Helper class for internationalization */
@SuppressWarnings("PMD.ShortClassName")
public final class GT {

  private GT() {
    // Utility class
  }

  /* Translate a fixed String into the current language */
  @SuppressWarnings("PMD.MethodNamingConventions")
  public static String _T(final String msg) {
    return msg;
  }

  /* Translate a String with placeholders into the current language */
  @FormatMethod
  @SuppressWarnings("PMD.MethodNamingConventions")
  public static String _T(@FormatString final String msg, final Object... variables) {
    return msg.formatted(variables);
  }
}
