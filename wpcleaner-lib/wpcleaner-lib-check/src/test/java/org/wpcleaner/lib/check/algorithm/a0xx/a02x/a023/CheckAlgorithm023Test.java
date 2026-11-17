package org.wpcleaner.lib.check.algorithm.a0xx.a02x.a023;

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
class CheckAlgorithm023Test {

  private static final int ALGORITHM = 23;

  @Autowired private CheckAlgorithm023 algorithm;

  @DisplayName("should report an unclosed <nowiki> tag, suggesting its removal")
  @Test
  void reportUnclosedStartTag() {
    // GIVEN
    final String text = "a <nowiki>b";

    // WHEN
    final List<CheckError> errors = analyze("Paris", text);

    // THEN
    Assertions.assertThat(errors).containsExactly(removal(text, "<nowiki>"));
  }

  @DisplayName("should report a </nowiki> tag without start tag, suggesting its removal")
  @Test
  void reportUnmatchedEndTag() {
    // GIVEN
    final String text = "a</nowiki> b";

    // WHEN
    final List<CheckError> errors = analyze("Paris", text);

    // THEN
    Assertions.assertThat(errors).containsExactly(removal(text, "</nowiki>"));
  }

  @DisplayName(
      "should not report complete <nowiki> tags, whatever the case or the whitespace in the start tag")
  @Test
  void ignoreCompleteTags() {
    // GIVEN
    final String text = "<nowiki>a</nowiki> <NOWIKI>b</nowiki> <nowiki >c</nowiki>";

    // WHEN
    final List<CheckError> errors = analyze("Paris", text);

    // THEN
    Assertions.assertThat(errors).isEmpty();
  }

  @DisplayName("should not report a self closing <nowiki/> tag")
  @Test
  void ignoreSelfClosingTag() {
    // GIVEN
    final String text = "a <nowiki/> b <nowiki />";

    // WHEN
    final List<CheckError> errors = analyze("Paris", text);

    // THEN
    Assertions.assertThat(errors).isEmpty();
  }

  @DisplayName("should report a </nowiki > end tag with whitespace, fixing it automatically")
  @Test
  void reportEndTagWithWhitespace() {
    // GIVEN
    final String text = "<nowiki>a</nowiki > <NOWIKI>b</NOWIKI\n>";

    // WHEN
    final List<CheckError> errors = analyze("Paris", text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(
            error(text, "</nowiki >", new Replacement("</nowiki>", true)),
            error(text, "</NOWIKI\n>", new Replacement("</NOWIKI>", true)));
  }

  @DisplayName("should report tags in all namespaces, including talk namespaces")
  @Test
  void reportInAllNamespaces() {
    // GIVEN
    final String text = "a <nowiki>b";

    // WHEN
    final List<CheckError> templateErrors = analyze("Modèle:Paris", text);
    final List<CheckError> talkErrors = analyze("Discussion utilisateur:Paris", text);

    // THEN
    Assertions.assertThat(templateErrors).hasSize(1);
    Assertions.assertThat(talkErrors).hasSize(1);
  }

  @DisplayName("should not report other tags, or <nowiki> tags in comments and non wiki content")
  @Test
  void ignoreOtherTags() {
    // GIVEN
    final String text =
        "<span>a <!-- <nowiki> --> <pre><nowiki></pre> <syntaxhighlight lang=\"x\"></nowiki></syntaxhighlight>";

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
