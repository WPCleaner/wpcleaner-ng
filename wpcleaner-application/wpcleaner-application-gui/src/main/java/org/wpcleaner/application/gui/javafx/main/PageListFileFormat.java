package org.wpcleaner.application.gui.javafx.main;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.wpcleaner.api.utils.GT;

enum PageListFileFormat {
  PAGE_NAMES(GT._T("Unformatted list of page names")),
  INTERNAL_LINKS(GT._T("Internal links in a formatted list"));

  private final String label;

  PageListFileFormat(final String label) {
    this.label = label;
  }

  @Override
  public String toString() {
    return label;
  }
}
