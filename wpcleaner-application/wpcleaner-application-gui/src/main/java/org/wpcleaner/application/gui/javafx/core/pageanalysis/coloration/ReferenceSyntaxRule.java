package org.wpcleaner.application.gui.javafx.core.pageanalysis.coloration;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;
import org.wpcleaner.api.analysis.PageAnalysis;
import org.wpcleaner.api.analysis.tag.TagKind;
import org.wpcleaner.api.analysis.tag.WikiTagType;
import org.wpcleaner.application.gui.core.style.PageAnalysisStylePropertiesInitializer;

@Component
public class ReferenceSyntaxRule implements PageSyntaxRule {

  private static final Set<String> REFERENCE_TAGS =
      Set.of(WikiTagType.REF.getNormalizedName(), WikiTagType.REFERENCES.getNormalizedName());

  @Override
  public String getStyleName() {
    return PageAnalysisStylePropertiesInitializer.REFERENCE;
  }

  @Override
  public List<RuleRange> getRanges(final PageAnalysis pageAnalysis) {
    return Stream.concat(
            pageAnalysis.getCompleteTags().stream()
                .filter(tag -> REFERENCE_TAGS.contains(tag.startTag().normalizedName()))
                .map(tag -> new RuleRange(tag.begin(), tag.end())),
            pageAnalysis.getTags().stream()
                .filter(tag -> tag.kind() == TagKind.SELF_CLOSING)
                .filter(tag -> REFERENCE_TAGS.contains(tag.normalizedName()))
                .map(tag -> new RuleRange(tag.begin(), tag.end())))
        .toList();
  }
}
