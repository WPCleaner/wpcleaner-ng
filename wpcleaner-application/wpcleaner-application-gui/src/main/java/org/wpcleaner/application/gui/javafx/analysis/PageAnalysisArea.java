package org.wpcleaner.application.gui.javafx.analysis;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.fxmisc.richtext.InlineCssTextArea;

public final class PageAnalysisArea extends InlineCssTextArea {

  public PageAnalysisArea() {
    super();
    setEditable(false);
    setWrapText(true);
  }
}
