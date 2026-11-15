package org.wpcleaner.api.repository.linter;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Arrays;
import java.util.Optional;

public enum LinterPriority {
  HIGH("high"),
  MEDIUM("medium"),
  LOW("low");

  public final String value;

  LinterPriority(final String value) {
    this.value = value;
  }

  public static Optional<LinterPriority> findByValue(final String value) {
    return Arrays.stream(values()).filter(priority -> priority.value.equals(value)).findFirst();
  }
}
