package org.wpcleaner.lib.check.algorithm.a0xx.a02x.a026;

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
class CheckAlgorithm026Test {

  private static final int ALGORITHM = 26;
  private static final List<Replacement> DELETION = List.of(new Replacement("", false));

  @Autowired private CheckAlgorithm026 algorithm;

  @DisplayName("should suggest bold wiki syntax or plain text for a complete tag")
  @Test
  void replaceCompleteTag() {
    // GIVEN
    final String text = "a <B class=\"x\">bold text</b> c";

    // WHEN
    final List<CheckError> errors = analyze("Paris", text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(
            error(
                text,
                "<B class=\"x\">bold text</b>",
                List.of(
                    new Replacement("'''bold text'''", false),
                    new Replacement("bold text", false))));
  }

  @DisplayName("should suggest deleting a complete tag with blank contents")
  @Test
  void deleteBlankTag() {
    // GIVEN
    final String text = "a <b> </b> c";

    // WHEN
    final List<CheckError> errors = analyze("Paris", text);

    // THEN
    Assertions.assertThat(errors).containsExactly(error(text, "<b> </b>", DELETION));
  }

  @DisplayName("should suggest deleting self closed, unclosed and unopened tags")
  @Test
  void deleteIncompleteTags() {
    // GIVEN
    final String text = "a <b/> c </b> d <b> e";

    // WHEN
    final List<CheckError> errors = analyze("Paris", text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(
            error(text, "<b/>", DELETION),
            error(text, "</b>", DELETION),
            error(text, "<b>", DELETION));
  }

  @DisplayName("should report tags in other subject namespaces, but not in files or talk pages")
  @Test
  void ignoreNamespaces() {
    // GIVEN
    final String text = "<b>a</b>";

    // WHEN
    final List<CheckError> userErrors = analyze("Utilisateur:Paris", text);
    final List<CheckError> fileErrors = analyze("Fichier:Paris.jpg", text);
    final List<CheckError> talkErrors = analyze("Discussion utilisateur:Paris", text);

    // THEN
    Assertions.assertThat(userErrors).hasSize(1);
    Assertions.assertThat(fileErrors).isEmpty();
    Assertions.assertThat(talkErrors).isEmpty();
  }

  @DisplayName("should not report other tags, or tags in comments and non wiki content")
  @Test
  void ignoreOtherTags() {
    // GIVEN
    final String text =
        "<big>a</big> <br> <!-- <b>a</b> --> <nowiki><b>a</b></nowiki> <pre><b>a</b></pre> <math><b>a</b></math> <source lang=\"x\"><b>a</b></source> <syntaxhighlight lang=\"x\"><b>a</b></syntaxhighlight>";

    // WHEN
    final List<CheckError> errors = analyze("Paris", text);

    // THEN
    Assertions.assertThat(errors).isEmpty();
  }

  private List<CheckError> analyze(final String title, final String text) {
    return algorithm.analyze(PageAnalysisTestFactory.createPageAnalysis(title, text));
  }

  private static CheckError error(
      final String text, final String found, final List<Replacement> replacements) {
    final int begin = text.lastIndexOf(found);
    return new CheckError(ALGORITHM, begin, begin + found.length(), replacements);
  }
}
