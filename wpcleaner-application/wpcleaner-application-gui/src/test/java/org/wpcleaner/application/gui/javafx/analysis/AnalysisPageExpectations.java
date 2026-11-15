package org.wpcleaner.application.gui.javafx.analysis;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.wpcleaner.api.api.query.Slot;
import org.wpcleaner.api.api.query.prop.revisions.MockApiRevisions;
import org.wpcleaner.api.api.query.prop.revisions.Page;
import org.wpcleaner.api.api.query.prop.revisions.Revision;
import org.wpcleaner.api.api.query.prop.revisions.RevisionSlot;
import org.wpcleaner.api.api.query.prop.revisions.RevisionsParameters;
import org.wpcleaner.api.api.query.prop.revisions.RevisionsQuery;
import org.wpcleaner.api.wiki.definition.WikiDefinition;

final class AnalysisPageExpectations {

  private static final RevisionsQuery CONTENT_QUERY =
      RevisionsQuery.emptyBuilder()
          .properties(
              Set.of(
                  RevisionsParameters.Properties.CONTENT, RevisionsParameters.Properties.TIMESTAMP))
          .slots(Set.of(Slot.MAIN))
          .build();

  private AnalysisPageExpectations() {}

  static void givenPageContent(
      final MockApiRevisions mockApiRevisions,
      final WikiDefinition wiki,
      final String title,
      final String text,
      final Instant timestamp) {
    final Revision revision =
        new Revision(
            null,
            false,
            null,
            null,
            123,
            null,
            null,
            Map.of(Slot.MAIN.value, new RevisionSlot(text, null, null, null)),
            List.of(),
            timestamp,
            null,
            null);
    mockApiRevisions
        .givenRetrieveRevisionsByTitle(wiki, List.of(title), CONTENT_QUERY)
        .willReturn(List.of(new Page(false, 0, 1234, List.of(revision), title)));
  }
}
