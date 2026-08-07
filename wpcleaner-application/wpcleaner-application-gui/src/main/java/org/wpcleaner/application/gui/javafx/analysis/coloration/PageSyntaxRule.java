package org.wpcleaner.application.gui.javafx.analysis.coloration;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import org.wpcleaner.api.analysis.PageAnalysis;

public interface PageSyntaxRule {

  String getStyleName();

  List<RuleRange> getRanges(PageAnalysis pageAnalysis);

  record RuleRange(int begin, int end) {}
}
