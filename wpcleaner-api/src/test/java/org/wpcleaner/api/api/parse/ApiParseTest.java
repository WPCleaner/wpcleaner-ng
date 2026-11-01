package org.wpcleaner.api.api.parse;

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
import org.wpcleaner.api.ApiSpringBootTest;
import org.wpcleaner.api.api.ApiError;
import org.wpcleaner.api.api.ApiException;
import org.wpcleaner.api.wiki.definition.WikipediaDefinitions;

@ApiSpringBootTest
class ApiParseTest {

  private static final ParseQuery QUERY =
      new ParseQuery(
          "Sandbox",
          "[[Paris]] [[Category:Cities]]",
          Set.of(ParseParameters.Properties.LINKS, ParseParameters.Properties.CATEGORIES));

  @Autowired private ApiParse apiParse;
  @Autowired private MockApiParse mockApiParse;

  @DisplayName("ApiParse sends the text and properties and returns the parse result")
  @Test
  void parse() {
    // GIVEN
    final Parse expectedParse =
        new Parse(
            "Sandbox",
            List.of(new Category("Cities", "", false, true)),
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of(new Link(0, "Paris", true)),
            null,
            List.of(),
            null);
    mockApiParse.givenParse(WikipediaDefinitions.EN, QUERY).willReturn(expectedParse);

    // WHEN
    final Parse parse = apiParse.parse(WikipediaDefinitions.EN, QUERY);

    // THEN
    Assertions.assertThat(parse).isEqualTo(expectedParse);
  }

  @DisplayName("ApiParse throws ApiException when MediaWiki returns errors")
  @Test
  void parseWithErrors() {
    // GIVEN
    mockApiParse
        .givenParse(WikipediaDefinitions.EN, QUERY)
        .willReturnErrors(List.of(new ApiError("invalidtitle", "parse", "Bad title.")));

    // WHEN / THEN
    Assertions.assertThatThrownBy(() -> apiParse.parse(WikipediaDefinitions.EN, QUERY))
        .isInstanceOf(ApiException.class);
  }
}
