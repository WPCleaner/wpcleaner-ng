package org.wpcleaner.api.analysis;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.wpcleaner.api.analysis.internallink.InternalLinkElement;
import org.wpcleaner.api.analysis.interwikilink.InterwikiLinkElement;

class PageAnalysisLeadingColonTest {

  @DisplayName("should analyze language prefix with leading colon as inline interwiki link")
  @Test
  void analyzeLanguageLinkWithLeadingColon() {
    // GIVEN
    final String text = "[[:en:Hello#Top|hi]] [[ :en:World]]";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getLanguageLinks()).isEmpty();
    Assertions.assertThat(analysis.getInternalLinks()).isEmpty();
    Assertions.assertThat(analysis.getInterwikiLinks())
        .containsExactly(
            new InterwikiLinkElement(0, 20, "en", "Hello", "Top", "hi", true),
            new InterwikiLinkElement(21, text.length(), "en", "World", null, null, true));
  }

  @DisplayName("should analyze category with leading colon as internal link to the category")
  @Test
  void analyzeCategoryWithLeadingColon() {
    // GIVEN
    final String text = "[[:Catégorie:Paris|Paris]]";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getCategories()).isEmpty();
    Assertions.assertThat(analysis.getInternalLinks())
        .containsExactly(
            new InternalLinkElement(0, text.length(), "Catégorie:Paris", null, "Paris", true));
  }

  @DisplayName("should analyze interwiki and internal links with leading colon")
  @Test
  void analyzeLinksWithLeadingColon() {
    // GIVEN
    final String text = "[[:wikt:mot]] [[wikt:mot]] [[:Page]]";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getInterwikiLinks())
        .containsExactlyInAnyOrder(
            new InterwikiLinkElement(0, 13, "wikt", "mot", null, null, true),
            new InterwikiLinkElement(14, 26, "wikt", "mot", null, null, false));
    Assertions.assertThat(analysis.getInternalLinks())
        .containsExactly(new InternalLinkElement(27, text.length(), "Page", null, null, true));
  }
}
