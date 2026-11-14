package org.wpcleaner.application.bot.task.contentcurrent;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wpcleaner.api.analysis.PageAnalysis;
import org.wpcleaner.api.analysis.PageAnalysisFactory;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.application.bot.content.ContentPage;
import org.wpcleaner.lib.check.CheckAlgorithmRegistry;

public final class PageErrorsDetector {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final WikiDefinition wiki;
  private final PageAnalysisFactory analysisFactory;
  private final CheckAlgorithmRegistry algorithmRegistry;
  private final List<SelectedAlgorithm> algorithms;
  private final Set<Integer> namespaces;

  public PageErrorsDetector(
      final WikiDefinition wiki,
      final PageAnalysisFactory analysisFactory,
      final CheckAlgorithmRegistry algorithmRegistry,
      final List<SelectedAlgorithm> algorithms) {
    this.wiki = wiki;
    this.analysisFactory = analysisFactory;
    this.algorithmRegistry = algorithmRegistry;
    this.algorithms = List.copyOf(algorithms);
    this.namespaces =
        algorithms.stream()
            .flatMap(algorithm -> algorithm.namespaces().stream())
            .collect(Collectors.toUnmodifiableSet());
  }

  @SuppressWarnings("PMD.AvoidCatchingGenericException")
  public List<ReportedError> detect(final ContentPage page) {
    if (!page.isWikitext() || !namespaces.contains(page.namespace())) {
      return List.of();
    }
    try {
      return analyze(page);
    } catch (final RuntimeException e) {
      LOGGER.warn("Unable to analyze {}: {}", page.title(), e.getMessage());
      return List.of();
    }
  }

  private List<ReportedError> analyze(final ContentPage page) {
    final PageAnalysis analysis = analysisFactory.analysis(wiki, page.title(), page.text());
    return algorithms.stream()
        .filter(selected -> selected.namespaces().contains(page.namespace()))
        .filter(selected -> !algorithmRegistry.isWhitelisted(analysis, selected.algorithm()))
        .flatMap(
            selected ->
                selected.algorithm().analyze(analysis).stream()
                    .map(
                        error ->
                            new ReportedError(
                                selected.algorithm().number(),
                                selected.algorithm().reportedText(analysis, error))))
        .toList();
  }
}
