package org.wpcleaner.api.api.query.list.search;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.wpcleaner.api.SpringBootTestConfig;
import org.wpcleaner.api.api.Limit;
import org.wpcleaner.api.repository.namespace.Namespace;
import org.wpcleaner.api.wiki.definition.WikimediaDefinitions;

@SpringBootTest(classes = SpringBootTestConfig.class)
class ApiSearchTest {

  @Autowired private ApiSearch apiSearch;

  @DisplayName("Search pages with default options")
  @Test
  void searchPagesDefault() {
    final List<SearchPage> pages =
        apiSearch.searchPages(
            WikimediaDefinitions.META, "intitle:\"Wikimedia Hackathon 2019\"", null);

    Assertions.assertThat(pages).as("pages").isNotEmpty();
    for (final SearchPage page : pages) {
      Assertions.assertThat(page.title())
          .as("title")
          .containsIgnoringCase("Wikimedia Hackathon 2019");
      Assertions.assertThat(page.pageId()).as("pageId").isNotZero();
    }
  }

  @DisplayName("Search pages following continuation")
  @Test
  void searchPagesWithContinuation() {
    final SearchQuery options =
        SearchQuery.emptyBuilder().limit(Limit.of(50)).namespace(Set.of(Namespace.MAIN)).build();

    final List<SearchPage> pages =
        apiSearch.searchPages(WikimediaDefinitions.META, "intitle:\"Wikimania 2017\"", options);

    Assertions.assertThat(pages).as("pages").hasSizeGreaterThan(50);
    Assertions.assertThat(pages).extracting(SearchPage::title).as("titles").doesNotHaveDuplicates();
    for (final SearchPage page : pages) {
      Assertions.assertThat(page.ns()).as("ns").isZero();
    }
  }

  @DisplayName("Search pages with offset and every property")
  @Test
  void searchPagesWithOffsetAndProperties() {
    final SearchQuery options =
        SearchQuery.emptyBuilder()
            .info(EnumSet.allOf(SearchParameters.Info.class))
            .limit(Limit.of(3))
            .offset(0)
            .properties(EnumSet.allOf(SearchParameters.Properties.class))
            .what(SearchParameters.What.TEXT)
            .build();

    final List<SearchPage> pages =
        apiSearch.searchPages(WikimediaDefinitions.META, "Wikimedia", options);

    Assertions.assertThat(pages).as("pages").hasSize(3);
    for (final SearchPage page : pages) {
      Assertions.assertThat(page.size()).as("size").isNotNull();
      Assertions.assertThat(page.wordCount()).as("wordCount").isNotNull();
      Assertions.assertThat(page.timestamp()).as("timestamp").isNotNull();
      Assertions.assertThat(page.snippet()).as("snippet").isNotNull();
    }
  }
}
