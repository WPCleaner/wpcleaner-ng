package org.wpcleaner.application.gui.javafx.core.action.content;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Objects;
import java.util.Set;
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

public final class RetrievePageContentTask implements LongRunningTask<String> {

  private final WikiDefinition wiki;
  private final String title;
  private final ApiRevisions apiRevisions;

  public RetrievePageContentTask(
      final WikiDefinition wiki, final String title, final ApiRevisions apiRevisions) {
    this.wiki = wiki;
    this.title = title;
    this.apiRevisions = apiRevisions;
  }

  @Override
  public String call(final ProgressTracker tracker) {
    try (ProgressStep _ = tracker.start(GT._T("Retrieving contents of %s", title))) {
      final RevisionsQuery query =
          RevisionsQuery.emptyBuilder()
              .properties(
                  Set.of(
                      RevisionsParameters.Properties.CONTENT, RevisionsParameters.Properties.IDS))
              .slots(Set.of("main"))
              .build();
      final List<Page> pages = apiRevisions.retrieveRevisionsByTitle(wiki, List.of(title), query);
      return pages.stream()
          .filter(page -> Objects.equals(page.title(), title))
          .findFirst()
          .map(this::extractContent)
          .orElse("");
    }
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
