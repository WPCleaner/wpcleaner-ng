package org.wpcleaner.api.api.query.list.random;

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
import org.wpcleaner.api.repository.CaseType;
import org.wpcleaner.api.repository.namespace.CommonNamespaces;
import org.wpcleaner.api.repository.namespace.Namespace;
import org.wpcleaner.api.wiki.definition.WikipediaDefinitions;

@ApiSpringBootTest
class MockApiRandomTest {

  @Autowired private ApiRandom apiRandom;
  @Autowired private MockApiRandom mockApiRandom;

  @DisplayName("Retrieve random pages returns the pages given to the mock")
  @Test
  void retrieveRandomPages() {
    final RandomQuery options =
        RandomQuery.emptyBuilder()
            .contentModel(RandomParameters.ContentModel.WIKITEXT)
            .filterRedirect(RandomParameters.FilterRedirect.NON_REDIRECTS)
            .limit(Limit.of(2))
            .maxSize(1000)
            .minSize(10)
            .namespace(
                Set.of(
                    Namespace.MAIN,
                    new Namespace(
                        CommonNamespaces.USER.id,
                        "User",
                        "User",
                        List.of(),
                        CaseType.FIRST_LETTER)))
            .rnContinue("0.5|0.6|0|0")
            .build();
    final List<RandomPage> pages =
        List.of(new RandomPage(1234, 0, "Café"), new RandomPage(5678, 2, "User:Jane Doe"));
    mockApiRandom.givenRetrieveRandomPages(WikipediaDefinitions.EN, options).willReturn(pages);

    final List<RandomPage> result = apiRandom.retrieveRandomPages(WikipediaDefinitions.EN, options);

    Assertions.assertThat(result).isEqualTo(pages);
  }

  @DisplayName("Retrieve random pages fails when the mock fails")
  @Test
  void retrieveRandomPagesWhenFailing() {
    mockApiRandom.givenRetrieveRandomPages(WikipediaDefinitions.EN, null).willFail();

    Assertions.assertThatThrownBy(
            () -> apiRandom.retrieveRandomPages(WikipediaDefinitions.EN, null))
        .isInstanceOf(HttpServerErrorException.class);
  }
}
