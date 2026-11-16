package org.wpcleaner.lib.check.algorithm.a0xx.a08x.a087;

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
class CheckAlgorithm087Test {

  private static final int ALGORITHM = 87;

  @Autowired private CheckAlgorithm087 algorithm;

  @DisplayName(
      "should suggest the character or an escaped ampersand, without automatic replacement")
  @Test
  void replaceEntityWithoutSemicolon() {
    // GIVEN
    final String text = "caf&eacute du coin";

    // WHEN
    final List<CheckError> errors = analyze(text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(
            new CheckError(
                ALGORITHM,
                3,
                3 + "&eacute".length(),
                List.of(new Replacement("é", false), new Replacement("&amp;eacute", false))));
  }

  @DisplayName("should include a semicolon separated by spaces and suggest to keep it")
  @Test
  void includeSeparatedSemicolon() {
    // GIVEN
    final String text = "caf&eacute  ; du coin";

    // WHEN
    final List<CheckError> errors = analyze(text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(
            new CheckError(
                ALGORITHM,
                3,
                text.indexOf(';') + 1,
                List.of(
                    new Replacement("é", false),
                    new Replacement("é  ;", false),
                    new Replacement("&amp;eacute  ;", false))));
  }

  @DisplayName("should not include spaces not followed by a semicolon")
  @Test
  void excludeSpacesWithoutSemicolon() {
    // GIVEN
    final String withText = "&eacute  x;";
    final String atEnd = "&eacute  ";

    // WHEN
    final List<CheckError> errorsWithText = analyze(withText);
    final List<CheckError> errorsAtEnd = analyze(atEnd);

    // THEN
    Assertions.assertThat(errorsWithText)
        .extracting(CheckError::end)
        .containsExactly("&eacute".length());
    Assertions.assertThat(errorsAtEnd)
        .extracting(CheckError::end)
        .containsExactly("&eacute".length());
  }

  @DisplayName("should report the longest entity, even followed by letters or at the end")
  @Test
  void reportLongestEntity() {
    // GIVEN
    final String text = "m&sup2 &notx &sup";

    // WHEN
    final List<CheckError> errors = analyze(text);

    // THEN
    Assertions.assertThat(errors)
        .extracting(
            CheckError::begin, CheckError::end, error -> error.replacements().getFirst().text())
        .containsExactly(
            Assertions.tuple(1, 6, "²"),
            Assertions.tuple(7, 11, "¬"),
            Assertions.tuple(13, text.length(), "⊃"));
  }

  @DisplayName("should not report entities with a semicolon, kept as entities or unknown")
  @Test
  void ignoreCorrectEntities() {
    // GIVEN
    final String text =
        "&eacute; &sup2; &notin; &amp &nbsp &lt &gt &unknown AT&T & <!-- &eacute -->"
            + " <source>&eacute</source> [http://example.org/?a&eacute x]";

    // WHEN
    final List<CheckError> errors = analyze(text);

    // THEN
    Assertions.assertThat(errors).isEmpty();
  }

  private List<CheckError> analyze(final String text) {
    return algorithm.analyze(PageAnalysisTestFactory.createPageAnalysis("Lyon", text));
  }
}
