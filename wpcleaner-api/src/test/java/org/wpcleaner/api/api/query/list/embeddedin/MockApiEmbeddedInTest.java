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
import org.springframework.web.client.HttpServerErrorException;
import org.wpcleaner.api.ApiSpringBootTest;
import org.wpcleaner.api.api.Limit;
import org.wpcleaner.api.repository.namespace.Namespace;
import org.wpcleaner.api.wiki.definition.WikipediaDefinitions;

@ApiSpringBootTest
class MockApiEmbeddedInTest {

  private static final String TEMPLATE = "Template:Infobox";

  @Autowired private ApiEmbeddedIn apiEmbeddedIn;
  @Autowired private MockApiEmbeddedIn mockApiEmbeddedIn;

  @DisplayName("Retrieve embedded in by title returns the pages given to the mock")
  @Test
  void retrieveEmbeddedInByTitle() {
    final EmbeddedInQuery options =
        EmbeddedInQuery.emptyBuilder()
            .direction(EmbeddedInParameters.Direction.DESCENDING)
            .filterRedirect(EmbeddedInParameters.FilterRedirect.NON_REDIRECTS)
            .limit(Limit.max())
            .namespace(Set.of(Namespace.MAIN))
            .build();
    final List<EmbeddingPage> pages =
        List.of(new EmbeddingPage(0, 22_939, "Physics"), new EmbeddingPage(0, null, "Optics"));
    mockApiEmbeddedIn
        .givenRetrieveEmbeddedInByTitle(WikipediaDefinitions.EN, TEMPLATE, options)
        .willReturn(pages);

    final List<EmbeddingPage> result =
        apiEmbeddedIn.retrieveEmbeddedInByTitle(WikipediaDefinitions.EN, TEMPLATE, options);

    Assertions.assertThat(result).isEqualTo(pages);
  }

  @DisplayName("Retrieve embedded in by page id returns the pages given to the mock")
  @Test
  void retrieveEmbeddedInByPageId() {
    final EmbeddedInQuery options = EmbeddedInQuery.emptyBuilder().eiContinue("0|42").build();
    final List<EmbeddingPage> pages = List.of(new EmbeddingPage(10, 1_234, "Template:Other"));
    mockApiEmbeddedIn
        .givenRetrieveEmbeddedInByPageId(WikipediaDefinitions.EN, 691_810, options)
        .willReturn(pages);

    final List<EmbeddingPage> result =
        apiEmbeddedIn.retrieveEmbeddedInByPageId(WikipediaDefinitions.EN, 691_810, options);

    Assertions.assertThat(result).isEqualTo(pages);
  }

  @DisplayName("Retrieve embedded in fails when the mock fails")
  @Test
  void retrieveEmbeddedInWhenFailing() {
    mockApiEmbeddedIn
        .givenRetrieveEmbeddedInByTitle(WikipediaDefinitions.EN, TEMPLATE, null)
        .willFail();

    Assertions.assertThatThrownBy(
            () -> apiEmbeddedIn.retrieveEmbeddedInByTitle(WikipediaDefinitions.EN, TEMPLATE, null))
        .isInstanceOf(HttpServerErrorException.class);
  }
}
