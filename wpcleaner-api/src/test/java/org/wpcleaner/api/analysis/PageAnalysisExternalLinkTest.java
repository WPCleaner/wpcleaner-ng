package org.wpcleaner.api.analysis;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.assertj.core.api.Assertions;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.wpcleaner.api.analysis.externallink.ExternalLinkElement;

class PageAnalysisExternalLinkTest {

  @DisplayName("should analyze external link when protocol is known")
  @Test
  void analyzeExternalLink() {
    // GIVEN
    final String text = "[https://example.com/ Hello]";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getExternalLinks()).hasSize(1);
    Assertions.assertThat(analysis.getExternalLinks().getFirst().begin()).isZero();
    Assertions.assertThat(analysis.getExternalLinks().getFirst().end()).isEqualTo(text.length());
  }

  @DisplayName("should not analyze external link when protocol is unknown")
  @Test
  void analyzeExternalLinkWithUnknownProtocol() {
    // GIVEN
    final String text = "[ftp://example.com/ Hello]";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getExternalLinks()).isEmpty();
  }

  @DisplayName("should analyze external link parts")
  @Test
  void analyzeExternalLinkParts() {
    // GIVEN
    final String text = "[https://example.com/ Hello world]";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getExternalLinks())
        .containsExactly(
            new ExternalLinkElement(0, text.length(), "https://example.com/", "Hello world", true));
  }

  @DisplayName("should analyze external link without text")
  @Test
  void analyzeExternalLinkWithoutText() {
    // GIVEN
    final String text = "[https://example.com/]";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getExternalLinks())
        .containsExactly(
            new ExternalLinkElement(0, text.length(), "https://example.com/", null, true));
  }

  @DisplayName("should analyze external link followed by a closing bracket")
  @Test
  void analyzeExternalLinkFollowedByClosingBracket() {
    // GIVEN
    final String text = "[https://example.com/ b]]";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getExternalLinks())
        .containsExactly(new ExternalLinkElement(0, 24, "https://example.com/", "b", true));
  }

  @DisplayName("should analyze external link inside double brackets")
  @Test
  void analyzeExternalLinkInsideDoubleBrackets() {
    // GIVEN
    final String text = "[[https://example.com/ b]] [[https://example.com/]]";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getInternalLinks()).isEmpty();
    Assertions.assertThat(analysis.getExternalLinks())
        .containsExactly(
            new ExternalLinkElement(1, 25, "https://example.com/", "b", true),
            new ExternalLinkElement(28, 50, "https://example.com/", null, true));
  }

  @DisplayName("should analyze external link after several brackets, even with brackets in text")
  @Test
  void analyzeExternalLinkAfterSeveralBrackets() {
    // GIVEN
    final String text =
        "[[http://a.org/&#x5B;A http://a.org/[A]] [[[https://b.org/|B](x)] [[[[ HTTPS://c.org/]";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getInternalLinks()).isEmpty();
    Assertions.assertThat(analysis.getExternalLinks())
        .containsExactly(
            new ExternalLinkElement(
                1, text.indexOf("]]") + 1, "http://a.org/&#x5B;A", "http://a.org/[A", true),
            new ExternalLinkElement(
                text.indexOf("[https://b"), text.indexOf("(x)"), "https://b.org/|B", null, true),
            new ExternalLinkElement(
                text.indexOf("HTTPS"), text.length() - 1, "HTTPS://c.org/", null, false));
  }

  @DisplayName("should end external link URL like MediaWiki")
  @ParameterizedTest
  @CsvSource(
      delimiter = ';',
      quoteCharacter = '`',
      value = {
        "[https://a.org/[x A];https://a.org/;[x A",
        "[https://a.org/\"x\" A];https://a.org/;\"x\" A",
        "[https://a.org/<b>x</b> A];https://a.org/;<b>x</b> A",
        "[https://a.org/<x A];https://a.org/;<x A",
        "[https://a.org/>x A];https://a.org/;>x A",
        "[https://a.org/\u00A0A];https://a.org/;A",
        "[https://a.org/\u2003\u2003A];https://a.org/;A",
        "`[https://a.org/\tA]`;https://a.org/;`\tA`",
        "[https://a.org/<!-- c -->x A];https://a.org/x;A",
        "[https://a.org/ <!-- c --> A];https://a.org/;A",
        "[https://a.org/<nowiki/>x A];https://a.org/;<nowiki/>x A",
        "[https://a.org/<nowiki/>];https://a.org/;<nowiki/>",
        "[https://a.org/\"];https://a.org/;\"",
        "[https://a.org/ ];https://a.org/;``",
        "[https://a.org/\u00A0];https://a.org/;``",
        "[https://a.org/<ref>r</ref> A];https://a.org/;<ref>r</ref> A",
        "[https://a.org/];https://a.org/;",
        "[https://a.org/x'y A];https://a.org/x'y;A",
        "[https://a.org/x''A''];https://a.org/x;''A''",
        "[https://a.org/x'''A'''];https://a.org/x;'''A'''",
        "[https://a.org/x''''A''''];https://a.org/x';'''A''''",
        "[https://a.org/x'''''A'''''];https://a.org/x;'''''A'''''",
        "[https://a.org/x''''''A''''''];https://a.org/x';'''''A''''''",
        "[https://a.org/x'''''''A];https://a.org/x'';'''''A"
      })
  void analyzeExternalLinkUrlEnd(
      final String text, final String target, @Nullable final String linkText) {
    // WHEN
    final PageAnalysis analysis =
        PageAnalysisTestFactory.createPageAnalysis(text.translateEscapes());

    // THEN
    Assertions.assertThat(analysis.getExternalLinks())
        .extracting(ExternalLinkElement::target, ExternalLinkElement::text)
        .containsExactly(
            Assertions.tuple(target, linkText == null ? null : linkText.translateEscapes()));
  }

  @DisplayName("should analyze external link with a pipe in its URL")
  @Test
  void analyzeExternalLinkWithPipe() {
    // GIVEN
    final String text =
        "[https://example.com/|b] [[https://example.com/|b]] {{a|[https://example.com/|b]}}";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getInternalLinks()).isEmpty();
    Assertions.assertThat(analysis.getExternalLinks())
        .extracting(
            ExternalLinkElement::begin, ExternalLinkElement::target, ExternalLinkElement::bracketed)
        .containsExactly(
            Assertions.tuple(0, "https://example.com/|b", true),
            Assertions.tuple(26, "https://example.com/|b", true),
            Assertions.tuple(57, "https://example.com/", false));
  }

  @DisplayName("should analyze protocol-relative URLs only between brackets")
  @Test
  void analyzeProtocolRelativeExternalLink() {
    // GIVEN
    final String text = "[//example.com/ b] //example.com/ x//example.com/ [[//example.com/ c]]";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    final int inner = text.indexOf("[//", 1);
    Assertions.assertThat(analysis.getInternalLinks()).isEmpty();
    Assertions.assertThat(analysis.getExternalLinks())
        .containsExactly(
            new ExternalLinkElement(0, 18, "//example.com/", "b", true),
            new ExternalLinkElement(inner, text.length() - 1, "//example.com/", "c", true));
  }

  @DisplayName("should analyze external link whatever the case of its protocol")
  @Test
  void analyzeExternalLinkWithUpperCaseProtocol() {
    // GIVEN
    final String text = "[HTTPS://example.com/ b] [[Https://example.com/ c]]";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getInternalLinks()).isEmpty();
    Assertions.assertThat(analysis.getExternalLinks())
        .containsExactly(
            new ExternalLinkElement(0, 24, "HTTPS://example.com/", "b", true),
            new ExternalLinkElement(26, text.length() - 1, "Https://example.com/", "c", true));
  }
}
