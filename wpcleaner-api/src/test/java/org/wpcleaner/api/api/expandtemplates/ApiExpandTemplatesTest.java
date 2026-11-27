package org.wpcleaner.api.api.expandtemplates;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.wpcleaner.api.ApiSpringBootTest;
import org.wpcleaner.api.api.ApiError;
import org.wpcleaner.api.api.ApiException;
import org.wpcleaner.api.api.parse.Category;
import org.wpcleaner.api.wiki.definition.WikipediaDefinitions;

@ApiSpringBootTest
class ApiExpandTemplatesTest {

  private static final ExpandTemplatesQuery QUERY =
      ExpandTemplatesQuery.emptyBuilder()
          .includeComments(true)
          .properties(
              Set.of(
                  ExpandTemplatesParameters.Properties.WIKITEXT,
                  ExpandTemplatesParameters.Properties.CATEGORIES,
                  ExpandTemplatesParameters.Properties.VOLATILE))
          .revisionId(42)
          .showStrategyKeys(true)
          .text("<!-- c -->{{PAGENAME}} {{#invoke:Missing|main}}")
          .title("Paris")
          .build();

  @Autowired private ApiExpandTemplates apiExpandTemplates;
  @Autowired private MockApiExpandTemplates mockApiExpandTemplates;

  @DisplayName("ApiExpandTemplates sends the text and parameters and returns the expanded text")
  @Test
  void expandTemplates() {
    // GIVEN
    final ExpandTemplates expectedExpandTemplates =
        new ExpandTemplates(
            List.of(new Category("Articles_with_script_errors", "", null, null)),
            null,
            Map.of(),
            List.of(),
            List.of(),
            List.of(),
            null,
            Map.of(),
            null,
            false,
            "<!-- c -->Paris Script error");
    mockApiExpandTemplates
        .givenExpandTemplates(WikipediaDefinitions.EN, QUERY)
        .willReturn(expectedExpandTemplates);

    // WHEN
    final ExpandTemplates expandTemplates =
        apiExpandTemplates.expandTemplates(WikipediaDefinitions.EN, QUERY);

    // THEN
    Assertions.assertThat(expandTemplates).isEqualTo(expectedExpandTemplates);
  }

  @DisplayName("ApiExpandTemplates throws ApiException when MediaWiki returns errors")
  @Test
  void expandTemplatesWithErrors() {
    // GIVEN
    mockApiExpandTemplates
        .givenExpandTemplates(WikipediaDefinitions.EN, QUERY)
        .willReturnErrors(List.of(new ApiError("nosuchrevid", "expandtemplates", "No revision.")));

    // WHEN / THEN
    Assertions.assertThatThrownBy(
            () -> apiExpandTemplates.expandTemplates(WikipediaDefinitions.EN, QUERY))
        .isInstanceOf(ApiException.class);
  }
}
