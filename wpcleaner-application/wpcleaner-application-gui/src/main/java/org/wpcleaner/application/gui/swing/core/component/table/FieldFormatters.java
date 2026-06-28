package org.wpcleaner.application.gui.swing.core.component.table;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import jakarta.annotation.Nullable;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public final class FieldFormatters {

  private FieldFormatters() {
    // Utility class
  }

  public static String formatTime(@Nullable final Instant instant) {
    if (instant == null) {
      return "";
    }
    return DateTimeFormatter.ISO_LOCAL_TIME.format(
        instant.atZone(ZoneId.systemDefault()).toLocalTime());
  }
}
