package org.wpcleaner.lib.check;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import org.springframework.stereotype.Service;
import org.wpcleaner.api.analysis.PageAnalysis;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.lib.check.configuration.CheckAlgorithmConfiguration;
import org.wpcleaner.lib.check.configuration.CheckWikiConfiguration;
import org.wpcleaner.lib.check.configuration.CheckWikiConfigurationRepository;

@Service
public class CheckAlgorithmRegistry {

  private final List<CheckAlgorithm> algorithms;
  private final CheckWikiConfigurationRepository configurationRepository;

  public CheckAlgorithmRegistry(
      final List<CheckAlgorithm> algorithms,
      final CheckWikiConfigurationRepository configurationRepository) {
    this.algorithms =
        algorithms.stream().sorted(Comparator.comparingInt(CheckAlgorithm::number)).toList();
    this.configurationRepository = configurationRepository;
  }

  public List<CheckAlgorithm> getAlgorithms() {
    return algorithms;
  }

  public List<CheckAlgorithm> getActiveAlgorithms(final WikiDefinition wiki) {
    return getAlgorithms(wiki, CheckAlgorithmConfiguration::isActive);
  }

  public List<CheckAlgorithm> getActiveAlgorithms(final PageAnalysis analysis) {
    return getAlgorithms(
        analysis.getWiki(),
        configuration ->
            configuration.isActive() && !configuration.isWhitelisted(analysis.getPageTitle()));
  }

  private List<CheckAlgorithm> getAlgorithms(
      final WikiDefinition wiki, final Predicate<CheckAlgorithmConfiguration> filter) {
    final CheckWikiConfiguration configuration = configurationRepository.getConfiguration(wiki);
    return algorithms.stream()
        .filter(
            algorithm -> configuration.getAlgorithm(algorithm.number()).filter(filter).isPresent())
        .toList();
  }

  public String getName(final WikiDefinition wiki, final CheckAlgorithm algorithm) {
    return getConfiguration(wiki, algorithm)
        .flatMap(CheckAlgorithmConfiguration::shortDescription)
        .orElseGet(algorithm::name);
  }

  public Optional<String> getDescription(
      final WikiDefinition wiki, final CheckAlgorithm algorithm) {
    return getConfiguration(wiki, algorithm).flatMap(CheckAlgorithmConfiguration::longDescription);
  }

  public Optional<String> getLink(final WikiDefinition wiki, final CheckAlgorithm algorithm) {
    return getConfiguration(wiki, algorithm).flatMap(CheckAlgorithmConfiguration::link);
  }

  private Optional<CheckAlgorithmConfiguration> getConfiguration(
      final WikiDefinition wiki, final CheckAlgorithm algorithm) {
    return configurationRepository.getConfiguration(wiki).getAlgorithm(algorithm.number());
  }

  public Optional<CheckAlgorithm> findAlgorithm(final int number) {
    return algorithms.stream().filter(algorithm -> algorithm.number() == number).findFirst();
  }
}
