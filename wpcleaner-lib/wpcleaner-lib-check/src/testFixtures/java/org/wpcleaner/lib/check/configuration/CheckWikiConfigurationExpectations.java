package org.wpcleaner.lib.check.configuration;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.wpcleaner.api.api.query.prop.revisions.MockApiRevisions;
import org.wpcleaner.api.hook.login.WPCleanerConfigurationExpectations;
import org.wpcleaner.api.repository.configuration.ConfigurationParser;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.lib.check.CheckAlgorithmRegistry;

public final class CheckWikiConfigurationExpectations {

  private CheckWikiConfigurationExpectations() {}

  public static void givenParametersPages(
      final MockApiRevisions mockApiRevisions,
      final CheckAlgorithmRegistry algorithmRegistry,
      final WikiDefinition wiki,
      final String translation,
      final Map<Integer, String> contentByNumber) {
    final List<Integer> numbers =
        CheckWikiConfiguration.of(
                ConfigurationParser.parse(translation),
                Map.of(),
                String.valueOf(wiki.checkWikiCode()))
            .algorithms()
            .values()
            .stream()
            .filter(CheckAlgorithmConfiguration::isActive)
            .map(CheckAlgorithmConfiguration::number)
            .filter(number -> algorithmRegistry.findAlgorithm(number).isPresent())
            .sorted()
            .toList();
    if (!numbers.isEmpty()) {
      WPCleanerConfigurationExpectations.givenPages(
          mockApiRevisions,
          wiki,
          numbers.stream().map(CheckWikiConfigurationExpectations::getParametersPage).toList(),
          numbers.stream()
              .filter(contentByNumber::containsKey)
              .collect(
                  Collectors.toMap(
                      CheckWikiConfigurationExpectations::getParametersPage,
                      contentByNumber::get)));
    }
  }

  private static String getParametersPage(final int number) {
    return CheckAlgorithmParametersReader.getParametersPage(List.of(), number);
  }
}
