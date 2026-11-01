package org.wpcleaner.api.analysis.preprocessor;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;

record CurlyBrackets(int begin, int end, int count, List<Part> parts) {

  CurlyBrackets {
    parts = List.copyOf(parts);
  }

  record Part(int begin, int end, int equalIndex) {

    static final int NO_EQUAL = -1;

    boolean hasEqual() {
      return equalIndex != NO_EQUAL;
    }
  }
}
