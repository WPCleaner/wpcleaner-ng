package org.wpcleaner.api.api.query.prop.linkshere;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Objects;
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
class ApiLinksHereTest {

  private static final String DISAMBIGUATION = "Albert Einstein (disambiguation)";

  @Autowired private ApiLinksHere apiLinksHere;

  @DisplayName("Retrieve all links to a page, following the continuation")
  @Test
  void retrieveLinksHereWithContinuation() {
    final LinksHereQuery options = LinksHereQuery.emptyBuilder().limit(Limit.of(10)).build();

    final List<Page> pages =
        apiLinksHere.retrieveLinksHereByTitle(
            WikipediaDefinitions.EN, List.of(DISAMBIGUATION), options);

    Assertions.assertThat(pages).as("pages").hasSize(1);
    final Page page = pages.getFirst();
    Assertions.assertThat(page.title()).as("title").isEqualTo(DISAMBIGUATION);
    Assertions.assertThat(page.linksHere())
        .as("links here")
        .hasSizeGreaterThan(10)
        .doesNotHaveDuplicates()
        .allMatch(linkHere -> linkHere.pageId() != null && linkHere.title() != null);
  }

  @DisplayName("Retrieve links to a page filtered by namespace and without redirects")
  @Test
  void retrieveLinksHereWithNamespaceAndShow() {
    final LinksHereQuery options =
        LinksHereQuery.emptyBuilder()
            .limit(Limit.max())
            .namespace(Set.of(Namespace.MAIN))
            .show(Set.of(LinksHereParameters.Show.NOT_REDIRECT))
            .build();

    final List<Page> pages =
        apiLinksHere.retrieveLinksHereByTitle(
            WikipediaDefinitions.EN, List.of(DISAMBIGUATION), options);

    Assertions.assertThat(pages).as("pages").hasSize(1);
    Assertions.assertThat(pages.getFirst().linksHere())
        .as("links here")
        .isNotEmpty()
        .allMatch(linkHere -> linkHere.ns() == 0)
        .allMatch(linkHere -> Boolean.FALSE.equals(linkHere.redirect()));
  }

  @DisplayName("Retrieve links to a page with only some properties")
  @Test
  void retrieveLinksHereWithProperties() {
    final LinksHereQuery options =
        LinksHereQuery.emptyBuilder()
            .limit(Limit.of(5))
            .properties(Set.of(LinksHereParameters.Properties.TITLE))
            .build();

    final List<Page> pages =
        apiLinksHere.retrieveLinksHereByTitle(
            WikipediaDefinitions.EN, List.of(DISAMBIGUATION), options);

    Assertions.assertThat(pages.getFirst().linksHere())
        .as("links here")
        .isNotEmpty()
        .allMatch(linkHere -> linkHere.pageId() == null && linkHere.redirect() == null)
        .allMatch(linkHere -> Objects.nonNull(linkHere.title()));
  }

  @DisplayName("Retrieve links to a page by page ID")
  @Test
  void retrieveLinksHereByPageId() {
    final LinksHereQuery options = LinksHereQuery.emptyBuilder().limit(Limit.max()).build();
    final List<Page> pagesByTitle =
        apiLinksHere.retrieveLinksHereByTitle(
            WikipediaDefinitions.EN, List.of(DISAMBIGUATION), options);
    final Integer pageId = pagesByTitle.getFirst().pageId();
    Assertions.assertThat(pageId).isNotNull();

    final List<Page> pagesById =
        apiLinksHere.retrieveLinksHereByPageId(WikipediaDefinitions.EN, List.of(pageId), options);

    Assertions.assertThat(pagesById).as("pages").hasSize(1);
    Assertions.assertThat(pagesById.getFirst().title()).as("title").isEqualTo(DISAMBIGUATION);
    Assertions.assertThat(pagesById.getFirst().linksHere())
        .as("links here")
        .isEqualTo(pagesByTitle.getFirst().linksHere());
  }

  @DisplayName("Retrieve links to a missing page")
  @Test
  void retrieveLinksHereMissingPage() {
    final List<Page> pages =
        apiLinksHere.retrieveLinksHereByTitle(
            WikipediaDefinitions.EN, List.of("NonExistentPageTitle123456789WPCleanerTest"), null);

    Assertions.assertThat(pages).as("pages").hasSize(1);
    Assertions.assertThat(pages.getFirst().missing()).as("missing").isTrue();
    Assertions.assertThat(pages.getFirst().linksHere()).as("links here").isEmpty();
  }
}
