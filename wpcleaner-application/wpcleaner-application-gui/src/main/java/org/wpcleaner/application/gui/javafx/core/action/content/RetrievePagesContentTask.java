package org.wpcleaner.application.gui.javafx.core.action.content;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.wpcleaner.api.api.query.prop.revisions.ApiRevisions;
import org.wpcleaner.api.api.query.prop.revisions.Page;
import org.wpcleaner.api.api.query.prop.revisions.RevisionSlot;
import org.wpcleaner.api.api.query.prop.revisions.RevisionsParameters;
import org.wpcleaner.api.api.query.prop.revisions.RevisionsQuery;
import org.wpcleaner.api.progress.LongRunningTask;
import org.wpcleaner.api.progress.ProgressStep;
import org.wpcleaner.api.progress.ProgressTracker;
import org.wpcleaner.api.utils.GT;
import org.wpcleaner.api.wiki.definition.WikiDefinition;

public final class RetrievePagesContentTask implements LongRunningTask<Map<String, String>> {

  private final WikiDefinition wiki;
  private final List<String> titles;
  private final ApiRevisions apiRevisions;

  public RetrievePagesContentTask(
      final WikiDefinition wiki, final List<String> titles, final ApiRevisions apiRevisions) {
    this.wiki = wiki;
    this.titles = titles;
    this.apiRevisions = apiRevisions;
  }

  @Override
  public Map<String, String> call(final ProgressTracker tracker) {
    try (ProgressStep _ = tracker.start(GT._T("Retrieving contents of %s", titles))) {
      return retrievePagesContent();
    }
  }

  private Map<String, String> retrievePagesContent() {
    final RevisionsQuery query =
        RevisionsQuery.emptyBuilder()
            .properties(
                Set.of(RevisionsParameters.Properties.CONTENT, RevisionsParameters.Properties.IDS))
            .slots(Set.of("main"))
            .build();
    final List<Page> pages = apiRevisions.retrieveRevisionsByTitle(wiki, titles, query);
    return titles.stream()
        .collect(
            Collectors.toMap(
                Function.identity(),
                title ->
                    pages.stream()
                        .filter(page -> Objects.equals(page.title(), title))
                        .findFirst()
                        .map(this::extractContent)
                        .orElse("")));
  }

  private String extractContent(final Page page) {
    return page.revisions().stream()
        .findFirst()
        .map(revision -> revision.slots().get("main"))
        .filter(Objects::nonNull)
        .map(RevisionSlot::content)
        .orElse("");
  }
}
