package org.wpcleaner.lib.check;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.wpcleaner.api.repository.configuration.WPCleanerConfiguration;
import org.wpcleaner.api.repository.configuration.WPCleanerConfigurationRepository;
import org.wpcleaner.api.repository.configuration.WPCleanerParameter;
import org.wpcleaner.api.utils.GT;
import org.wpcleaner.api.wiki.definition.WikiDefinition;

@Service
public class CheckWikiSummary {

  private static final String ALGORITHMS_PLACEHOLDER = "{0}";
  private static final String ALGORITHMS_SEPARATOR = " - ";
  private static final String DEFAULT_PROJECT = "Check Wiki project";

  private final CheckAlgorithmRegistry algorithmRegistry;
  private final WPCleanerConfigurationRepository configurationRepository;

  public CheckWikiSummary(
      final CheckAlgorithmRegistry algorithmRegistry,
      final WPCleanerConfigurationRepository configurationRepository) {
    this.algorithmRegistry = algorithmRegistry;
    this.configurationRepository = configurationRepository;
  }

  public String create(final WikiDefinition wiki, final List<CheckAlgorithm> fixedAlgorithms) {
    if (fixedAlgorithms.isEmpty()) {
      return "";
    }
    final String names =
        fixedAlgorithms.stream()
            .map(algorithm -> algorithmRegistry.getName(wiki, algorithm))
            .distinct()
            .collect(Collectors.joining(ALGORITHMS_SEPARATOR));
    final String comment = getComment(configurationRepository.getConfiguration(wiki));
    return comment.contains(ALGORITHMS_PLACEHOLDER)
        ? comment.replace(ALGORITHMS_PLACEHOLDER, names)
        : "%s (%s)".formatted(comment, names);
  }

  private static String getComment(final WPCleanerConfiguration configuration) {
    return configuration
        .getString(WPCleanerParameter.CHECK_WIKI_COMMENT)
        .orElseGet(
            () ->
                GT._T(
                    "Fixed using %s",
                    configuration
                        .getString(WPCleanerParameter.CHECK_WIKI_PROJECT_PAGE)
                        .map("[[%s]]"::formatted)
                        .orElse(DEFAULT_PROJECT)));
  }
}
