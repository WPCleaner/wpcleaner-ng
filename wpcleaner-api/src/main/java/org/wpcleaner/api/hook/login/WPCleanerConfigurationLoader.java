package org.wpcleaner.api.hook.login;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.wpcleaner.api.api.query.prop.revisions.ApiRevisions;
import org.wpcleaner.api.api.query.prop.revisions.Page;
import org.wpcleaner.api.api.query.prop.revisions.RevisionSlot;
import org.wpcleaner.api.api.query.prop.revisions.RevisionsParameters;
import org.wpcleaner.api.api.query.prop.revisions.RevisionsQuery;
import org.wpcleaner.api.repository.configuration.ConfigurationParser;
import org.wpcleaner.api.repository.configuration.WPCleanerConfiguration;
import org.wpcleaner.api.repository.configuration.WPCleanerConfigurationRepository;
import org.wpcleaner.api.wiki.definition.WikiDefinition;

@Service
public class WPCleanerConfigurationLoader {

  public static final String GENERAL_USERNAME = "NicoV";
  private static final String MAIN_SLOT = "main";
  public static final RevisionsQuery CONTENT_QUERY =
      RevisionsQuery.emptyBuilder()
          .properties(Set.of(RevisionsParameters.Properties.CONTENT))
          .slots(Set.of(MAIN_SLOT))
          .build();

  private final ApiRevisions apiRevisions;
  private final WPCleanerConfigurationRepository repository;

  public WPCleanerConfigurationLoader(
      final ApiRevisions apiRevisions, final WPCleanerConfigurationRepository repository) {
    this.apiRevisions = apiRevisions;
    this.repository = repository;
  }

  public static String getConfigurationPage(final String username) {
    return "User:%s/WikiCleanerConfiguration".formatted(username);
  }

  public void load(final WikiDefinition wiki, final String username) {
    final String generalPage = getConfigurationPage(GENERAL_USERNAME);
    final String userPage = getConfigurationPage(username);
    repository.setConfiguration(
        wiki,
        new WPCleanerConfiguration(
            retrieveProperties(wiki, generalPage),
            userPage.equals(generalPage) ? Map.of() : retrieveProperties(wiki, userPage)));
  }

  private Map<String, String> retrieveProperties(final WikiDefinition wiki, final String title) {
    return apiRevisions.retrieveRevisionsByTitle(wiki, List.of(title), CONTENT_QUERY).stream()
        .filter(page -> !page.missing())
        .map(Page::revisions)
        .flatMap(List::stream)
        .findFirst()
        .flatMap(revision -> Optional.ofNullable(revision.slots().get(MAIN_SLOT)))
        .map(RevisionSlot::content)
        .map(ConfigurationParser::parse)
        .orElse(Map.of());
  }
}
