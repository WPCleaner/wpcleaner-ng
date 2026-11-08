package org.wpcleaner.api.api.query.prop.revisions;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.client.MockRestServiceServer;
import org.wpcleaner.api.ApiSpringBootTest;
import org.wpcleaner.api.api.Limit;
import org.wpcleaner.api.api.query.Slot;
import org.wpcleaner.api.hook.login.WPCleanerConfigurationExpectations;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.api.wiki.definition.WikipediaDefinitions;

@ApiSpringBootTest
class ApiRevisionsContinuationTest {

  private static final WikiDefinition WIKI = WikipediaDefinitions.FR;
  private static final String FIRST_TITLE = "Liste blanche 001";
  private static final String SECOND_TITLE = "Liste blanche 002";
  private static final Map<String, String> CONTINUATION =
      Map.of("rvcontinue", "1234|5678", "continue", "||");

  @Autowired private ApiRevisions apiRevisions;
  @Autowired private MockApiRevisions mockApiRevisions;
  @Autowired private MockRestServiceServer mediaWikiServer;

  @BeforeEach
  void setUp() {
    mediaWikiServer.reset();
  }

  @DisplayName("should continue the retrieval until all the pages are read")
  @Test
  void retrieveRevisionsWithContinuation() {
    // GIVEN
    final RevisionsQuery query =
        RevisionsQuery.emptyBuilder()
            .properties(Set.of(RevisionsParameters.Properties.CONTENT))
            .slots(Set.of(Slot.MAIN))
            .build();
    final List<String> titles = List.of(FIRST_TITLE, SECOND_TITLE);
    mockApiRevisions
        .givenRetrieveRevisionsByTitle(WIKI, titles, query)
        .willReturn(
            List.of(
                WPCleanerConfigurationExpectations.createPage(FIRST_TITLE, "* [[Paris]]"),
                withoutRevisions(SECOND_TITLE)),
            CONTINUATION);
    mockApiRevisions
        .givenRetrieveRevisionsByTitle(WIKI, titles, query, CONTINUATION)
        .willReturn(
            List.of(
                withoutRevisions(FIRST_TITLE),
                WPCleanerConfigurationExpectations.createPage(SECOND_TITLE, "* [[Lyon]]")));

    // WHEN
    final List<Page> pages = apiRevisions.retrieveRevisionsByTitle(WIKI, titles, query);

    // THEN
    mediaWikiServer.verify();
    Assertions.assertThat(pages)
        .extracting(Page::title, ApiRevisionsContinuationTest::extractContent)
        .containsExactly(
            Assertions.tuple(FIRST_TITLE, Optional.of("* [[Paris]]")),
            Assertions.tuple(SECOND_TITLE, Optional.of("* [[Lyon]]")));
  }

  @DisplayName("should not continue the retrieval when enumerating the revisions of a page")
  @Test
  void retrieveRevisionsWithLimit() {
    // GIVEN
    final RevisionsQuery query = RevisionsQuery.emptyBuilder().limit(Limit.of(1)).build();
    final List<String> titles = List.of(FIRST_TITLE);
    mockApiRevisions
        .givenRetrieveRevisionsByTitle(WIKI, titles, query)
        .willReturn(
            List.of(WPCleanerConfigurationExpectations.createPage(FIRST_TITLE, "* [[Paris]]")),
            CONTINUATION);

    // WHEN
    final List<Page> pages = apiRevisions.retrieveRevisionsByTitle(WIKI, titles, query);

    // THEN
    mediaWikiServer.verify();
    Assertions.assertThat(pages)
        .singleElement()
        .satisfies(page -> Assertions.assertThat(page.revisions()).hasSize(1));
  }

  private static Page withoutRevisions(final String title) {
    return new Page(false, 4, 1234, List.of(), title);
  }

  private static Optional<String> extractContent(final Page page) {
    return page.revisions().stream()
        .findFirst()
        .flatMap(Revision::mainSlot)
        .map(RevisionSlot::content);
  }
}
