package org.wpcleaner.application.gui.javafx.analysis.coloration;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import org.springframework.stereotype.Component;
import org.wpcleaner.api.analysis.PageAnalysis;
import org.wpcleaner.application.gui.core.style.PageAnalysisStylePropertiesInitializer;

@Component
public class CommentSyntaxRule implements PageSyntaxRule {

  @Override
  public String getStyleName() {
    return PageAnalysisStylePropertiesInitializer.COMMENT;
  }

  @Override
  public List<RuleRange> getRanges(final PageAnalysis pageAnalysis) {
    return pageAnalysis.getComments().getComments().stream()
        .map(comment -> new RuleRange(comment.begin(), comment.end()))
        .toList();
  }
}
