package org.wpcleaner.application.gui.javafx.core.pageanalysis.coloration;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;
import org.wpcleaner.api.analysis.PageAnalysis;
import org.wpcleaner.application.gui.core.style.PageAnalysisStylePropertiesInitializer;

@Component
public class ListItemSyntaxRule implements PageSyntaxRule {

  @Override
  public String getStyleName() {
    return PageAnalysisStylePropertiesInitializer.LIST_ITEM;
  }

  @Override
  public List<RuleRange> getRanges(final PageAnalysis pageAnalysis) {
    return pageAnalysis.getListItems().stream()
        .flatMap(
            item ->
                Stream.concat(
                    Stream.of(new RuleRange(item.begin(), item.begin() + item.prefix().length())),
                    Optional.ofNullable(item.definition()).stream()
                        .map(
                            definition ->
                                new RuleRange(definition.begin(), definition.begin() + 1))))
        .toList();
  }
}
