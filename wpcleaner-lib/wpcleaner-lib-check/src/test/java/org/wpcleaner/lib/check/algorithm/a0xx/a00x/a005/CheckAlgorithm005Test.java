package org.wpcleaner.lib.check.algorithm.a0xx.a00x.a005;

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
class CheckAlgorithm005Test {

  private static final int ALGORITHM = 5;

  @Autowired private CheckAlgorithm005 algorithm;

  @DisplayName("should not report well formed comments")
  @Test
  void ignoreClosedComments() {
    Assertions.assertThat(analyze("a <!-- b --> c <!-- d --> e")).isEmpty();
  }

  @DisplayName("should offer to uncomment a comment never closed")
  @Test
  void uncommentUnclosedComment() {
    final List<CheckError> errors = analyze("a <!--  b c");

    Assertions.assertThat(errors)
        .containsExactly(new CheckError(ALGORITHM, 2, 8, List.of(new Replacement("", false))));
  }

  @DisplayName("should offer to fix a malformed end")
  @Test
  void fixMalformedEnd() {
    final List<CheckError> errors = analyze("a <!-- b -> c");

    Assertions.assertThat(errors)
        .containsExactly(
            new CheckError(ALGORITHM, 2, 11, List.of(new Replacement("<!-- b -->", false))));
  }

  @DisplayName("should ignore a malformed end located after the next comment start")
  @Test
  void ignoreMalformedEndAfterNextStart() {
    final String text = "<!-- a <!-- b -> c";

    final List<CheckError> errors = analyze(text);

    Assertions.assertThat(errors).hasSize(2);
    Assertions.assertThat(errors.getFirst().end()).isEqualTo(11);
  }

  @DisplayName("should offer replacements for a comment started again before being closed")
  @Test
  void reportNestedStart() {
    final String text = "<!-- a\nb <!-- c -->";

    final List<CheckError> errors = analyze(text);

    Assertions.assertThat(errors)
        .containsExactly(
            new CheckError(
                ALGORITHM,
                0,
                13,
                List.of(
                    new Replacement("<!-- a-->\nb <!--", false),
                    new Replacement("<!-- a\nb--> <!--", false),
                    new Replacement("<!-- a\nb ", false),
                    new Replacement("a\nb <!--", false))));
  }

  @DisplayName("should report each comment start not closed")
  @Test
  void reportEachUnclosedStart() {
    Assertions.assertThat(analyze("<!-- a <!-- b <!-- c -->")).hasSize(2);
  }

  @DisplayName("should not consider an empty start as closed")
  @Test
  void reportStartLookingLikeEnd() {
    Assertions.assertThat(analyze("a <!-->")).hasSize(1);
  }

  @DisplayName("should report comments in any namespace")
  @Test
  void reportOtherNamespaces() {
    Assertions.assertThat(
            algorithm.analyze(
                PageAnalysisTestFactory.createPageAnalysis("Utilisateur:Paris", "<!-- a")))
        .hasSize(1);
  }

  private List<CheckError> analyze(final String text) {
    return algorithm.analyze(PageAnalysisTestFactory.createPageAnalysis("Paris", text));
  }
}
