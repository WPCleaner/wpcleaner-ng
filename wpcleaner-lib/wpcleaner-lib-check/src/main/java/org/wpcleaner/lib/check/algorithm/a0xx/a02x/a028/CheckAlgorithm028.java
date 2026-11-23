package org.wpcleaner.lib.check.algorithm.a0xx.a02x.a028;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import org.wpcleaner.api.analysis.PageAnalysis;
import org.wpcleaner.api.analysis.pagetitle.PageTitle;
import org.wpcleaner.api.repository.namespace.CommonNamespaces;
import org.wpcleaner.lib.check.CheckAlgorithm;
import org.wpcleaner.lib.check.CheckError;
import org.wpcleaner.lib.check.configuration.CheckAlgorithmConfiguration;
import org.wpcleaner.lib.check.configuration.CheckWikiConfigurationRepository;

@Component
public class CheckAlgorithm028 implements CheckAlgorithm {

  private static final int ALGORITHM_NUMBER = 28;
  private static final String PARAMETER_TEMPLATES = "templates";
  private static final String HAS_NEWLINE_MARKER = "@@hasnewline@@";
  private static final String TEMPLATE_START = "{{";
  private static final String TEMPLATE_END = "}}";

  private final CheckWikiConfigurationRepository checkWikiRepository;

  public CheckAlgorithm028(final CheckWikiConfigurationRepository checkWikiRepository) {
    this.checkWikiRepository = checkWikiRepository;
  }

  @Override
  public int number() {
    return ALGORITHM_NUMBER;
  }

  @Override
  public List<CheckError> analyze(final PageAnalysis analysis) {
    final CheckAlgorithmConfiguration configuration =
        checkWikiRepository
            .getConfiguration(analysis.getWiki())
            .getAlgorithm(ALGORITHM_NUMBER)
            .orElseGet(() -> new CheckAlgorithmConfiguration(ALGORITHM_NUMBER, Map.of()));
    final TableLimits limits =
        TableLimits.find(analysis, findEndTemplates(analysis, configuration));
    return findUnclosedStarts(limits).stream()
        .map(start -> new CheckError(ALGORITHM_NUMBER, start.begin(), start.end(), List.of()))
        .toList();
  }

  private static List<TableLimit> findUnclosedStarts(final TableLimits limits) {
    final List<TableLimit> unclosedStarts = new ArrayList<>(limits.starts());
    for (final TableLimit end : limits.ends()) {
      unclosedStarts.stream()
          .filter(start -> start.end() < end.begin())
          .reduce((first, second) -> second)
          .ifPresent(unclosedStarts::remove);
    }
    return List.copyOf(unclosedStarts);
  }

  private static Set<PageTitle> findEndTemplates(
      final PageAnalysis analysis, final CheckAlgorithmConfiguration configuration) {
    return configuration.getStrings(PARAMETER_TEMPLATES).stream()
        .map(CheckAlgorithm028::cleanTemplateName)
        .filter(name -> !name.isEmpty())
        .flatMap(name -> analysis.normalizeTitle(name, CommonNamespaces.TEMPLATE.id).stream())
        .collect(Collectors.toSet());
  }

  private static String cleanTemplateName(final String value) {
    final String trimmed = value.trim();
    final int markerIndex = trimmed.toLowerCase(Locale.ROOT).indexOf(HAS_NEWLINE_MARKER);
    final String withoutMarker =
        (markerIndex < 0 ? trimmed : trimmed.substring(0, markerIndex)).trim();
    final String withoutStart =
        withoutMarker.startsWith(TEMPLATE_START)
            ? withoutMarker.substring(TEMPLATE_START.length())
            : withoutMarker;
    return withoutStart.endsWith(TEMPLATE_END)
        ? withoutStart.substring(0, withoutStart.length() - TEMPLATE_END.length())
        : withoutStart;
  }
}
