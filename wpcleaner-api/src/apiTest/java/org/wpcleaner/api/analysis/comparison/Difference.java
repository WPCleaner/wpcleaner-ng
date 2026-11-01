package org.wpcleaner.api.analysis.comparison;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

record Difference(String type, Side side, String detail) {
  enum Side {
    ONLY_MEDIAWIKI,
    ONLY_WPCLEANER
  }
}
