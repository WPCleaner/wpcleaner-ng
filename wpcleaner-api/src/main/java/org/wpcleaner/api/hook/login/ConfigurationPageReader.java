package org.wpcleaner.api.hook.login;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.wpcleaner.api.api.query.Slot;
import org.wpcleaner.api.api.query.prop.revisions.ApiRevisions;
import org.wpcleaner.api.api.query.prop.revisions.Page;
import org.wpcleaner.api.api.query.prop.revisions.Revision;
import org.wpcleaner.api.api.query.prop.revisions.RevisionSlot;
import org.wpcleaner.api.api.query.prop.revisions.RevisionsParameters;
import org.wpcleaner.api.api.query.prop.revisions.RevisionsQuery;
import org.wpcleaner.api.repository.configuration.ConfigurationParser;
import org.wpcleaner.api.wiki.definition.WikiDefinition;

@Service
public class ConfigurationPageReader {

  public static final RevisionsQuery CONTENT_QUERY =
      RevisionsQuery.emptyBuilder()
          .properties(Set.of(RevisionsParameters.Properties.CONTENT))
          .slots(Set.of(Slot.MAIN))
          .build();

  private final ApiRevisions apiRevisions;

  public ConfigurationPageReader(final ApiRevisions apiRevisions) {
    this.apiRevisions = apiRevisions;
  }

  public Map<String, String> retrieveProperties(final WikiDefinition wiki, final String title) {
    return apiRevisions.retrieveRevisionsByTitle(wiki, List.of(title), CONTENT_QUERY).stream()
        .filter(page -> !page.missing())
        .map(Page::revisions)
        .flatMap(List::stream)
        .findFirst()
        .flatMap(Revision::mainSlot)
        .map(RevisionSlot::content)
        .map(ConfigurationParser::parse)
        .orElse(Map.of());
  }
}
