package org.wpcleaner.api.analysis.comparison;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

record ParseTreeElement(Kind kind, int begin, int end, int level) {
  enum Kind {
    COMMENT,
    EXTENSION_TAG,
    HEADING,
    IGNORE,
    TEMPLATE,
    TEMPLATE_ARGUMENT
  }
}
