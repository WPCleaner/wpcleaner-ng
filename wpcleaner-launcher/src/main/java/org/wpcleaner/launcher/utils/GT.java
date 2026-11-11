package org.wpcleaner.launcher.utils;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import com.google.errorprone.annotations.FormatMethod;
import com.google.errorprone.annotations.FormatString;
import org.jspecify.annotations.Nullable;

@SuppressWarnings("PMD.ShortClassName")
public final class GT {

  private GT() {}

  @SuppressWarnings("PMD.MethodNamingConventions")
  public static String _T(final String msg) {
    return msg;
  }

  @FormatMethod
  @SuppressWarnings("PMD.MethodNamingConventions")
  public static String _T(@FormatString final String msg, @Nullable final Object... variables) {
    return msg.formatted(variables);
  }
}
