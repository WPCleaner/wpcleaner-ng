package org.wpcleaner.lib.check.algorithm.a0xx.a01x.a012;

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
class CheckAlgorithm012Test {

  private static final int ALGORITHM = 12;

  @Autowired private CheckAlgorithm012 algorithm;

  @DisplayName("should report each HTML list tag separately, without replacement")
  @Test
  void reportTags() {
    // GIVEN
    final String text = "<ul><li>a</li><LI class=\"x\">b</ul> <ol/>";

    // WHEN
    final List<CheckError> errors = analyze("Paris", text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(
            error(text, "<ul>"),
            error(text, "<li>"),
            error(text, "</li>"),
            error(text, "<LI class=\"x\">"),
            error(text, "</ul>"),
            error(text, "<ol/>"));
  }

  @DisplayName("should report tags in other subject namespaces, but not in talk namespaces")
  @Test
  void ignoreTalkNamespaces() {
    // GIVEN
    final String text = "<ul><li>a</li><LI class=\"x\">b</ul> <ol/>";

    // WHEN
    final List<CheckError> userErrors = analyze("Utilisateur:Paris", text);
    final List<CheckError> talkErrors = analyze("Discussion utilisateur:Paris", text);

    // THEN
    Assertions.assertThat(userErrors).hasSize(6);
    Assertions.assertThat(talkErrors).isEmpty();
  }

  @DisplayName("should not report other tags, or tags in comments and non wiki content")
  @Test
  void ignoreOtherTags() {
    // GIVEN
    final String text =
        "<span>a</span> <!-- <ul><li>a</li></ul> --> <nowiki><ul><li>a</li></ul></nowiki> <source lang=\"x\"><ul><li>a</li></ul></source> <syntaxhighlight lang=\"x\"><ul><li>a</li></ul></syntaxhighlight>";

    // WHEN
    final List<CheckError> errors = analyze("Paris", text);

    // THEN
    Assertions.assertThat(errors).isEmpty();
  }

  @DisplayName("should not report list items with a value")
  @Test
  void ignoreListItemsWithValue() {
    // GIVEN
    final String text = "<li value=\"3\">a</li> <li VALUE=4>b <ol value=\"5\">";

    // WHEN
    final List<CheckError> errors = analyze("Paris", text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(error(text, "</li>"), error(text, "<ol value=\"5\">"));
  }

  private List<CheckError> analyze(final String title, final String text) {
    return algorithm.analyze(PageAnalysisTestFactory.createPageAnalysis(title, text));
  }

  private static CheckError error(final String text, final String found) {
    final int begin = text.indexOf(found);
    return new CheckError(ALGORITHM, begin, begin + found.length(), List.of());
  }
}
