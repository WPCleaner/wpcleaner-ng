package org.wpcleaner.lib.check.algorithm.a1xx.a10x.a109;

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
class CheckAlgorithm109Test {

  private static final int ALGORITHM = 109;

  @Autowired private CheckAlgorithm109 algorithm;

  @DisplayName("should report an unclosed <noinclude> tag, suggesting its removal")
  @Test
  void reportUnclosedStartTag() {
    // GIVEN
    final String text = "a <noinclude>b";

    // WHEN
    final List<CheckError> errors = analyze("Paris", text);

    // THEN
    Assertions.assertThat(errors).containsExactly(removal(text, "<noinclude>"));
  }

  @DisplayName("should report a </noinclude> tag without start tag, suggesting its removal")
  @Test
  void reportUnmatchedEndTag() {
    // GIVEN
    final String text = "a</noinclude> b";

    // WHEN
    final List<CheckError> errors = analyze("Paris", text);

    // THEN
    Assertions.assertThat(errors).containsExactly(removal(text, "</noinclude>"));
  }

  @DisplayName(
      "should not report complete <noinclude> tags, whatever the case or the whitespace in the start tag")
  @Test
  void ignoreCompleteTags() {
    // GIVEN
    final String text =
        "<noinclude>a</noinclude> <NOINCLUDE>b</noinclude> <noinclude >c</noinclude>";

    // WHEN
    final List<CheckError> errors = analyze("Paris", text);

    // THEN
    Assertions.assertThat(errors).isEmpty();
  }

  @DisplayName("should report a self closing <noinclude/> tag, suggesting its removal")
  @Test
  void reportSelfClosingTag() {
    // GIVEN
    final String text = "a <noinclude/> b <noinclude />";

    // WHEN
    final List<CheckError> errors = analyze("Paris", text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(removal(text, "<noinclude/>"), removal(text, "<noinclude />"));
  }

  @DisplayName("should report a </noinclude > end tag with whitespace, fixing it automatically")
  @Test
  void reportEndTagWithWhitespace() {
    // GIVEN
    final String text = "<noinclude>a</noinclude > <NOINCLUDE>b</NOINCLUDE\n>";

    // WHEN
    final List<CheckError> errors = analyze("Paris", text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(
            error(text, "</noinclude >", new Replacement("</noinclude>", true)),
            error(text, "</NOINCLUDE\n>", new Replacement("</NOINCLUDE>", true)));
  }

  @DisplayName("should report tags in all namespaces, including talk namespaces")
  @Test
  void reportInAllNamespaces() {
    // GIVEN
    final String text = "a <noinclude>b";

    // WHEN
    final List<CheckError> templateErrors = analyze("Modèle:Paris", text);
    final List<CheckError> talkErrors = analyze("Discussion utilisateur:Paris", text);

    // THEN
    Assertions.assertThat(templateErrors).hasSize(1);
    Assertions.assertThat(talkErrors).hasSize(1);
  }

  @DisplayName("should not report other tags, or <noinclude> tags in comments and non wiki content")
  @Test
  void ignoreOtherTags() {
    // GIVEN
    final String text =
        "<span>a <!-- <noinclude> --> <nowiki><noinclude></nowiki> <syntaxhighlight lang=\"x\"></noinclude></syntaxhighlight>";

    // WHEN
    final List<CheckError> errors = analyze("Paris", text);

    // THEN
    Assertions.assertThat(errors).isEmpty();
  }

  @DisplayName("should also report unclosed <includeonly> tags")
  @Test
  void reportIncludeonlyTags() {
    // GIVEN
    final String text = "a</includeonly> <includeonly>b</includeonly>";

    // WHEN
    final List<CheckError> errors = analyze("Paris", text);

    // THEN
    Assertions.assertThat(errors).containsExactly(removal(text, "</includeonly>"));
  }

  @DisplayName("should also report unclosed <onlyinclude> tags")
  @Test
  void reportOnlyincludeTags() {
    // GIVEN
    final String text = "a</onlyinclude> <onlyinclude>b</onlyinclude>";

    // WHEN
    final List<CheckError> errors = analyze("Paris", text);

    // THEN
    Assertions.assertThat(errors).containsExactly(removal(text, "</onlyinclude>"));
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
