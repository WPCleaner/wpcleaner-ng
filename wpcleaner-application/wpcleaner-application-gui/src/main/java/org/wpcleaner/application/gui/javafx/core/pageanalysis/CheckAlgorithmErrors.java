package org.wpcleaner.application.gui.javafx.core.pageanalysis;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.api.analysis.PageAnalysis;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.lib.check.CheckAlgorithm;
import org.wpcleaner.lib.check.CheckAlgorithmRegistry;
import org.wpcleaner.lib.check.CheckError;

public record CheckAlgorithmErrors(
    CheckAlgorithm algorithm,
    String name,
    @Nullable String description,
    @Nullable String linkUrl,
    List<CheckError> errors) {

  public CheckAlgorithmErrors {
    errors = List.copyOf(errors);
  }

  public static CheckAlgorithmErrors of(
      final CheckAlgorithmRegistry registry,
      final CheckAlgorithm algorithm,
      final PageAnalysis analysis) {
    final WikiDefinition wiki = analysis.getWiki();
    return new CheckAlgorithmErrors(
        algorithm,
        registry.getName(wiki, algorithm),
        registry.getDescription(wiki, algorithm).orElse(null),
        registry.getLink(wiki, algorithm).map(wiki::pageUrl).orElse(null),
        algorithm.analyze(analysis));
  }

  public CheckAlgorithmErrors atPosition(final int position) {
    return new CheckAlgorithmErrors(
        algorithm,
        name,
        description,
        linkUrl,
        errors.stream()
            .filter(error -> error.begin() <= position && position < error.end())
            .toList());
  }
}
