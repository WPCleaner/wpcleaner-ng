package org.wpcleaner.lib.check.algorithm.a0xx.a04x.a040;

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

@CheckSpringBootTest
class CheckAlgorithm040Test {

  private static final int ALGORITHM = 40;

  @Autowired private CheckAlgorithm040 algorithm;

  @DisplayName("should report each font tag separately, without replacement")
  @Test
  void reportTags() {
    // GIVEN
    final String text = "<font color=\"red\">a</font> <FONT>b";

    // WHEN
    final List<CheckError> errors = analyze("Paris", text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(
            error(text, "<font color=\"red\">"), error(text, "</font>"), error(text, "<FONT>"));
  }

  @DisplayName("should report tags in other subject namespaces, but not in talk namespaces")
  @Test
  void ignoreTalkNamespaces() {
    // GIVEN
    final String text = "<font color=\"red\">a</font> <FONT>b";

    // WHEN
    final List<CheckError> userErrors = analyze("Utilisateur:Paris", text);
    final List<CheckError> talkErrors = analyze("Discussion utilisateur:Paris", text);

    // THEN
    Assertions.assertThat(userErrors).hasSize(3);
    Assertions.assertThat(talkErrors).isEmpty();
  }

  @DisplayName("should not report other tags, or tags in comments and non wiki content")
  @Test
  void ignoreOtherTags() {
    // GIVEN
    final String text =
        "<span>a</span> <!-- <font>a</font> --> <nowiki><font>a</font></nowiki> <source lang=\"x\"><font>a</font></source> <syntaxhighlight lang=\"x\"><font>a</font></syntaxhighlight>";

    // WHEN
    final List<CheckError> errors = analyze("Paris", text);

    // THEN
    Assertions.assertThat(errors).isEmpty();
  }

  private List<CheckError> analyze(final String title, final String text) {
    return algorithm.analyze(PageAnalysisTestFactory.createPageAnalysis(title, text));
  }

  private static CheckError error(final String text, final String found) {
    final int begin = text.indexOf(found);
    return new CheckError(ALGORITHM, begin, begin + found.length(), List.of());
  }
}
