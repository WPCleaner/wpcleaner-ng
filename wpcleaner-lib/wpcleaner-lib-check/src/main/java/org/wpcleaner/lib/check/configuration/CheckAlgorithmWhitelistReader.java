package org.wpcleaner.lib.check.configuration;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

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
import org.wpcleaner.api.repository.namespace.CommonNamespaces;
import org.wpcleaner.api.repository.namespace.NamespaceRepository;
import org.wpcleaner.api.wiki.definition.WikiDefinition;

@Service
public class CheckAlgorithmWhitelistReader {

  private final PageAnalysisFactory analysisFactory;
  private final NamespaceRepository namespaceRepository;
  private final ConfigurationPageReader pageReader;

  public CheckAlgorithmWhitelistReader(
      final PageAnalysisFactory analysisFactory,
      final NamespaceRepository namespaceRepository,
      final ConfigurationPageReader pageReader) {
    this.analysisFactory = analysisFactory;
    this.namespaceRepository = namespaceRepository;
    this.pageReader = pageReader;
  }

  public Function<CheckAlgorithmConfiguration, Set<PageTitle>> retrieveWhitelists(
      final WikiDefinition wiki, final List<CheckAlgorithmConfiguration> algorithms) {
    final PageTitleNormalizer normalizer =
        new PageTitleNormalizer(namespaceRepository.getNamespaces(wiki));
    final List<String> whitelistPages =
        algorithms.stream()
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
