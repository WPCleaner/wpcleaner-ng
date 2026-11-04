package org.wpcleaner.application.gui.javafx.core.pageanalysis;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import org.fxmisc.richtext.InlineCssTextArea;
import org.fxmisc.richtext.model.StyleSpans;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.api.analysis.Element;
import org.wpcleaner.api.analysis.PageAnalysis;
import org.wpcleaner.api.analysis.PageAnalysisFactory;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.application.gui.javafx.core.pageanalysis.coloration.PageSyntaxColorizer;

public final class PageAnalysisArea extends InlineCssTextArea {

  private final PageSyntaxColorizer colorizer;
  private StyleSpans<String> syntaxSpans;

  public PageAnalysisArea(final PageSyntaxColorizer colorizer) {
    super();
    this.colorizer = colorizer;
    this.syntaxSpans = getStyleSpans(0, 0);
    setWrapText(true);
    setEditable(false);
    setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
  }

  public void updateAnalysis(final PageAnalysis analysis) {
    super.replaceText(analysis.getText());
    syntaxSpans = colorizer.computeStyleSpans(analysis);
    setStyleSpans(0, syntaxSpans);
  }

  public void highlight(final List<? extends Element> elements) {
    if (syntaxSpans.length() == getLength()) {
      setStyleSpans(0, colorizer.highlight(syntaxSpans, elements));
    }
  }

  public void updateText(
      final WikiDefinition wiki,
      final String title,
      @Nullable final String text,
      final PageAnalysisFactory factory) {
    if (text == null) {
      super.replaceText("");
    } else {
      updateAnalysis(factory.analysis(wiki, title, text));
    }
  }
}
