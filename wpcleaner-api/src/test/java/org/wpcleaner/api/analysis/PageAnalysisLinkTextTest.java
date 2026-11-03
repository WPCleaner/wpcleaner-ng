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
import org.wpcleaner.api.analysis.interwikilink.InterwikiLinkElement;

class PageAnalysisLinkTextTest {

  @DisplayName("should analyze link text containing single brackets and new lines")
  @Test
  void analyzeLinkTextWithBrackets() {
    // GIVEN
    final String text = "[[Paris|a [b] c]] [[:en:X|d [e] f]] [[Lyon|g\nh]]";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getInternalLinks())
        .extracting(InternalLinkElement::begin, InternalLinkElement::end, InternalLinkElement::text)
        .containsExactly(Assertions.tuple(0, 17, "a [b] c"), Assertions.tuple(36, 48, "g\nh"));
    Assertions.assertThat(analysis.getInterwikiLinks())
        .containsExactly(new InterwikiLinkElement(18, 35, "en", "X", null, "d [e] f", true));
  }

  @DisplayName("should not analyze link containing another link in its text")
  @Test
  void analyzeLinkTextWithNestedLink() {
    // GIVEN
    final String text = "[[a|b [[c]] d]]";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getInternalLinks())
        .extracting(InternalLinkElement::begin, InternalLinkElement::target)
        .containsExactly(Assertions.tuple(6, "c"));
  }

  @DisplayName("should keep the closing bracket of an external link inside link text")
  @Test
  void analyzeLinkTextWithExternalLink() {
    // GIVEN
    final String text = "[[a|b [http://x.org c]]]";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getInternalLinks())
        .extracting(InternalLinkElement::begin, InternalLinkElement::end, InternalLinkElement::text)
        .containsExactly(Assertions.tuple(0, text.length(), "b [http://x.org c]"));
    Assertions.assertThat(analysis.getExternalLinks())
        .containsExactly(new ExternalLinkElement(6, 22, "http://x.org", "c", true));
  }

  @DisplayName("should analyze external link text containing a bracket and an internal link")
  @Test
  void analyzeExternalLinkTextWithBracketAndInternalLink() {
    // GIVEN
    final String text = "[http://x.org a [b [[c|d]] e]";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getExternalLinks())
        .containsExactly(
            new ExternalLinkElement(0, text.length(), "http://x.org", "a [b [[c|d]] e", true));
    Assertions.assertThat(analysis.getInternalLinks())
        .extracting(InternalLinkElement::begin, InternalLinkElement::end, InternalLinkElement::text)
        .containsExactly(Assertions.tuple(19, 26, "d"));
  }

  @DisplayName("should display the text of a link, or its target with its anchor")
  @Test
  void analyzeDisplayedText() {
    // GIVEN
    final String text = "[[Paris| la ville ]] [[:Paris]] [[Paris#Histoire]]";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getInternalLinks())
        .map(InternalLinkElement::displayedText)
        .containsExactly(" la ville ", "Paris", "Paris#Histoire");
  }
}
