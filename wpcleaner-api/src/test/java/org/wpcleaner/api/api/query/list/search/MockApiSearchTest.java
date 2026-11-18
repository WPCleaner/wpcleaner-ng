package org.wpcleaner.api.api.query.list.search;

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
import org.wpcleaner.api.repository.namespace.Namespace;
import org.wpcleaner.api.wiki.definition.WikipediaDefinitions;

@ApiSpringBootTest
class MockApiSearchTest {

  private static final String SEARCH = "intitle:\"Café\"";

  @Autowired private ApiSearch apiSearch;
  @Autowired private MockApiSearch mockApiSearch;

  @DisplayName("Search pages returns the pages given to the mock")
  @Test
  void searchPages() {
    final SearchQuery options =
        SearchQuery.emptyBuilder()
            .enableRewrites(true)
            .info(Set.of(SearchParameters.Info.TOTAL_HITS, SearchParameters.Info.SUGGESTION))
            .interwiki(true)
            .limit(Limit.max())
            .namespace(Set.of(Namespace.MAIN))
            .offset(20)
            .properties(
                Set.of(SearchParameters.Properties.SIZE, SearchParameters.Properties.TIMESTAMP))
            .queryDependentProfile(SearchParameters.QueryDependentProfile.ENGINE_AUTOSELECT)
            .queryIndependentProfile(SearchParameters.QueryIndependentProfile.CLASSIC)
            .sort(SearchParameters.Sort.TITLE_NATURAL_ASC)
            .what(SearchParameters.What.TITLE)
            .build();
    final List<SearchPage> pages =
        List.of(
            new SearchPage(
                null,
                Map.of(),
                null,
                0,
                1234,
                null,
                null,
                null,
                null,
                5678,
                null,
                Instant.parse("2026-11-18T10:15:30Z"),
                "Café",
                null,
                null),
            new SearchPage(
                null,
                null,
                null,
                0,
                5678,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                "Café noir",
                null,
                null));
    mockApiSearch.givenSearchPages(WikipediaDefinitions.EN, SEARCH, options).willReturn(pages);

    final List<SearchPage> result = apiSearch.searchPages(WikipediaDefinitions.EN, SEARCH, options);

    Assertions.assertThat(result).isEqualTo(pages);
  }

  @DisplayName("Search pages fails when the mock fails")
  @Test
  void searchPagesWhenFailing() {
    mockApiSearch.givenSearchPages(WikipediaDefinitions.EN, SEARCH, null).willFail();

    Assertions.assertThatThrownBy(
            () -> apiSearch.searchPages(WikipediaDefinitions.EN, SEARCH, null))
        .isInstanceOf(HttpServerErrorException.class);
  }
}
