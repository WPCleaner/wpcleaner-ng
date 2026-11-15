package org.wpcleaner.lib.check.algorithm.a0xx.a00x.a004;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.wpcleaner.api.analysis.PageAnalysisTestFactory;
import org.wpcleaner.api.repository.protocol.Protocol;
import org.wpcleaner.api.repository.protocol.ProtocolRepository;
import org.wpcleaner.lib.check.CheckError;
import org.wpcleaner.lib.check.CheckSpringBootTest;
import org.wpcleaner.lib.check.Replacement;

@CheckSpringBootTest
class CheckAlgorithm004Test {

  private static final int ALGORITHM = 4;

  @Autowired private CheckAlgorithm004 algorithm;
  @Autowired private ProtocolRepository protocolRepository;

  @BeforeEach
  void setUp() {
    protocolRepository.addProtocol(PageAnalysisTestFactory.WIKI, new Protocol("https://"));
    protocolRepository.addProtocol(PageAnalysisTestFactory.WIKI, new Protocol("http://"));
  }

  @DisplayName("should offer an external link and the text for a link to an external site")
  @Test
  void reportExternalLink() {
    // GIVEN
    final String text = "<a href=\"https://example.org/page\">site</a>";

    // WHEN
    final List<CheckError> errors = analyze(text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(
            new CheckError(
                ALGORITHM,
                0,
                text.length(),
                List.of(
                    new Replacement("[https://example.org/page site]", false),
                    new Replacement("site", false))));
  }

  @DisplayName("should fix automatically a link to an article of the wiki")
  @Test
  void fixArticleLink() {
    // GIVEN
    final String text = "<a href=\"https://fr.wikipedia.org/wiki/Paris\">la capitale</a>";

    // WHEN
    final List<CheckError> errors = analyze(text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(
            new CheckError(
                ALGORITHM,
                0,
                text.length(),
                List.of(
                    new Replacement("[[Paris|la capitale]]", true),
                    new Replacement("[https://fr.wikipedia.org/wiki/Paris la capitale]", false),
                    new Replacement("la capitale", false))));
  }

  @DisplayName("should not fix automatically an article link with a fragment or attributes")
  @Test
  void keepManualArticleLinkWithFragment() {
    // GIVEN
    final String text =
        "<a href=\"https://fr.wikipedia.org/wiki/Paris#Histoire\">a</a>"
            + "<a href=\"https://fr.wikipedia.org/w/index.php?title=Paris&action=edit\">b</a>";

    // WHEN
    final List<CheckError> errors = analyze(text);

    // THEN
    Assertions.assertThat(errors)
        .extracting(error -> error.replacements().getFirst())
        .containsExactly(
            new Replacement("[[Paris#Histoire|a]]", false), new Replacement("[[Paris|b]]", false));
  }

  @DisplayName("should fix automatically a telephone link")
  @Test
  void fixTelephoneLink() {
    // GIVEN
    final String text = "<a href=\"tel:+33123456789\">01 23 45 67 89</a>";

    // WHEN
    final List<CheckError> errors = analyze(text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(
            new CheckError(
                ALGORITHM, 0, text.length(), List.of(new Replacement("01 23 45 67 89", true))));
  }

  @DisplayName("should only offer the text when there is no usable href")
  @Test
  void reportLinkWithoutHref() {
    // GIVEN
    final String text = "<a name=\"x\">texte</a> <a href=\"page\">autre</a>";

    // WHEN
    final List<CheckError> errors = analyze(text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(
            new CheckError(ALGORITHM, 0, 21, List.of(new Replacement("texte", false))),
            new CheckError(ALGORITHM, 22, text.length(), List.of(new Replacement("autre", false))));
  }

  @DisplayName("should offer to remove full tags and incomplete tags")
  @Test
  void reportFullAndIncompleteTags() {
    // GIVEN
    final String text = "<a href=\"x\" /> texte <a name=\"y\"> fin </a>";

    // WHEN
    final List<CheckError> errors = analyze(text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(
            new CheckError(ALGORITHM, 0, 14, List.of(new Replacement("", false))),
            new CheckError(ALGORITHM, 21, text.length(), List.of(new Replacement(" fin ", false))));
  }

  @DisplayName("should offer to remove an orphan start tag and an orphan end tag")
  @Test
  void reportOrphanTags() {
    // GIVEN
    final String text = "<a href=\"x\"> texte</a> </a> <a>";

    // WHEN
    final List<CheckError> errors = analyze(text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(
            new CheckError(ALGORITHM, 0, 22, List.of(new Replacement(" texte", false))),
            new CheckError(ALGORITHM, 23, 27, List.of(new Replacement("", false))),
            new CheckError(ALGORITHM, 28, 31, List.of(new Replacement("", false))));
  }

  @DisplayName("should ignore tags in code, math, nowiki, pre, source and syntaxhighlight")
  @Test
  void ignoreTagsInNonWikiAreas() {
    // GIVEN
    final String text =
        "<code><a href=\"x\">a</a></code> <nowiki><a>b</a></nowiki> <pre><a>c</a></pre> "
            + "<math><a>d</a></math> <source><a>e</a></source> "
            + "<syntaxhighlight><a>f</a></syntaxhighlight>";

    // WHEN
    final List<CheckError> errors = analyze(text);

    // THEN
    Assertions.assertThat(errors).isEmpty();
  }

  @DisplayName("should ignore tags in imagemap")
  @Test
  void ignoreTagsInImagemap() {
    // GIVEN
    final String text = "<imagemap>\nImage:A.png\n<a href=\"x\">a</a>\n</imagemap>";

    // WHEN
    final List<CheckError> errors = analyze(text);

    // THEN
    Assertions.assertThat(errors).isEmpty();
  }

  @DisplayName("should ignore pages outside the main namespace")
  @Test
  void ignoreOtherNamespaces() {
    // GIVEN
    final String text = "<a href=\"https://example.org/\">site</a>";

    // WHEN
    final List<CheckError> errors =
        algorithm.analyze(PageAnalysisTestFactory.createPageAnalysis("Utilisateur:Paris", text));

    // THEN
    Assertions.assertThat(errors).isEmpty();
  }

  private List<CheckError> analyze(final String text) {
    return algorithm.analyze(PageAnalysisTestFactory.createPageAnalysis("Paris", text));
  }
}
