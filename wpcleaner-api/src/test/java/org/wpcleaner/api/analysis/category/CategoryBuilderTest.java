package org.wpcleaner.api.analysis.category;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.wpcleaner.api.analysis.PageAnalysisTestFactory;
import org.wpcleaner.api.analysis.pagetitle.PageTitle;
import org.wpcleaner.api.repository.namespace.CommonNamespaces;

class CategoryBuilderTest {

  private static final String NAMESPACE = "Catégorie";
  private static final String PARIS = "Paris";

  @DisplayName("should build a category with or without its sort key")
  @Test
  void buildWithSortKey() {
    // GIVEN
    final CategoryBuilder builder = CategoryBuilder.to(NAMESPACE, PARIS);

    // WHEN
    final String withoutSortKey = builder.build();
    final String withSortKey = builder.withSortKey(" Ville ").build();
    final String withEmptySortKey = builder.withSortKey("").build();
    final String withNullSortKey = builder.withSortKey(" Ville ").withSortKey(null).build();

    // THEN
    Assertions.assertThat(withoutSortKey).isEqualTo("[[Catégorie:Paris]]");
    Assertions.assertThat(withSortKey).isEqualTo("[[Catégorie:Paris| Ville ]]");
    Assertions.assertThat(withEmptySortKey).isEqualTo("[[Catégorie:Paris|]]");
    Assertions.assertThat(withNullSortKey).isEqualTo(withoutSortKey);
    assertCategory(withoutSortKey, null);
    assertCategory(withSortKey, " Ville ");
    assertCategory(withEmptySortKey, "");
  }

  private static void assertCategory(final String wikitext, final String sortKey) {
    Assertions.assertThat(PageAnalysisTestFactory.createPageAnalysis(wikitext).getCategories())
        .singleElement()
        .satisfies(
            category -> {
              Assertions.assertThat(category.namespace()).isEqualTo(NAMESPACE);
              Assertions.assertThat(category.name()).isEqualTo(PARIS);
              Assertions.assertThat(category.title())
                  .isEqualTo(new PageTitle(CommonNamespaces.CATEGORY.id, PARIS));
              Assertions.assertThat(category.sortKey()).isEqualTo(sortKey);
            });
  }
}
