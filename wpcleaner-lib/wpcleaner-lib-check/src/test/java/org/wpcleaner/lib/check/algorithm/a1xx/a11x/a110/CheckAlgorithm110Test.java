package org.wpcleaner.lib.check.algorithm.a1xx.a11x.a110;

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
class CheckAlgorithm110Test {

  private static final int ALGORITHM = 110;

  @Autowired private CheckAlgorithm110 algorithm;

  @DisplayName("should report complete include tags as one error, without replacement")
  @Test
  void reportCompleteTags() {
    // GIVEN
    final String text =
        "<noinclude>a</noinclude> <includeonly>b</includeonly> <ONLYINCLUDE>c</onlyinclude>";

    // WHEN
    final List<CheckError> errors = analyze("Paris", text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(
            error(text, "<noinclude>a</noinclude>"),
            error(text, "<includeonly>b</includeonly>"),
            error(text, "<ONLYINCLUDE>c</onlyinclude>"));
  }

  @DisplayName("should report separately include tags that are not complete")
  @Test
  void reportIncompleteTags() {
    // GIVEN
    final String text = "a</includeonly> <noinclude/> <onlyinclude>b";

    // WHEN
    final List<CheckError> errors = analyze("Paris", text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(
            error(text, "</includeonly>"),
            error(text, "<noinclude/>"),
            error(text, "<onlyinclude>"));
  }

  @DisplayName("should report tags in other subject namespaces, but not in talk namespaces")
  @Test
  void ignoreTalkNamespaces() {
    // GIVEN
    final String text = "<noinclude>a</noinclude>";

    // WHEN
    final List<CheckError> templateErrors = analyze("Modèle:Paris", text);
    final List<CheckError> talkErrors = analyze("Discussion utilisateur:Paris", text);

    // THEN
    Assertions.assertThat(templateErrors).hasSize(1);
    Assertions.assertThat(talkErrors).isEmpty();
  }

  @DisplayName("should not report other tags, or tags in comments and non wiki content")
  @Test
  void ignoreOtherTags() {
    // GIVEN
    final String text =
        "<span>a</span> <!-- <noinclude>a</noinclude> --> <nowiki><noinclude>a</noinclude></nowiki>"
            + " <source lang=\"x\"><includeonly>a</includeonly></source>"
            + " <syntaxhighlight lang=\"x\"><onlyinclude>a</onlyinclude></syntaxhighlight>";

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
