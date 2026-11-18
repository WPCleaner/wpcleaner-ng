package org.wpcleaner.api.api.query.prop.links;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpServerErrorException;
import org.wpcleaner.api.ApiSpringBootTest;
import org.wpcleaner.api.api.Limit;
import org.wpcleaner.api.repository.namespace.Namespace;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.api.wiki.definition.WikipediaDefinitions;

@ApiSpringBootTest
class MockApiLinksTest {

  private static final WikiDefinition WIKI = WikipediaDefinitions.FR;
  private static final List<String> TITLES = List.of("Paris", "Page inexistante");
  private static final Map<String, String> CONTINUATION =
      Map.of("plcontinue", "681159|0|Lyon", "continue", "||");

  @Autowired private ApiLinks apiLinks;
  @Autowired private MockApiLinks mockApiLinks;
  @Autowired private MockRestServiceServer mediaWikiServer;

  @BeforeEach
  void setUp() {
    mediaWikiServer.reset();
  }

  @DisplayName("Retrieve links by title returns the pages given to the mock")
  @Test
  void retrieveLinksByTitle() {
    final LinksQuery options =
        LinksQuery.emptyBuilder()
            .direction(LinksParameters.Direction.DESCENDING)
            .limit(Limit.max())
            .namespace(Set.of(Namespace.MAIN))
            .plContinue("681159|0|Lyon")
            .titles(List.of("Lyon"))
            .build();
    final List<Page> pages =
        List.of(
            new Page(List.of(new Link(0, "Lyon")), false, 0, 681_159, "Paris"),
            new Page(List.of(), true, 0, null, "Page inexistante"));
    mockApiLinks.givenRetrieveLinksByTitle(WIKI, TITLES, options).willReturn(pages);

    final List<Page> result = apiLinks.retrieveLinksByTitle(WIKI, TITLES, options);

    mediaWikiServer.verify();
    Assertions.assertThat(result).isEqualTo(pages);
  }

  @DisplayName("Retrieve links by page ID returns the pages given to the mock")
  @Test
  void retrieveLinksByPageId() {
    final List<Page> pages =
        List.of(new Page(List.of(new Link(4, "Wikipédia:Accueil")), false, 0, 681_159, "Paris"));
    mockApiLinks.givenRetrieveLinksByPageId(WIKI, List.of(681_159), null).willReturn(pages);

    final List<Page> result = apiLinks.retrieveLinksByPageId(WIKI, List.of(681_159), null);

    mediaWikiServer.verify();
    Assertions.assertThat(result).isEqualTo(pages);
  }

  @DisplayName("Retrieve links follows the continuation and merges the links of each page")
  @Test
  void retrieveLinksWithContinuation() {
    final List<String> titles = List.of("Paris");
    mockApiLinks
        .givenRetrieveLinksByTitle(WIKI, titles, null)
        .willReturn(
            List.of(new Page(List.of(new Link(0, "Bordeaux")), false, 0, 681_159, "Paris")),
            CONTINUATION);
    mockApiLinks
        .givenRetrieveLinksByTitle(WIKI, titles, null, CONTINUATION)
        .willReturn(List.of(new Page(List.of(new Link(0, "Lyon")), false, 0, 681_159, "Paris")));

    final List<Page> result = apiLinks.retrieveLinksByTitle(WIKI, titles, null);

    mediaWikiServer.verify();
    Assertions.assertThat(result)
        .singleElement()
        .extracting(Page::links)
        .isEqualTo(List.of(new Link(0, "Bordeaux"), new Link(0, "Lyon")));
  }

  @DisplayName("Retrieve links doesn't follow the continuation when an explicit one is given")
  @Test
  void retrieveLinksWithExplicitContinuation() {
    final List<String> titles = List.of("Paris");
    final LinksQuery options = LinksQuery.emptyBuilder().plContinue("681159|0|Bordeaux").build();
    final List<Page> pages =
        List.of(new Page(List.of(new Link(0, "Bordeaux")), false, 0, 681_159, "Paris"));
    mockApiLinks.givenRetrieveLinksByTitle(WIKI, titles, options).willReturn(pages, CONTINUATION);

    final List<Page> result = apiLinks.retrieveLinksByTitle(WIKI, titles, options);

    mediaWikiServer.verify();
    Assertions.assertThat(result).isEqualTo(pages);
  }

  @DisplayName("Retrieve links by title fails when the mock fails")
  @Test
  void retrieveLinksByTitleWhenFailing() {
    mockApiLinks.givenRetrieveLinksByTitle(WIKI, TITLES, null).willFail();

    Assertions.assertThatThrownBy(() -> apiLinks.retrieveLinksByTitle(WIKI, TITLES, null))
        .isInstanceOf(HttpServerErrorException.class);
  }
}
