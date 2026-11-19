package org.wpcleaner.api.api.query.prop.linkshere;

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
class MockApiLinksHereTest {

  private static final WikiDefinition WIKI = WikipediaDefinitions.FR;
  private static final List<String> TITLES = List.of("Paris", "Page inexistante");
  private static final Map<String, String> CONTINUATION =
      Map.of("lhcontinue", "Paris|1406560", "continue", "||");
  private static final LinkHere LYON = new LinkHere(0, 1_406_560, false, "Lyon");
  private static final LinkHere BORDEAUX = new LinkHere(0, 681_160, false, "Bordeaux");

  @Autowired private ApiLinksHere apiLinksHere;
  @Autowired private MockApiLinksHere mockApiLinksHere;
  @Autowired private MockRestServiceServer mediaWikiServer;

  @BeforeEach
  void setUp() {
    mediaWikiServer.reset();
  }

  @DisplayName("Retrieve links here by title returns the pages given to the mock")
  @Test
  void retrieveLinksHereByTitle() {
    final LinksHereQuery options =
        LinksHereQuery.emptyBuilder()
            .lhContinue("Paris|1406560")
            .limit(Limit.max())
            .namespace(Set.of(Namespace.MAIN))
            .properties(Set.of(LinksHereParameters.Properties.TITLE))
            .show(Set.of(LinksHereParameters.Show.NOT_REDIRECT))
            .build();
    final List<Page> pages =
        List.of(
            new Page(List.of(LYON), false, 0, 681_159, "Paris"),
            new Page(List.of(), true, 0, null, "Page inexistante"));
    mockApiLinksHere.givenRetrieveLinksHereByTitle(WIKI, TITLES, options).willReturn(pages);

    final List<Page> result = apiLinksHere.retrieveLinksHereByTitle(WIKI, TITLES, options);

    mediaWikiServer.verify();
    Assertions.assertThat(result).isEqualTo(pages);
  }

  @DisplayName("Retrieve links here by page ID returns the pages given to the mock")
  @Test
  void retrieveLinksHereByPageId() {
    final List<Page> pages =
        List.of(
            new Page(
                List.of(new LinkHere(2, 42, true, "Utilisateur:Jane")),
                false,
                0,
                681_159,
                "Paris"));
    mockApiLinksHere.givenRetrieveLinksHereByPageId(WIKI, List.of(681_159), null).willReturn(pages);

    final List<Page> result = apiLinksHere.retrieveLinksHereByPageId(WIKI, List.of(681_159), null);

    mediaWikiServer.verify();
    Assertions.assertThat(result).isEqualTo(pages);
  }

  @DisplayName("Retrieve links here follows the continuation and merges the links of each page")
  @Test
  void retrieveLinksHereWithContinuation() {
    final List<String> titles = List.of("Paris");
    mockApiLinksHere
        .givenRetrieveLinksHereByTitle(WIKI, titles, null)
        .willReturn(List.of(new Page(List.of(BORDEAUX), false, 0, 681_159, "Paris")), CONTINUATION);
    mockApiLinksHere
        .givenRetrieveLinksHereByTitle(WIKI, titles, null, CONTINUATION)
        .willReturn(List.of(new Page(List.of(LYON), false, 0, 681_159, "Paris")));

    final List<Page> result = apiLinksHere.retrieveLinksHereByTitle(WIKI, titles, null);

    mediaWikiServer.verify();
    Assertions.assertThat(result)
        .singleElement()
        .extracting(Page::linksHere)
        .isEqualTo(List.of(BORDEAUX, LYON));
  }

  @DisplayName("Retrieve links here doesn't follow the continuation when an explicit one is given")
  @Test
  void retrieveLinksHereWithExplicitContinuation() {
    final List<String> titles = List.of("Paris");
    final LinksHereQuery options = LinksHereQuery.emptyBuilder().lhContinue("Paris|681160").build();
    final List<Page> pages = List.of(new Page(List.of(BORDEAUX), false, 0, 681_159, "Paris"));
    mockApiLinksHere
        .givenRetrieveLinksHereByTitle(WIKI, titles, options)
        .willReturn(pages, CONTINUATION);

    final List<Page> result = apiLinksHere.retrieveLinksHereByTitle(WIKI, titles, options);

    mediaWikiServer.verify();
    Assertions.assertThat(result).isEqualTo(pages);
  }

  @DisplayName("Retrieve links here by title fails when the mock fails")
  @Test
  void retrieveLinksHereByTitleWhenFailing() {
    mockApiLinksHere.givenRetrieveLinksHereByTitle(WIKI, TITLES, null).willFail();

    Assertions.assertThatThrownBy(() -> apiLinksHere.retrieveLinksHereByTitle(WIKI, TITLES, null))
        .isInstanceOf(HttpServerErrorException.class);
  }
}
