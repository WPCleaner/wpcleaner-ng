package org.wpcleaner.api.api.query.prop.links;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

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
import org.wpcleaner.api.wiki.definition.WikipediaDefinitions;

@SpringBootTest(classes = SpringBootTestConfig.class)
class ApiLinksTest {

  @Autowired private ApiLinks apiLinks;

  @DisplayName("Retrieve all links of a page, following the continuation")
  @Test
  void retrieveLinksWithContinuation() {
    final LinksQuery options = LinksQuery.emptyBuilder().limit(Limit.of(10)).build();

    final List<Page> pages =
        apiLinks.retrieveLinksByTitle(WikipediaDefinitions.EN, List.of("Main Page"), options);

    Assertions.assertThat(pages).as("pages").hasSize(1);
    final Page page = pages.getFirst();
    Assertions.assertThat(page.title()).as("title").isEqualTo("Main Page");
    Assertions.assertThat(page.links()).as("links").hasSizeGreaterThan(10).doesNotHaveDuplicates();
  }

  @DisplayName("Retrieve links filtered by namespace in descending order")
  @Test
  void retrieveLinksWithNamespaceAndDirection() {
    final LinksQuery options =
        LinksQuery.emptyBuilder()
            .direction(LinksParameters.Direction.DESCENDING)
            .limit(Limit.max())
            .namespace(Set.of(Namespace.MAIN))
            .build();

    final List<Page> pages =
        apiLinks.retrieveLinksByTitle(WikipediaDefinitions.EN, List.of("Paris"), options);

    Assertions.assertThat(pages).as("pages").hasSize(1);
    final List<Link> links = pages.getFirst().links();
    Assertions.assertThat(links).as("links").isNotEmpty().allMatch(link -> link.ns() == 0);
    Assertions.assertThat(links.getFirst().title().compareTo(links.getLast().title()))
        .as("descending")
        .isPositive();
  }

  @DisplayName("Retrieve links restricted to some targets")
  @Test
  void retrieveLinksWithTitles() {
    final LinksQuery options =
        LinksQuery.emptyBuilder().titles(List.of("France", "NonExistentTarget123456789")).build();

    final List<Page> pages =
        apiLinks.retrieveLinksByTitle(WikipediaDefinitions.EN, List.of("Paris"), options);

    Assertions.assertThat(pages).as("pages").hasSize(1);
    Assertions.assertThat(pages.getFirst().links())
        .as("links")
        .containsExactly(new Link(0, "France"));
  }

  @DisplayName("Retrieve links by page ID")
  @Test
  void retrieveLinksByPageId() {
    final LinksQuery options = LinksQuery.emptyBuilder().limit(Limit.of(5)).build();
    final List<Page> pagesByTitle =
        apiLinks.retrieveLinksByTitle(WikipediaDefinitions.EN, List.of("Main Page"), options);
    final Integer pageId = pagesByTitle.getFirst().pageId();
    Assertions.assertThat(pageId).isNotNull();

    final List<Page> pagesById =
        apiLinks.retrieveLinksByPageId(WikipediaDefinitions.EN, List.of(pageId), options);

    Assertions.assertThat(pagesById).as("pages").hasSize(1);
    Assertions.assertThat(pagesById.getFirst().title()).as("title").isEqualTo("Main Page");
    Assertions.assertThat(pagesById.getFirst().links())
        .as("links")
        .isEqualTo(pagesByTitle.getFirst().links());
  }

  @DisplayName("Retrieve links for a missing page")
  @Test
  void retrieveLinksMissingPage() {
    final List<Page> pages =
        apiLinks.retrieveLinksByTitle(
            WikipediaDefinitions.EN, List.of("NonExistentPageTitle123456789WPCleanerTest"), null);

    Assertions.assertThat(pages).as("pages").hasSize(1);
    Assertions.assertThat(pages.getFirst().missing()).as("missing").isTrue();
    Assertions.assertThat(pages.getFirst().links()).as("links").isEmpty();
  }
}
