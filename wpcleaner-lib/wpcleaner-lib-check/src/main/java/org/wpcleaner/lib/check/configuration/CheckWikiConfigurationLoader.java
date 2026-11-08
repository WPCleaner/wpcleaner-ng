package org.wpcleaner.lib.check.configuration;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import org.wpcleaner.api.analysis.PageAnalysisFactory;
import org.wpcleaner.api.analysis.pagetitle.PageTitle;
import org.wpcleaner.api.analysis.pagetitle.PageTitleNormalizer;
import org.wpcleaner.api.hook.login.ConfigurationPageReader;
import org.wpcleaner.api.hook.login.LoginStep;
import org.wpcleaner.api.progress.ProgressStep;
import org.wpcleaner.api.progress.ProgressTracker;
import org.wpcleaner.api.repository.configuration.WPCleanerConfiguration;
import org.wpcleaner.api.repository.configuration.WPCleanerConfigurationRepository;
import org.wpcleaner.api.repository.configuration.WPCleanerParameter;
import org.wpcleaner.api.repository.namespace.CommonNamespaces;
import org.wpcleaner.api.repository.namespace.NamespaceRepository;
import org.wpcleaner.api.utils.GT;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.lib.check.CheckAlgorithmRegistry;

@Service
public class CheckWikiConfigurationLoader implements LoginStep {

  private final CheckAlgorithmRegistry algorithmRegistry;
  private final PageAnalysisFactory analysisFactory;
  private final NamespaceRepository namespaceRepository;
  private final ConfigurationPageReader pageReader;
  private final CheckWikiConfigurationRepository repository;
  private final WPCleanerConfigurationRepository wpcleanerRepository;

  public CheckWikiConfigurationLoader(
      final CheckAlgorithmRegistry algorithmRegistry,
      final PageAnalysisFactory analysisFactory,
      final NamespaceRepository namespaceRepository,
      final ConfigurationPageReader pageReader,
      final CheckWikiConfigurationRepository repository,
      final WPCleanerConfigurationRepository wpcleanerRepository) {
    this.algorithmRegistry = algorithmRegistry;
    this.analysisFactory = analysisFactory;
    this.namespaceRepository = namespaceRepository;
    this.pageReader = pageReader;
    this.repository = repository;
    this.wpcleanerRepository = wpcleanerRepository;
  }

  @Override
  public void execute(final WikiDefinition wiki, final ProgressTracker tracker) {
    final WPCleanerConfiguration wpcleanerConfiguration =
        wpcleanerRepository.getConfiguration(wiki);
    final String code = wiki.checkWikiCode();
    final Optional<String> translationPage =
        wpcleanerConfiguration.getString(WPCleanerParameter.CHECK_WIKI_TRANSLATION_PAGE);
    if (code == null || translationPage.isEmpty()) {
      repository.setConfiguration(wiki, CheckWikiConfiguration.EMPTY);
      return;
    }
    try (ProgressStep _ = tracker.start(GT._T("Retrieving Check Wiki configuration"))) {
      final CheckWikiConfiguration configuration =
          CheckWikiConfiguration.of(
              pageReader.retrieveProperties(wiki, translationPage.get()),
              wpcleanerConfiguration.userProperties(),
              code);
      repository.setConfiguration(
          wiki, configuration.withWhitelists(retrieveWhitelists(wiki, configuration)));
    }
  }

  private Function<CheckAlgorithmConfiguration, Set<PageTitle>> retrieveWhitelists(
      final WikiDefinition wiki, final CheckWikiConfiguration configuration) {
    final PageTitleNormalizer normalizer =
        new PageTitleNormalizer(namespaceRepository.getNamespaces(wiki));
    final List<String> whitelistPages =
        configuration.algorithms().values().stream()
            .filter(CheckAlgorithmConfiguration::isActive)
            .filter(algorithm -> algorithmRegistry.findAlgorithm(algorithm.number()).isPresent())
            .sorted(Comparator.comparingInt(CheckAlgorithmConfiguration::number))
            .map(CheckAlgorithmConfiguration::whitelistPage)
            .flatMap(Optional::stream)
            .distinct()
            .toList();
    final Map<PageTitle, Set<PageTitle>> whitelistsByPage = new HashMap<>();
    pageReader
        .retrieveContents(wiki, whitelistPages)
        .forEach(
            (title, content) ->
                normalize(normalizer, title)
                    .ifPresent(
                        pageTitle ->
                            whitelistsByPage.put(pageTitle, extractLinks(wiki, title, content))));
    return algorithm ->
        Stream.concat(
                algorithm.whitelistPage().flatMap(page -> normalize(normalizer, page)).stream()
                    .flatMap(
                        pageTitle -> whitelistsByPage.getOrDefault(pageTitle, Set.of()).stream()),
                algorithm.inlineWhitelist().stream()
                    .flatMap(name -> normalize(normalizer, name).stream()))
            .collect(Collectors.toSet());
  }

  private static Optional<PageTitle> normalize(
      final PageTitleNormalizer normalizer, final String name) {
    return normalizer.normalize(name, CommonNamespaces.MAIN.id);
  }

  private Set<PageTitle> extractLinks(
      final WikiDefinition wiki, final String title, final String content) {
    return analysisFactory.analysis(wiki, title, content).getInternalLinks().stream()
        .flatMap(link -> Stream.ofNullable(link.title()))
        .collect(Collectors.toSet());
  }
}
