package org.wpcleaner.application.gui.javafx.main;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Comparator;
import java.util.List;
import org.wpcleaner.api.api.Limit;
import org.wpcleaner.api.api.query.prop.links.ApiLinks;
import org.wpcleaner.api.api.query.prop.links.Link;
import org.wpcleaner.api.api.query.prop.links.LinksQuery;
import org.wpcleaner.api.api.query.prop.links.Page;
import org.wpcleaner.api.progress.LongRunningTask;
import org.wpcleaner.api.progress.ProgressStep;
import org.wpcleaner.api.progress.ProgressTracker;
import org.wpcleaner.api.repository.namespace.Namespace;
import org.wpcleaner.api.utils.GT;
import org.wpcleaner.api.wiki.definition.WikiDefinition;

record InternalLinksTask(
    ApiLinks apiLinks,
    WikiDefinition wiki,
    List<Namespace> namespaces,
    String pageName,
    InternalLinksMode mode)
    implements LongRunningTask<List<String>> {

  @Override
  public List<String> call(final ProgressTracker tracker) {
    try (ProgressStep _ = tracker.start(GT._T("Retrieving internal links in %s", pageName))) {
      return apiLinks
          .retrieveLinksByTitle(
              wiki, List.of(pageName), LinksQuery.emptyBuilder().limit(Limit.max()).build())
          .stream()
          .map(Page::links)
          .flatMap(List::stream)
          .map(this::convertTalkPage)
          .filter(link -> mode == InternalLinksMode.ALL || link.ns() == Namespace.MAIN.id())
          .sorted(Comparator.comparingInt(Link::ns).thenComparing(Link::title))
          .map(Link::title)
          .distinct()
          .toList();
    }
  }

  private Link convertTalkPage(final Link link) {
    if (mode != InternalLinksMode.TALK_PAGES_CONVERTED || link.ns() % 2 == 0) {
      return link;
    }
    final int subjectNamespace = link.ns() - 1;
    final String text = link.title().substring(link.title().indexOf(':') + 1);
    final String prefix =
        Namespace.findNamespace(namespaces, subjectNamespace).map(Namespace::name).orElse("");
    return new Link(subjectNamespace, prefix.isEmpty() ? text : prefix + ":" + text);
  }
}
