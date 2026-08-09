package org.wpcleaner.application.gui.javafx.analysis;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.fxmisc.richtext.InlineCssTextArea;
import org.wpcleaner.application.gui.javafx.analysis.coloration.PageSyntaxColorizer;

public final class PageAnalysisArea extends InlineCssTextArea {

  private final PageSyntaxColorizer colorizer;

  public PageAnalysisArea(final PageSyntaxColorizer colorizer) {
    super();
    this.colorizer = colorizer;
    setWrapText(true);

    textProperty()
        .addListener(
            (_, _, newText) -> setStyleSpans(0, this.colorizer.computeStyleSpans(newText)));
  }
}
