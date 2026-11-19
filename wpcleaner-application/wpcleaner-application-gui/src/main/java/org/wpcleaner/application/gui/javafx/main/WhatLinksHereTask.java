package org.wpcleaner.application.gui.javafx.main;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import org.wpcleaner.api.api.Limit;
import org.wpcleaner.api.api.query.prop.linkshere.ApiLinksHere;
import org.wpcleaner.api.api.query.prop.linkshere.LinkHere;
import org.wpcleaner.api.api.query.prop.linkshere.LinksHereQuery;
import org.wpcleaner.api.api.query.prop.linkshere.Page;
import org.wpcleaner.api.progress.LongRunningTask;
import org.wpcleaner.api.progress.ProgressStep;
import org.wpcleaner.api.progress.ProgressTracker;
import org.wpcleaner.api.utils.GT;
import org.wpcleaner.api.wiki.definition.WikiDefinition;

record WhatLinksHereTask(ApiLinksHere apiLinksHere, WikiDefinition wiki, String pageName)
    implements LongRunningTask<List<String>> {

  @Override
  public List<String> call(final ProgressTracker tracker) {
    try (ProgressStep _ = tracker.start(GT._T("Retrieving links to %s", pageName))) {
      return apiLinksHere
          .retrieveLinksHereByTitle(
              wiki, List.of(pageName), LinksHereQuery.emptyBuilder().limit(Limit.max()).build())
          .stream()
          .map(Page::linksHere)
          .flatMap(List::stream)
          .sorted(
              Comparator.comparingInt(LinkHere::ns)
                  .thenComparing(LinkHere::title, Comparator.nullsLast(Comparator.naturalOrder())))
          .flatMap(linkHere -> Stream.ofNullable(linkHere.title()))
          .distinct()
          .toList();
    }
  }
}
