package org.wpcleaner.lib.check.algorithm.a0xx.a05x.a050;

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
class CheckAlgorithm050Test {

  private static final int ALGORITHM = 50;

  @Autowired private CheckAlgorithm050 algorithm;

  @DisplayName("should automatically replace en dash and em dash entities")
  @Test
  void replaceDashes() {
    // GIVEN
    final String text = "1900&ndash;1910 &mdash; texte";

    // WHEN
    final List<CheckError> errors = analyze(text);

    // THEN
    final int emDash = text.indexOf("&mdash;");
    Assertions.assertThat(errors)
        .containsExactly(
            new CheckError(
                ALGORITHM, 4, 4 + "&ndash;".length(), List.of(new Replacement("–", true))),
            new CheckError(
                ALGORITHM,
                emDash,
                emDash + "&mdash;".length(),
                List.of(new Replacement("—", true))));
  }

  @DisplayName("should not report other entities, nor dashes without semicolon or in comments")
  @Test
  void ignoreOtherEntities() {
    // GIVEN
    final String text = "&minus; &ndash &mdash &eacute; <!-- &ndash; -->";

    // WHEN
    final List<CheckError> errors = analyze(text);

    // THEN
    Assertions.assertThat(errors).isEmpty();
  }

  private List<CheckError> analyze(final String text) {
    return algorithm.analyze(PageAnalysisTestFactory.createPageAnalysis("Lyon", text));
  }
}
