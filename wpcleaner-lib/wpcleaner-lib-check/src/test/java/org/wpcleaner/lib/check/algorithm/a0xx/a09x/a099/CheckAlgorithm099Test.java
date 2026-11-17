package org.wpcleaner.lib.check.algorithm.a0xx.a09x.a099;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.wpcleaner.api.analysis.PageAnalysisTestFactory;
import org.wpcleaner.lib.check.CheckError;
import org.wpcleaner.lib.check.CheckSpringBootTest;
import org.wpcleaner.lib.check.Replacement;

@CheckSpringBootTest
class CheckAlgorithm099Test {

  private static final int ALGORITHM = 99;

  @Autowired private CheckAlgorithm099 algorithm;

  @DisplayName("should report an unclosed <sup> tag, suggesting its removal")
  @Test
  void reportUnclosedStartTag() {
    // GIVEN
    final String text = "a <sup>b";

    // WHEN
    final List<CheckError> errors = analyze("Paris", text);

    // THEN
    Assertions.assertThat(errors).containsExactly(removal(text, "<sup>"));
  }

  @DisplayName("should report a </sup> tag without start tag, suggesting its removal")
  @Test
  void reportUnmatchedEndTag() {
    // GIVEN
    final String text = "a</sup> b";

    // WHEN
    final List<CheckError> errors = analyze("Paris", text);

    // THEN
    Assertions.assertThat(errors).containsExactly(removal(text, "</sup>"));
  }

  @DisplayName(
      "should not report complete <sup> tags, whatever the case or the whitespace in the start tag")
  @Test
  void ignoreCompleteTags() {
    // GIVEN
    final String text = "<sup>a</sup> <SUP>b</sup> <sup >c</sup>";

    // WHEN
    final List<CheckError> errors = analyze("Paris", text);

    // THEN
    Assertions.assertThat(errors).isEmpty();
  }

  @DisplayName("should report a self closing <sup/> tag, suggesting its removal")
  @Test
  void reportSelfClosingTag() {
    // GIVEN
    final String text = "a <sup/> b <sup />";

    // WHEN
    final List<CheckError> errors = analyze("Paris", text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(removal(text, "<sup/>"), removal(text, "<sup />"));
  }

  @DisplayName("should report a </sup > end tag with whitespace, fixing it automatically")
  @Test
  void reportEndTagWithWhitespace() {
    // GIVEN
    final String text = "<sup>a</sup > <SUP>b</SUP\n>";

    // WHEN
    final List<CheckError> errors = analyze("Paris", text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(
            error(text, "</sup >", new Replacement("</sup>", true)),
            error(text, "</SUP\n>", new Replacement("</SUP>", true)));
  }

  @DisplayName("should report tags in all namespaces, including talk namespaces")
  @Test
  void reportInAllNamespaces() {
    // GIVEN
    final String text = "a <sup>b";

    // WHEN
    final List<CheckError> templateErrors = analyze("Modèle:Paris", text);
    final List<CheckError> talkErrors = analyze("Discussion utilisateur:Paris", text);

    // THEN
    Assertions.assertThat(templateErrors).hasSize(1);
    Assertions.assertThat(talkErrors).hasSize(1);
  }

  @DisplayName("should not report other tags, or <sup> tags in comments and non wiki content")
  @Test
  void ignoreOtherTags() {
    // GIVEN
    final String text =
        "<span>a <!-- <sup> --> <nowiki><sup></nowiki> <syntaxhighlight lang=\"x\"></sup></syntaxhighlight>";

    // WHEN
    final List<CheckError> errors = analyze("Paris", text);

    // THEN
    Assertions.assertThat(errors).isEmpty();
  }

  private List<CheckError> analyze(final String title, final String text) {
    return algorithm.analyze(PageAnalysisTestFactory.createPageAnalysis(title, text));
  }

  private static CheckError removal(final String text, final String found) {
    return error(text, found, new Replacement("", false));
  }

  private static CheckError error(
      final String text, final String found, final Replacement replacement) {
    final int begin = text.indexOf(found);
    return new CheckError(ALGORITHM, begin, begin + found.length(), List.of(replacement));
  }
}
