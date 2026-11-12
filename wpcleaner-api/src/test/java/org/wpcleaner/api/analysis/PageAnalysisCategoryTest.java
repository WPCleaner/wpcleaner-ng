package org.wpcleaner.api.analysis;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.wpcleaner.api.analysis.category.CategoryElement;
import org.wpcleaner.api.analysis.pagetitle.PageTitle;
import org.wpcleaner.api.wiki.definition.WikipediaDefinitions;

class PageAnalysisCategoryTest {

  @DisplayName("should not analyze category with namespaces of another wiki")
  @Test
  void analyzeCategoryWithNamespacesOfAnotherWiki() {
    // GIVEN
    final String text = "[[Catégorie:My category]]";

    // WHEN
    final PageAnalysis analysis =
        PageAnalysisTestFactory.createPageAnalysis(WikipediaDefinitions.EN, text);

    // THEN
    Assertions.assertThat(analysis.getCategories()).isEmpty();
    Assertions.assertThat(analysis.getInternalLinks()).hasSize(1);
  }

  @DisplayName("should analyze category")
  @Test
  void analyzeCategory() {
    // GIVEN
    final String text = "[[Catégorie:My category]]";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getInternalLinks()).isEmpty();
    Assertions.assertThat(analysis.getCategories()).hasSize(1);
    Assertions.assertThat(analysis.getCategories().getFirst().begin()).isZero();
    Assertions.assertThat(analysis.getCategories().getFirst().end()).isEqualTo(text.length());
  }

  @DisplayName("should analyze category parts")
  @Test
  void analyzeCategoryParts() {
    // GIVEN
    final String text = "[[Catégorie: My category |Sort key]]";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getCategories())
        .containsExactly(
            new CategoryElement(
                0,
                text.length(),
                "Catégorie",
                "My category",
                new PageTitle(14, "My category"),
                "Sort key"));
  }

  @DisplayName("should analyze category when namespace is surrounded by whitespace")
  @Test
  void analyzeCategoryWithWhitespaceAroundNamespace() {
    // GIVEN
    final String text = "[[ Catégorie :My category]]";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getInternalLinks()).isEmpty();
    Assertions.assertThat(analysis.getCategories())
        .containsExactly(
            new CategoryElement(
                0,
                text.length(),
                "Catégorie",
                "My category",
                new PageTitle(14, "My category"),
                null));
  }
}
