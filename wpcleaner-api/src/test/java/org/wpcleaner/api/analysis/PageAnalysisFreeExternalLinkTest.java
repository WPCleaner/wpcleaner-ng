package org.wpcleaner.api.analysis;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.wpcleaner.api.analysis.externallink.ExternalLinkElement;

class PageAnalysisFreeExternalLinkTest {

  @DisplayName("should analyze free external link without trailing punctuation")
  @Test
  void analyzeFreeExternalLink() {
    // GIVEN
    final String text = "See https://example.com/page, and more.";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getExternalLinks())
        .containsExactly(new ExternalLinkElement(4, 28, "https://example.com/page", null, false));
  }

  @DisplayName("should keep closing parenthesis only when the URL has an opening one")
  @Test
  void analyzeFreeExternalLinksWithParenthesis() {
    // GIVEN
    final String text = "(see http://a.org/x) http://a.org/Foo_(bar).";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getExternalLinks())
        .containsExactly(
            new ExternalLinkElement(5, 19, "http://a.org/x", null, false),
            new ExternalLinkElement(21, 43, "http://a.org/Foo_(bar)", null, false));
  }

  @DisplayName("should end free external link at escaped angle brackets but keep trailing entities")
  @Test
  void analyzeFreeExternalLinksWithEntities() {
    // GIVEN
    final String text = "http://a.org/&lt;b http://a.org/x&amp;";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getExternalLinks())
        .containsExactly(
            new ExternalLinkElement(0, 13, "http://a.org/", null, false),
            new ExternalLinkElement(19, 38, "http://a.org/x&amp;", null, false));
  }

  @DisplayName("should not analyze free external links in links, image options or comments")
  @Test
  void analyzeFreeExternalLinksInsideOtherElements() {
    // GIVEN
    final String text =
        "[http://a.org b] [[File:X.jpg|link=http://b.org|Voir http://c.org]]"
            + " xhttp://d.org ftp://e.org <!-- http://f.org -->";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getExternalLinks())
        .containsExactly(
            new ExternalLinkElement(0, 16, "http://a.org", "b", true),
            new ExternalLinkElement(53, 65, "http://c.org", null, false));
  }

  @DisplayName("should end free external link at the end of a template parameter or at a template")
  @Test
  void analyzeFreeExternalLinksInsideTemplates() {
    // GIVEN
    final String text = "{{T|http://a.org|b}} {{T|url=http://b.org/{{PAGENAME}}}}";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getExternalLinks())
        .containsExactly(
            new ExternalLinkElement(4, 16, "http://a.org", null, false),
            new ExternalLinkElement(29, 42, "http://b.org/", null, false));
  }

  @DisplayName("should not analyze magic link inside a free external link")
  @Test
  void analyzeMagicLinkInsideFreeExternalLink() {
    // GIVEN
    final String text = "http://a.org/-ISBN&nbsp;1234567890";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getExternalLinks())
        .containsExactly(new ExternalLinkElement(0, 18, "http://a.org/-ISBN", null, false));
    Assertions.assertThat(analysis.getMagicLinks()).isEmpty();
  }
}
