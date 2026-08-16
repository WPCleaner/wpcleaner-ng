package org.wpcleaner.api.analysis;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.wpcleaner.api.repository.CaseType;
import org.wpcleaner.api.repository.namespace.CommonNamespaces;
import org.wpcleaner.api.repository.namespace.Namespace;
import org.wpcleaner.api.repository.namespace.NamespaceRepository;

class PageAnalysisTest {

  @DisplayName("should analyze comment")
  @Test
  void analyzeComment() {
    // GIVEN
    final String text = "<!-- COMMENT -->";

    // WHEN
    final PageAnalysis analysis = createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getComments()).hasSize(1);
    Assertions.assertThat(analysis.getComments().getFirst().begin()).isZero();
    Assertions.assertThat(analysis.getComments().getFirst().end()).isEqualTo(text.length());
  }

  @DisplayName("should analyze internal link")
  @Test
  void analyzeInternalLink() {
    // GIVEN
    final String text = "[[Link]]";

    // WHEN
    final PageAnalysis analysis = createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getInternalLinks()).hasSize(1);
    Assertions.assertThat(analysis.getInternalLinks().getFirst().begin()).isZero();
    Assertions.assertThat(analysis.getInternalLinks().getFirst().end()).isEqualTo(text.length());
  }

  @DisplayName("should analyze category")
  @Test
  void analyzeCategory() {
    // GIVEN
    final String text = "[[Catégorie:My category]]";

    // WHEN
    final PageAnalysis analysis = createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getInternalLinks()).isEmpty();
    Assertions.assertThat(analysis.getCategories()).hasSize(1);
    Assertions.assertThat(analysis.getCategories().getFirst().begin()).isZero();
    Assertions.assertThat(analysis.getCategories().getFirst().end()).isEqualTo(text.length());
  }

  private PageAnalysis createPageAnalysis(final String text) {
    final NamespaceRepository namespaceRepository = new NamespaceRepository();
    namespaceRepository.addNamespace(
        new Namespace(
            CommonNamespaces.CATEGORY.id,
            "Category",
            "Catégorie",
            List.of("Catégories"),
            CaseType.FIRST_LETTER));
    return new PageAnalysis("Title", text, namespaceRepository);
  }
}
