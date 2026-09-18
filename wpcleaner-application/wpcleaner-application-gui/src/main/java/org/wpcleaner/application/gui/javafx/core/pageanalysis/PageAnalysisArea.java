package org.wpcleaner.application.gui.javafx.core.pageanalysis;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.fxmisc.richtext.InlineCssTextArea;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.api.analysis.PageAnalysis;
import org.wpcleaner.api.analysis.PageAnalysisFactory;
import org.wpcleaner.application.gui.javafx.core.pageanalysis.coloration.PageSyntaxColorizer;

public final class PageAnalysisArea extends InlineCssTextArea {

  private final PageSyntaxColorizer colorizer;

  public PageAnalysisArea(final PageSyntaxColorizer colorizer) {
    super();
    this.colorizer = colorizer;
    setWrapText(true);
    setEditable(false);
    setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
  }

  public void updateAnalysis(final PageAnalysis analysis) {
    super.replaceText(analysis.getText());
    setStyleSpans(0, colorizer.computeStyleSpans(analysis));
  }

  public void updateText(
      final String title, @Nullable final String text, final PageAnalysisFactory factory) {
    if (text == null) {
      super.replaceText("");
    } else {
      updateAnalysis(factory.analysis(title, text));
    }
  }
}
