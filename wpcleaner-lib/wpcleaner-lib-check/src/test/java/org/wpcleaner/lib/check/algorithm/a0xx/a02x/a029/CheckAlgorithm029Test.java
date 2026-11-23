package org.wpcleaner.lib.check.algorithm.a0xx.a02x.a029;

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
class CheckAlgorithm029Test {

  private static final int ALGORITHM = 29;

  @Autowired private CheckAlgorithm029 algorithm;

  @DisplayName("should report an unclosed <gallery> tag, suggesting its removal")
  @Test
  void reportUnclosedStartTag() {
    // GIVEN
    final String text = "a <gallery>\nFile:A.jpg\n";

    // WHEN
    final List<CheckError> errors = analyze("Paris", text);

    // THEN
    Assertions.assertThat(errors).containsExactly(removal(text, "<gallery>"));
  }

  @DisplayName("should report a </gallery> tag without start tag, suggesting its removal")
  @Test
  void reportUnmatchedEndTag() {
    // GIVEN
    final String text = "<gallery>\nFile:A.jpg\n</gallery>\n</gallery>";

    // WHEN
    final List<CheckError> errors = analyze("Paris", text);

    // THEN
    final int begin = text.lastIndexOf("</gallery>");
    Assertions.assertThat(errors)
        .containsExactly(
            new CheckError(ALGORITHM, begin, text.length(), List.of(new Replacement("", false))));
  }

  @DisplayName("should not report complete <gallery> tags, whatever the case or the attributes")
  @Test
  void ignoreCompleteTags() {
    // GIVEN
    final String text =
        "<gallery>\nFile:A.jpg\n</gallery> <GALLERY mode=\"packed\">\nFile:B.jpg\n</gallery>";

    // WHEN
    final List<CheckError> errors = analyze("Paris", text);

    // THEN
    Assertions.assertThat(errors).isEmpty();
  }

  @DisplayName("should report a self closing <gallery/> tag, suggesting its removal")
  @Test
  void reportSelfClosingTag() {
    // GIVEN
    final String text = "a <gallery/> b <gallery />";

    // WHEN
    final List<CheckError> errors = analyze("Paris", text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(removal(text, "<gallery/>"), removal(text, "<gallery />"));
  }

  @DisplayName("should report a </gallery > end tag with whitespace, fixing it automatically")
  @Test
  void reportEndTagWithWhitespace() {
    // GIVEN
    final String text = "<gallery>\nFile:A.jpg\n</gallery >";

    // WHEN
    final List<CheckError> errors = analyze("Paris", text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(error(text, "</gallery >", new Replacement("</gallery>", true)));
  }

  @DisplayName("should merge a duplicated <gallery> start tag with the previous one")
  @Test
  void mergeDuplicatedStartTag() {
    // GIVEN
    final String text = "a <gallery>\n <gallery>\nFile:A.jpg\n</gallery>";

    // WHEN
    final List<CheckError> errors = analyze("Paris", text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(error(text, "<gallery>\n <gallery>", new Replacement("<gallery>", false)));
  }

  @DisplayName(
      "should merge adjacent <gallery> start tags, suggesting first the one with attributes")
  @Test
  void mergeAdjacentStartTagsWithAttributes() {
    // GIVEN
    final String text =
        "<gallery> <gallery mode=\"packed\">\nFile:A.jpg\n</gallery>\n"
            + "<gallery caption=\"b\"> <gallery>\nFile:B.jpg\n</gallery>";

    // WHEN
    final List<CheckError> errors = analyze("Paris", text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(
            error(
                text,
                "<gallery> <gallery mode=\"packed\">",
                new Replacement("<gallery mode=\"packed\">", false),
                new Replacement("<gallery>", false)),
            error(
                text,
                "<gallery caption=\"b\"> <gallery>",
                new Replacement("<gallery caption=\"b\">", false),
                new Replacement("<gallery>", false)));
  }

  @DisplayName(
      "should not merge <gallery> start tags when both have attributes or when separated by text")
  @Test
  void keepNonAdjacentStartTags() {
    // GIVEN
    final String text =
        "<gallery caption=\"a\"> <gallery mode=\"packed\">\nFile:A.jpg\n</gallery>\n"
            + "<gallery> b <gallery>\nFile:B.jpg\n</gallery>";

    // WHEN
    final List<CheckError> errors = analyze("Paris", text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(
            removal(text, "<gallery mode=\"packed\">"), removal(text, "b <gallery>", "<gallery>"));
  }

  @DisplayName("should not merge an unclosed <gallery> tag with a previous unclosed one")
  @Test
  void keepUnclosedPreviousTag() {
    // GIVEN
    final String text = "a <gallery> <gallery>\nFile:A.jpg\n";

    // WHEN
    final List<CheckError> errors = analyze("Paris", text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(removal(text, "<gallery>"), removal(text, "> <gallery>", "<gallery>"));
  }

  @DisplayName("should merge only unclosed <gallery> start tags with an adjacent previous tag")
  @Test
  void mergeOnlyUnclosedStartTags() {
    // GIVEN
    final String text =
        "<gallery/>\n<gallery> <gallery>\nFile:A.jpg\n</gallery>\n<gallery> </gallery >";

    // WHEN
    final List<CheckError> errors = analyze("Paris", text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(
            removal(text, "<gallery/>"),
            error(text, "<gallery> <gallery>", new Replacement("<gallery>", false)),
            error(text, "</gallery >", new Replacement("</gallery>", true)));
  }

  @DisplayName("should report tags in all namespaces, including talk namespaces")
  @Test
  void reportInAllNamespaces() {
    // GIVEN
    final String text = "a <gallery>\nFile:A.jpg\n";

    // WHEN
    final List<CheckError> templateErrors = analyze("Modèle:Paris", text);
    final List<CheckError> talkErrors = analyze("Discussion utilisateur:Paris", text);

    // THEN
    Assertions.assertThat(templateErrors).hasSize(1);
    Assertions.assertThat(talkErrors).hasSize(1);
  }

  @DisplayName("should not report <gallery> tags in comments and non wiki content")
  @Test
  void ignoreTagsInExcludedAreas() {
    // GIVEN
    final String text =
        "a <!-- <gallery> --> <nowiki><gallery></nowiki> <syntaxhighlight lang=\"x\"></gallery></syntaxhighlight>";

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

  private static CheckError removal(final String text, final String context, final String found) {
    final int begin = text.indexOf(context) + context.indexOf(found);
    return new CheckError(
        ALGORITHM, begin, begin + found.length(), List.of(new Replacement("", false)));
  }

  private static CheckError error(
      final String text, final String found, final Replacement... replacements) {
    final int begin = text.indexOf(found);
    return new CheckError(ALGORITHM, begin, begin + found.length(), List.of(replacements));
  }
}
