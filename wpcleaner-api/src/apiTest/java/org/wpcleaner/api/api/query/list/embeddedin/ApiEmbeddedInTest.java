package org.wpcleaner.api.api.query.list.embeddedin;

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
import org.wpcleaner.api.wiki.definition.WikimediaDefinitions;

@SpringBootTest(classes = SpringBootTestConfig.class)
class ApiEmbeddedInTest {

  private static final String TEMPLATE = "Template:Wikimania";
  private static final int TEMPLATE_PAGE_ID = 56_115;

  @Autowired private ApiEmbeddedIn apiEmbeddedIn;

  @DisplayName("Retrieve embedded in by title with default options")
  @Test
  void retrieveEmbeddedInByTitleDefault() {
    final List<EmbeddingPage> pages =
        apiEmbeddedIn.retrieveEmbeddedInByTitle(WikimediaDefinitions.META, TEMPLATE, null);

    Assertions.assertThat(pages).as("pages").isNotEmpty();
    for (final EmbeddingPage page : pages) {
      Assertions.assertThat(page.title()).as("title").isNotBlank();
      Assertions.assertThat(page.pageId()).as("pageId").isNotNull();
    }
  }

  @DisplayName("Retrieve embedded in by page id")
  @Test
  void retrieveEmbeddedInByPageId() {
    final List<EmbeddingPage> byPageId =
        apiEmbeddedIn.retrieveEmbeddedInByPageId(WikimediaDefinitions.META, TEMPLATE_PAGE_ID, null);
    final List<EmbeddingPage> byTitle =
        apiEmbeddedIn.retrieveEmbeddedInByTitle(WikimediaDefinitions.META, TEMPLATE, null);

    Assertions.assertThat(byPageId).as("pages").isNotEmpty().isEqualTo(byTitle);
  }

  @DisplayName("Retrieve embedded in following continuation")
  @Test
  void retrieveEmbeddedInWithContinuation() {
    final EmbeddedInQuery options = EmbeddedInQuery.emptyBuilder().limit(Limit.of(2)).build();

    final List<EmbeddingPage> pages =
        apiEmbeddedIn.retrieveEmbeddedInByTitle(WikimediaDefinitions.META, TEMPLATE, options);

    Assertions.assertThat(pages).as("pages").hasSizeGreaterThan(2);
    Assertions.assertThat(pages)
        .extracting(EmbeddingPage::title)
        .as("titles")
        .doesNotHaveDuplicates();
  }

  @DisplayName("Retrieve embedded in with every option")
  @Test
  void retrieveEmbeddedInWithOptions() {
    final EmbeddedInQuery options =
        EmbeddedInQuery.emptyBuilder()
            .direction(EmbeddedInParameters.Direction.DESCENDING)
            .filterRedirect(EmbeddedInParameters.FilterRedirect.NON_REDIRECTS)
            .limit(Limit.max())
            .namespace(Set.of(Namespace.MAIN))
            .build();

    final List<EmbeddingPage> pages =
        apiEmbeddedIn.retrieveEmbeddedInByTitle(WikimediaDefinitions.META, TEMPLATE, options);

    Assertions.assertThat(pages).as("pages").isNotEmpty();
    for (final EmbeddingPage page : pages) {
      Assertions.assertThat(page.ns()).as("ns").isZero();
    }
  }
}
