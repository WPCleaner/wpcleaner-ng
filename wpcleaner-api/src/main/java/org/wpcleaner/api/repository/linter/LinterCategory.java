package org.wpcleaner.api.repository.linter;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.jspecify.annotations.Nullable;

public record LinterCategory(String name, LinterPriority priority, @Nullable String label) {

  public LinterCategory withLabel(@Nullable final String newLabel) {
    return new LinterCategory(name, priority, newLabel);
  }
}
