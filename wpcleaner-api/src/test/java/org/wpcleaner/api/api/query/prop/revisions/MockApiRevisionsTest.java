package org.wpcleaner.api.api.query.prop.revisions;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.HttpServerErrorException;
import org.wpcleaner.api.ApiSpringBootTest;
import org.wpcleaner.api.api.Limit;
import org.wpcleaner.api.wiki.definition.WikipediaDefinitions;

@ApiSpringBootTest
class MockApiRevisionsTest {

  private static final List<String> TITLES = List.of("Main Page", "Café");

  @Autowired private ApiRevisions apiRevisions;
  @Autowired private MockApiRevisions mockApiRevisions;

  @DisplayName("Retrieve revisions by title returns the pages given to the mock")
  @Test
  void retrieveRevisionsByTitle() {
    final RevisionsQuery options =
        RevisionsQuery.emptyBuilder()
            .contentFormatMain("text/x-wiki")
            .direction(RevisionsParameters.Direction.OLDER)
            .end(Instant.parse("2026-01-01T00:00:00Z"))
            .endId(1)
            .excludeUser("Vandal")
            .limit(Limit.of(1))
            .properties(
                Set.of(
                    RevisionsParameters.Properties.CONTENT,
                    RevisionsParameters.Properties.IDS,
                    RevisionsParameters.Properties.TIMESTAMP))
            .rvContinue("20260101|1")
            .section(2)
            .slots(Set.of("main"))
            .start(Instant.parse("2026-10-28T00:00:00Z"))
            .startId(999)
            .tag("mw-undo")
            .user("Author")
            .build();
    final Revision revision =
        new Revision(
            "Café edit",
            true,
            122,
            "<b>Café</b> edit",
            123,
            "abc",
            12,
            Map.of("main", new RevisionSlot("Main page content", "wikitext", "def", 17)),
            List.of("mw-undo"),
            Instant.parse("2026-10-27T12:34:56Z"),
            "Author",
            42);
    final List<Page> pages =
        List.of(
            new Page(false, 0, 15_580_374, List.of(revision), "Main Page"),
            new Page(true, 0, null, List.of(), "Café"));
    mockApiRevisions
        .givenRetrieveRevisionsByTitle(WikipediaDefinitions.EN, TITLES, options)
        .willReturn(pages);

    final List<Page> result =
        apiRevisions.retrieveRevisionsByTitle(WikipediaDefinitions.EN, TITLES, options);

    Assertions.assertThat(result).isEqualTo(pages);
  }

  @DisplayName("Retrieve revisions by title fails when the mock fails")
  @Test
  void retrieveRevisionsByTitleWhenFailing() {
    mockApiRevisions
        .givenRetrieveRevisionsByTitle(WikipediaDefinitions.EN, TITLES, null)
        .willFail();

    Assertions.assertThatThrownBy(
            () -> apiRevisions.retrieveRevisionsByTitle(WikipediaDefinitions.EN, TITLES, null))
        .isInstanceOf(HttpServerErrorException.class);
  }
}
