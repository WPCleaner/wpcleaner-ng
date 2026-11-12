package org.wpcleaner.api.analysis;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.wpcleaner.api.analysis.externallink.ExternalLinkElement;
import org.wpcleaner.api.analysis.internallink.InternalLinkElement;
import org.wpcleaner.api.analysis.pagetitle.PageTitle;

class PageAnalysisInternalLinkTest {

  @DisplayName("should analyze internal link")
  @Test
  void analyzeInternalLink() {
    // GIVEN
    final String text = "[[Link]]";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getInternalLinks()).hasSize(1);
    Assertions.assertThat(analysis.getInternalLinks().getFirst().begin()).isZero();
    Assertions.assertThat(analysis.getInternalLinks().getFirst().end()).isEqualTo(text.length());
  }

  @DisplayName("should analyze internal link parts")
  @Test
  void analyzeInternalLinkParts() {
    // GIVEN
    final String text = "[[ Link # Anchor |Some text]]";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getInternalLinks())
        .containsExactly(
            new InternalLinkElement(
                0, text.length(), "Link", new PageTitle(0, "Link"), "Anchor", "Some text", false));
  }

  @DisplayName("should analyze internal link without text nor anchor")
  @Test
  void analyzeInternalLinkWithoutTextNorAnchor() {
    // GIVEN
    final String text = "[[Link]]";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getInternalLinks())
        .containsExactly(
            new InternalLinkElement(
                0, text.length(), "Link", new PageTitle(0, "Link"), null, null, false));
  }

  @DisplayName("should analyze internal link with empty text")
  @Test
  void analyzeInternalLinkWithEmptyText() {
    // GIVEN
    final String text = "[[Link|]]";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getInternalLinks())
        .containsExactly(
            new InternalLinkElement(
                0, text.length(), "Link", new PageTitle(0, "Link"), null, "", false));
  }

  @DisplayName("should analyze internal link to an anchor of the same page")
  @Test
  void analyzeInternalLinkToAnchor() {
    // GIVEN
    final String text = "[[#Section]]";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getInternalLinks())
        .containsExactly(
            new InternalLinkElement(0, text.length(), "", null, "Section", null, false));
  }

  @DisplayName("should analyze internal link followed by a closing bracket")
  @Test
  void analyzeInternalLinkFollowedByClosingBracket() {
    // GIVEN
    final String text = "[[a|b]]]";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getInternalLinks())
        .containsExactly(
            new InternalLinkElement(0, 7, "a", new PageTitle(0, "A"), null, "b", false));
  }

  @DisplayName("should analyze internal link after an even number of opening brackets")
  @Test
  void analyzeInternalLinkAfterManyBrackets() {
    // GIVEN
    final String text =
        "[[[Lyon]]] [[[[Paris]]]] [[[[[Nantes]]]]] [[[[[[Rennes]]]]]] [[[<!-- c -->[Lille|L]]";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getInternalLinks())
        .extracting(InternalLinkElement::begin, InternalLinkElement::end, InternalLinkElement::text)
        .containsExactly(
            Assertions.tuple(
                text.indexOf("[[Paris"), text.indexOf("[[Paris") + "[[Paris]]".length(), null),
            Assertions.tuple(
                text.indexOf("[[Rennes"), text.indexOf("[[Rennes") + "[[Rennes]]".length(), null),
            Assertions.tuple(text.indexOf("[<!-- c"), text.length(), "L"));
  }

  @DisplayName("should not analyze internal link with spaces before a URL")
  @Test
  void analyzeSpacesBeforeUrlInsideDoubleBrackets() {
    // GIVEN
    final String text = "[[ https://example.com/ b]]";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getInternalLinks()).isEmpty();
    Assertions.assertThat(analysis.getExternalLinks())
        .containsExactly(new ExternalLinkElement(3, 23, "https://example.com/", null, false));
  }
}
