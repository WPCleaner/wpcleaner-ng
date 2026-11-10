package org.wpcleaner.lib.check.configuration;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.wpcleaner.api.hook.login.ConfigurationPageReader;
import org.wpcleaner.api.hook.login.LoginStep;
import org.wpcleaner.api.hook.login.LoginWarning;
import org.wpcleaner.api.progress.ProgressStep;
import org.wpcleaner.api.progress.ProgressTracker;
import org.wpcleaner.api.repository.configuration.WPCleanerConfiguration;
import org.wpcleaner.api.repository.configuration.WPCleanerConfigurationRepository;
import org.wpcleaner.api.repository.configuration.WPCleanerParameter;
import org.wpcleaner.api.utils.GT;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.lib.check.CheckAlgorithmRegistry;

@Service
public class CheckWikiConfigurationLoader implements LoginStep {

  private final CheckAlgorithmRegistry algorithmRegistry;
  private final ConfigurationPageReader pageReader;
  private final CheckAlgorithmParametersReader parametersReader;
  private final CheckWikiConfigurationRepository repository;
  private final CheckAlgorithmWhitelistReader whitelistReader;
  private final WPCleanerConfigurationRepository wpcleanerRepository;

  public CheckWikiConfigurationLoader(
      final CheckAlgorithmRegistry algorithmRegistry,
      final ConfigurationPageReader pageReader,
      final CheckAlgorithmParametersReader parametersReader,
      final CheckWikiConfigurationRepository repository,
      final CheckAlgorithmWhitelistReader whitelistReader,
      final WPCleanerConfigurationRepository wpcleanerRepository) {
    this.algorithmRegistry = algorithmRegistry;
    this.pageReader = pageReader;
    this.parametersReader = parametersReader;
    this.repository = repository;
    this.whitelistReader = whitelistReader;
    this.wpcleanerRepository = wpcleanerRepository;
  }

  @Override
  public List<LoginWarning> execute(final WikiDefinition wiki, final ProgressTracker tracker) {
    final WPCleanerConfiguration wpcleanerConfiguration =
        wpcleanerRepository.getConfiguration(wiki);
    final String code = wiki.checkWikiCode();
    final Optional<String> translationPage =
        wpcleanerConfiguration.getString(WPCleanerParameter.CHECK_WIKI_TRANSLATION_PAGE);
    if (code == null || translationPage.isEmpty()) {
      repository.setConfiguration(wiki, CheckWikiConfiguration.EMPTY);
      return List.of();
    }
    try (ProgressStep _ = tracker.start(GT._T("Retrieving Check Wiki configuration"))) {
      final CheckWikiConfiguration configuration =
          CheckWikiConfiguration.of(
              pageReader.retrieveProperties(wiki, translationPage.get()),
              wpcleanerConfiguration.userProperties(),
              code);
      final List<CheckAlgorithmConfiguration> loadedAlgorithms = getLoadedAlgorithms(configuration);
      final List<LoginWarning> warnings = new ArrayList<>();
      repository.setConfiguration(
          wiki,
          configuration
              .withWhitelists(whitelistReader.retrieveWhitelists(wiki, loadedAlgorithms))
              .withParameters(
                  parametersReader.retrieveParameters(
                      wiki,
                      loadedAlgorithms.stream().map(CheckAlgorithmConfiguration::number).toList(),
                      warnings::add)));
      return List.copyOf(warnings);
    }
  }

  private List<CheckAlgorithmConfiguration> getLoadedAlgorithms(
      final CheckWikiConfiguration configuration) {
    return configuration.algorithms().values().stream()
        .filter(CheckAlgorithmConfiguration::isActive)
        .filter(algorithm -> algorithmRegistry.findAlgorithm(algorithm.number()).isPresent())
        .sorted(Comparator.comparingInt(CheckAlgorithmConfiguration::number))
        .toList();
  }
}
