package org.wpcleaner.application.gui.javafx.core.pageanalysis;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.fxmisc.flowless.VirtualizedScrollPane;
import org.wpcleaner.application.gui.javafx.core.pageanalysis.coloration.PageSyntaxColorizer;

public final class PageAnalysisScrollPane extends VirtualizedScrollPane<PageAnalysisArea> {

  private final PageAnalysisArea area;

  public PageAnalysisScrollPane(final PageSyntaxColorizer colorizer) {
    this(new PageAnalysisArea(colorizer));
  }

  private PageAnalysisScrollPane(final PageAnalysisArea area) {
    super(area);
    this.area = area;
  }

  public PageAnalysisArea getArea() {
    return area;
  }
}
