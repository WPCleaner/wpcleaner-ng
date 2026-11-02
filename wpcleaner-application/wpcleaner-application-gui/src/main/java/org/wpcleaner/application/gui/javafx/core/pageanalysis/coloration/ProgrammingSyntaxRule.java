package org.wpcleaner.application.gui.javafx.core.pageanalysis.coloration;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;
import org.wpcleaner.api.analysis.PageAnalysis;
import org.wpcleaner.application.gui.core.style.PageAnalysisStylePropertiesInitializer;

@Component
public class ProgrammingSyntaxRule implements PageSyntaxRule {

  @Override
  public String getStyleName() {
    return PageAnalysisStylePropertiesInitializer.PROGRAMMING;
  }

  @Override
  public List<RuleRange> getRanges(final PageAnalysis pageAnalysis) {
    return Stream.of(
            pageAnalysis.getFunctions(),
            pageAnalysis.getParameters(),
            pageAnalysis.getBehaviorSwitches())
        .flatMap(List::stream)
        .map(element -> new RuleRange(element.begin(), element.end()))
        .toList();
  }
}
