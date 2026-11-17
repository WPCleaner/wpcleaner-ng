package org.wpcleaner.lib.check.algorithm.a0xx.a01x.a014;

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
class CheckAlgorithm014Test {

  private static final int ALGORITHM = 14;

  @Autowired private CheckAlgorithm014 algorithm;

  @DisplayName("should report an unclosed <source> tag, suggesting its removal")
  @Test
  void reportUnclosedStartTag() {
    // GIVEN
    final String text = "a <source>b";

    // WHEN
    final List<CheckError> errors = analyze("Paris", text);

    // THEN
    Assertions.assertThat(errors).containsExactly(removal(text, "<source>"));
  }

  @DisplayName("should report a </source> tag without start tag, suggesting its removal")
  @Test
  void reportUnmatchedEndTag() {
    // GIVEN
    final String text = "a</source> b";

    // WHEN
    final List<CheckError> errors = analyze("Paris", text);

    // THEN
    Assertions.assertThat(errors).containsExactly(removal(text, "</source>"));
  }

  @DisplayName(
      "should not report complete <source> tags, whatever the case or the whitespace in the start tag")
  @Test
  void ignoreCompleteTags() {
    // GIVEN
    final String text = "<source>a</source> <SOURCE>b</source> <source >c</source>";

    // WHEN
    final List<CheckError> errors = analyze("Paris", text);

    // THEN
    Assertions.assertThat(errors).isEmpty();
  }

  @DisplayName("should report a self closing <source/> tag, suggesting its removal")
  @Test
  void reportSelfClosingTag() {
    // GIVEN
    final String text = "a <source/> b <source />";

    // WHEN
    final List<CheckError> errors = analyze("Paris", text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(removal(text, "<source/>"), removal(text, "<source />"));
  }

  @DisplayName("should report a </source > end tag with whitespace, fixing it automatically")
  @Test
  void reportEndTagWithWhitespace() {
    // GIVEN
    final String text = "<source>a</source > <SOURCE>b</SOURCE\n>";

    // WHEN
    final List<CheckError> errors = analyze("Paris", text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(
            error(text, "</source >", new Replacement("</source>", true)),
            error(text, "</SOURCE\n>", new Replacement("</SOURCE>", true)));
  }

  @DisplayName("should report tags in all namespaces, including talk namespaces")
  @Test
  void reportInAllNamespaces() {
    // GIVEN
    final String text = "a <source>b";

    // WHEN
    final List<CheckError> templateErrors = analyze("Modèle:Paris", text);
    final List<CheckError> talkErrors = analyze("Discussion utilisateur:Paris", text);

    // THEN
    Assertions.assertThat(templateErrors).hasSize(1);
    Assertions.assertThat(talkErrors).hasSize(1);
  }

  @DisplayName("should not report other tags, or <source> tags in comments and non wiki content")
  @Test
  void ignoreOtherTags() {
    // GIVEN
    final String text =
        "<span>a <!-- <source> --> <nowiki><source></nowiki> <math></source></math>";

    // WHEN
    final List<CheckError> errors = analyze("Paris", text);

    // THEN
    Assertions.assertThat(errors).isEmpty();
  }

  @DisplayName("should also report unclosed <syntaxhighlight> tags")
  @Test
  void reportSyntaxhighlightTags() {
    // GIVEN
    final String text = "a</syntaxhighlight> <syntaxhighlight>b</syntaxhighlight>";

    // WHEN
    final List<CheckError> errors = analyze("Paris", text);

    // THEN
    Assertions.assertThat(errors).containsExactly(removal(text, "</syntaxhighlight>"));
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
