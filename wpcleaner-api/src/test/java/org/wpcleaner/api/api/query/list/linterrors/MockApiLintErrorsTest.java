package org.wpcleaner.api.api.query.list.linterrors;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.client.MockRestServiceServer;
import org.wpcleaner.api.ApiSpringBootTest;
import org.wpcleaner.api.api.ApiError;
import org.wpcleaner.api.api.ApiException;
import org.wpcleaner.api.api.Limit;
import org.wpcleaner.api.repository.namespace.Namespace;
import org.wpcleaner.api.rest.lint.LintError;
import org.wpcleaner.api.wiki.definition.WikipediaDefinitions;

@ApiSpringBootTest
class MockApiLintErrorsTest {

  @Autowired private ApiLintErrors apiLintErrors;
  @Autowired private MockApiLintErrors mockApiLintErrors;
  @Autowired private MockRestServiceServer mediaWikiServer;

  @AfterEach
  void tearDown() {
    mediaWikiServer.reset();
  }

  @DisplayName("Retrieve lint errors with every parameter returns the errors given to the mock")
  @Test
  void retrieveLintErrors() {
    // GIVEN
    final LintErrorsQuery query =
        LintErrorsQuery.emptyBuilder()
            .categories(Set.of("obsolete-tag", "missing-end-tag"))
            .from(5_000_000L)
            .limit(Limit.max())
            .namespace(Set.of(Namespace.MAIN))
            .pageId(Set.of(892_374L))
            .title("Somme de Minkowski")
            .build();
    final List<PageLintError> errors =
        List.of(
            new PageLintError(
                "obsolete-tag",
                5_000_288L,
                List.of(306, 369),
                0,
                892_374L,
                Map.of("name", "center"),
                new LintError.TemplateInfo(true, null),
                "Somme de Minkowski"));
    mockApiLintErrors.givenRetrieveLintErrors(WikipediaDefinitions.FR, query).willReturn(errors);

    // WHEN
    final List<PageLintError> result =
        apiLintErrors.retrieveLintErrors(WikipediaDefinitions.FR, query);

    // THEN
    mediaWikiServer.verify();
    Assertions.assertThat(result).isEqualTo(errors);
  }

  @DisplayName("Retrieve lint errors fails when the API returns errors")
  @Test
  void retrieveLintErrorsWithErrors() {
    // GIVEN
    final LintErrorsQuery query =
        LintErrorsQuery.emptyBuilder().categories(Set.of("unknown")).build();
    mockApiLintErrors
        .givenRetrieveLintErrors(WikipediaDefinitions.FR, query)
        .willReturnErrors(List.of(new ApiError("badvalue", "main", "Bad value.")));

    // WHEN, THEN
    Assertions.assertThatThrownBy(
            () -> apiLintErrors.retrieveLintErrors(WikipediaDefinitions.FR, query))
        .isInstanceOf(ApiException.class);
  }
}
