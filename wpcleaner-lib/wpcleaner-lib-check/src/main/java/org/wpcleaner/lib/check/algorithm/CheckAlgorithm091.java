package org.wpcleaner.lib.check.algorithm;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.wpcleaner.api.analysis.PageAnalysis;
import org.wpcleaner.api.analysis.externallink.ExternalLinkElement;
import org.wpcleaner.api.analysis.internallink.InternalLinkBuilder;
import org.wpcleaner.api.repository.interwiki.Interwiki;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.lib.check.CheckAlgorithm;
import org.wpcleaner.lib.check.CheckError;
import org.wpcleaner.lib.check.Replacement;
import org.wpcleaner.lib.check.configuration.CheckAlgorithmConfiguration;
import org.wpcleaner.lib.check.configuration.CheckWikiConfigurationRepository;
import org.wpcleaner.lib.check.configuration.TemplateDefinition;

@Component
public class CheckAlgorithm091 implements CheckAlgorithm {

  private static final int ALGORITHM_NUMBER = 91;
  private static final String PARAMETER_LINK_TEMPLATES = "link_templates";
  private static final String PARAMETER_ONLY_LANGUAGE = "only_language";
  private static final String PARAMETER_ONLY_LOCAL = "only_local";

  private final CheckWikiConfigurationRepository checkWikiRepository;

  public CheckAlgorithm091(final CheckWikiConfigurationRepository checkWikiRepository) {
    this.checkWikiRepository = checkWikiRepository;
  }

  @Override
  public int number() {
    return ALGORITHM_NUMBER;
  }

  @Override
  public List<CheckError> analyze(final PageAnalysis analysis) {
    if (analysis.getPageTitle().namespace() % 2 != 0) {
      return List.of();
    }
    final CheckAlgorithmConfiguration configuration =
        checkWikiRepository
            .getConfiguration(analysis.getWiki())
            .getAlgorithm(ALGORITHM_NUMBER)
            .orElseGet(() -> new CheckAlgorithmConfiguration(ALGORITHM_NUMBER, Map.of()));
    final boolean onlyLanguage = configuration.getBoolean(PARAMETER_ONLY_LANGUAGE, true);
    final boolean onlyLocal = configuration.getBoolean(PARAMETER_ONLY_LOCAL, true);
    final List<Interwiki> interwikis =
        analysis.getInterwikis().stream()
            .filter(interwiki -> !onlyLanguage || interwiki.language() != null)
            .filter(interwiki -> !onlyLocal || interwiki.local())
            .filter(interwiki -> !isWikiItself(analysis.getWiki(), interwiki))
            .sorted(
                Comparator.comparing((Interwiki interwiki) -> interwiki.language() == null)
                    .thenComparing(interwiki -> interwiki.deprecated() != null))
            .toList();
    final List<TemplateDefinition> linkTemplates =
        configuration.getTemplateDefinitions(
            PARAMETER_LINK_TEMPLATES, List.of(ArticleLink.URL_FIELD, ArticleLink.TEXT_FIELD));
    return analysis.getExternalLinks().stream()
        .flatMap(
            link ->
                findInterwikiUrl(interwikis, link).stream()
                    .map(found -> createError(analysis, link, found, linkTemplates)))
        .sorted(Comparator.comparingInt(CheckError::begin))
        .toList();
  }

  private static boolean isWikiItself(final WikiDefinition wiki, final Interwiki interwiki) {
    return ArticleUrl.findHost(interwiki)
        .filter(host -> wiki.hosts().stream().anyMatch(host::equalsIgnoreCase))
        .isPresent();
  }

  private static Optional<InterwikiUrl> findInterwikiUrl(
      final List<Interwiki> interwikis, final ExternalLinkElement link) {
    return interwikis.stream()
        .map(
            interwiki ->
                ArticleUrl.of(interwiki, link.target())
                    .map(url -> new InterwikiUrl(interwiki, url))
                    .orElse(null))
        .filter(Objects::nonNull)
        .findFirst();
  }

  private static CheckError createError(
      final PageAnalysis analysis,
      final ExternalLinkElement link,
      final InterwikiUrl found,
      final List<TemplateDefinition> linkTemplates) {
    final ArticleLink articleLink = ArticleLink.of(analysis, link, found.url(), linkTemplates);
    final String text = articleLink.text();
    final boolean automatic =
        found.interwiki().language() == null
            && link.bracketed()
            && text != null
            && found.url().attributes().isEmpty();
    final String replacement =
        InternalLinkBuilder.to(found.interwiki().prefix() + ":" + found.url().title())
            .withAnchor(found.url().fragment())
            .withColon(true)
            .withText(text == null ? found.url().title() : text)
            .build();
    return new CheckError(
        ALGORITHM_NUMBER,
        articleLink.begin(),
        articleLink.end(),
        List.of(new Replacement(replacement, automatic)));
  }

  private record InterwikiUrl(Interwiki interwiki, ArticleUrl url) {}
}
