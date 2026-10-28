package org.wpcleaner.api.api.query.prop.langlinks;

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
import org.wpcleaner.api.wiki.definition.WikipediaDefinitions;

@ApiSpringBootTest
class MockApiLanglinksTest {

  private static final List<String> TITLES = List.of("Main Page", "Café");

  @Autowired private ApiLanglinks apiLanglinks;
  @Autowired private MockApiLanglinks mockApiLanglinks;

  @DisplayName("Retrieve langlinks by title returns the pages given to the mock")
  @Test
  void retrieveLanglinksByTitle() {
    final LanglinksQuery options =
        LanglinksQuery.emptyBuilder()
            .direction(LanglinksParameters.Direction.DESCENDING)
            .inLanguageCode("en")
            .language("fr")
            .limit(Limit.max())
            .llContinue("1|fr")
            .properties(
                Set.of(
                    LanglinksParameters.Properties.URL,
                    LanglinksParameters.Properties.AUTONYM,
                    LanglinksParameters.Properties.LANGUAGE_NAME))
            .title("Café")
            .build();
    final List<Page> pages =
        List.of(
            new Page(
                List.of(
                    new Langlink(
                        "français",
                        "fr",
                        "French",
                        "Accueil",
                        "https://fr.wikipedia.org/wiki/Accueil")),
                false,
                0,
                15_580_374,
                "Main Page"),
            new Page(List.of(), true, 0, null, "Café"));
    mockApiLanglinks
        .givenRetrieveLanglinksByTitle(WikipediaDefinitions.EN, TITLES, options)
        .willReturn(pages);

    final List<Page> result =
        apiLanglinks.retrieveLanglinksByTitle(WikipediaDefinitions.EN, TITLES, options);

    Assertions.assertThat(result).isEqualTo(pages);
  }

  @DisplayName("Retrieve langlinks by title fails when the mock fails")
  @Test
  void retrieveLanglinksByTitleWhenFailing() {
    mockApiLanglinks
        .givenRetrieveLanglinksByTitle(WikipediaDefinitions.EN, TITLES, null)
        .willFail();

    Assertions.assertThatThrownBy(
            () -> apiLanglinks.retrieveLanglinksByTitle(WikipediaDefinitions.EN, TITLES, null))
        .isInstanceOf(HttpServerErrorException.class);
  }
}
