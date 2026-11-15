package org.wpcleaner.api.api.query.list.linterrors;

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
class ApiLintErrorsTest {

  @Autowired private ApiLintErrors apiLintErrors;

  @DisplayName("Retrieve lint errors of a category in the main namespace")
  @Test
  void retrieveLintErrors() {
    final List<PageLintError> errors =
        apiLintErrors.retrieveLintErrors(
            WikipediaDefinitions.FR,
            LintErrorsQuery.emptyBuilder()
                .categories(Set.of("obsolete-tag"))
                .namespace(Set.of(Namespace.MAIN))
                .limit(Limit.of(5))
                .build());

    Assertions.assertThat(errors)
        .isNotEmpty()
        .hasSizeLessThanOrEqualTo(5)
        .allSatisfy(
            error -> {
              Assertions.assertThat(error.category()).isEqualTo("obsolete-tag");
              Assertions.assertThat(error.namespace()).isZero();
              Assertions.assertThat(error.title()).isNotBlank();
              Assertions.assertThat(error.pageId()).isPositive();
              Assertions.assertThat(error.location()).hasSize(2);
            });
  }

  @DisplayName("Retrieve lint errors of a page")
  @Test
  void retrieveLintErrorsOfPage() {
    final PageLintError first =
        apiLintErrors
            .retrieveLintErrors(
                WikipediaDefinitions.FR,
                LintErrorsQuery.emptyBuilder()
                    .categories(Set.of("obsolete-tag"))
                    .limit(Limit.of(1))
                    .build())
            .getFirst();

    final List<PageLintError> errors =
        apiLintErrors.retrieveLintErrors(
            WikipediaDefinitions.FR,
            LintErrorsQuery.emptyBuilder()
                .categories(Set.of("obsolete-tag"))
                .pageId(Set.of(first.pageId()))
                .build());

    Assertions.assertThat(errors)
        .isNotEmpty()
        .allSatisfy(error -> Assertions.assertThat(error.pageId()).isEqualTo(first.pageId()));
  }
}
