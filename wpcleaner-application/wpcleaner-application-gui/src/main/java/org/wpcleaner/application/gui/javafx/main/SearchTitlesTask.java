package org.wpcleaner.application.gui.javafx.main;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Set;
import org.wpcleaner.api.analysis.pagetitle.PageTitle;
import org.wpcleaner.api.analysis.pagetitle.PageTitleNormalizer;
import org.wpcleaner.api.api.Limit;
import org.wpcleaner.api.api.query.list.search.ApiSearch;
import org.wpcleaner.api.api.query.list.search.SearchPage;
import org.wpcleaner.api.api.query.list.search.SearchQuery;
import org.wpcleaner.api.progress.LongRunningTask;
import org.wpcleaner.api.progress.ProgressStep;
import org.wpcleaner.api.progress.ProgressTracker;
import org.wpcleaner.api.repository.namespace.Namespace;
import org.wpcleaner.api.utils.GT;
import org.wpcleaner.api.wiki.definition.WikiDefinition;

record SearchTitlesTask(
    ApiSearch apiSearch, WikiDefinition wiki, List<Namespace> namespaces, String pageName)
    implements LongRunningTask<List<String>> {

  @Override
  public List<String> call(final ProgressTracker tracker) {
    try (ProgressStep _ = tracker.start(GT._T("Searching titles containing %s", pageName))) {
      final PageTitle title =
          new PageTitleNormalizer(namespaces)
              .normalize(pageName, Namespace.MAIN.id())
              .orElseGet(() -> new PageTitle(Namespace.MAIN.id(), pageName));
      final SearchQuery options =
          SearchQuery.emptyBuilder()
              .limit(Limit.max())
              .namespace(
                  Set.of(
                      Namespace.findNamespace(namespaces, title.namespace())
                          .orElse(Namespace.MAIN)))
              .build();
      return apiSearch
          .searchPages(wiki, "intitle:\"" + title.text().replace("\"", "\\\"") + "\"", options)
          .stream()
          .map(SearchPage::title)
          .distinct()
          .sorted()
          .toList();
    }
  }
}
