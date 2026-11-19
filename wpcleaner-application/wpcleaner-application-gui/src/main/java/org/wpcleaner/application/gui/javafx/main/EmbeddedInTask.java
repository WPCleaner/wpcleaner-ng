package org.wpcleaner.application.gui.javafx.main;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import org.wpcleaner.api.api.Limit;
import org.wpcleaner.api.api.query.list.embeddedin.ApiEmbeddedIn;
import org.wpcleaner.api.api.query.list.embeddedin.EmbeddedInQuery;
import org.wpcleaner.api.api.query.list.embeddedin.EmbeddingPage;
import org.wpcleaner.api.progress.LongRunningTask;
import org.wpcleaner.api.progress.ProgressStep;
import org.wpcleaner.api.progress.ProgressTracker;
import org.wpcleaner.api.utils.GT;
import org.wpcleaner.api.wiki.definition.WikiDefinition;

record EmbeddedInTask(ApiEmbeddedIn apiEmbeddedIn, WikiDefinition wiki, String templateTitle)
    implements LongRunningTask<List<String>> {

  @Override
  public List<String> call(final ProgressTracker tracker) {
    try (ProgressStep _ = tracker.start(GT._T("Retrieving pages embedding %s", templateTitle))) {
      return apiEmbeddedIn
          .retrieveEmbeddedInByTitle(
              wiki, templateTitle, EmbeddedInQuery.emptyBuilder().limit(Limit.max()).build())
          .stream()
          .sorted(
              Comparator.comparingInt(EmbeddingPage::ns)
                  .thenComparing(
                      EmbeddingPage::title, Comparator.nullsLast(Comparator.naturalOrder())))
          .flatMap(page -> Stream.ofNullable(page.title()))
          .distinct()
          .toList();
    }
  }
}
