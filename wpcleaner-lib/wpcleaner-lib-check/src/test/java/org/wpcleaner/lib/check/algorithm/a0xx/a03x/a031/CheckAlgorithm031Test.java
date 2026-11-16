package org.wpcleaner.lib.check.algorithm.a0xx.a03x.a031;

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
class CheckAlgorithm031Test {

  private static final int ALGORITHM = 31;

  @Autowired private CheckAlgorithm031 algorithm;

  @DisplayName("should report each HTML table tag separately, without replacement")
  @Test
  void reportTags() {
    // GIVEN
    final String text = "<table class=\"x\"><tr><th>a</th><td>b</TD></tr></table>";

    // WHEN
    final List<CheckError> errors = analyze("Paris", text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(
            error(text, "<table class=\"x\">"),
            error(text, "<tr>"),
            error(text, "<th>"),
            error(text, "</th>"),
            error(text, "<td>"),
            error(text, "</TD>"),
            error(text, "</tr>"),
            error(text, "</table>"));
  }

  @DisplayName("should report tags in other subject namespaces, but not in talk namespaces")
  @Test
  void ignoreTalkNamespaces() {
    // GIVEN
    final String text = "<table class=\"x\"><tr><th>a</th><td>b</TD></tr></table>";

    // WHEN
    final List<CheckError> userErrors = analyze("Utilisateur:Paris", text);
    final List<CheckError> talkErrors = analyze("Discussion utilisateur:Paris", text);

    // THEN
    Assertions.assertThat(userErrors).hasSize(8);
    Assertions.assertThat(talkErrors).isEmpty();
  }

  @DisplayName("should not report other tags, or tags in comments and non wiki content")
  @Test
  void ignoreOtherTags() {
    // GIVEN
    final String text =
        "<span>a</span> <!-- <table><tr><td>a</td></tr></table> --> <nowiki><table><tr><td>a</td></tr></table></nowiki> <source lang=\"x\"><table><tr><td>a</td></tr></table></source> <syntaxhighlight lang=\"x\"><table><tr><td>a</td></tr></table></syntaxhighlight>";

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
