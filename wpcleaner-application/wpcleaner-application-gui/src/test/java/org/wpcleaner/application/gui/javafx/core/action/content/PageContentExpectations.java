package org.wpcleaner.application.gui.javafx.core.action.content;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.wpcleaner.api.api.query.prop.revisions.MockApiRevisions;
import org.wpcleaner.api.api.query.prop.revisions.Page;
import org.wpcleaner.api.api.query.prop.revisions.Revision;
import org.wpcleaner.api.api.query.prop.revisions.RevisionSlot;
import org.wpcleaner.api.api.query.prop.revisions.RevisionsParameters;
import org.wpcleaner.api.api.query.prop.revisions.RevisionsQuery;
import org.wpcleaner.api.wiki.definition.WikiDefinition;

public final class PageContentExpectations {

  private static final RevisionsQuery CONTENT_QUERY =
      RevisionsQuery.emptyBuilder()
          .properties(
              Set.of(RevisionsParameters.Properties.CONTENT, RevisionsParameters.Properties.IDS))
          .slots(Set.of("main"))
          .build();

  private PageContentExpectations() {}

  public static void givenPageContent(
      final MockApiRevisions mockApiRevisions,
      final WikiDefinition wiki,
      final String title,
      final String content) {
    mockApiRevisions
        .givenRetrieveRevisionsByTitle(wiki, List.of(title), CONTENT_QUERY)
        .willReturn(List.of(createPage(title, content)));
  }

  public static void givenPageContentFailure(
      final MockApiRevisions mockApiRevisions, final WikiDefinition wiki, final String title) {
    mockApiRevisions.givenRetrieveRevisionsByTitle(wiki, List.of(title), CONTENT_QUERY).willFail();
  }

  private static Page createPage(final String title, final String content) {
    final Revision revision =
        new Revision(
            null,
            false,
            null,
            null,
            123,
            null,
            null,
            Map.of("main", new RevisionSlot(content, null, null, null)),
            List.of(),
            null,
            null,
            null);
    return new Page(false, 2, 1234, List.of(revision), title);
  }
}
