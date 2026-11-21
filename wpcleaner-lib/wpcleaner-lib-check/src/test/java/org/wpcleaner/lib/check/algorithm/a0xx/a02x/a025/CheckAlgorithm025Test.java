package org.wpcleaner.lib.check.algorithm.a0xx.a02x.a025;

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
class CheckAlgorithm025Test {

  private static final int ALGORITHM = 25;

  @Autowired private CheckAlgorithm025 algorithm;

  @DisplayName("should not report a correct heading hierarchy")
  @Test
  void ignoreCorrectHierarchy() {
    Assertions.assertThat(analyze("== a ==\n=== b ===\n==== c ====\n== d ==\n=== e ===")).isEmpty();
  }

  @DisplayName("should not report a first heading of a deep level")
  @Test
  void ignoreFirstHeading() {
    Assertions.assertThat(analyze("a\n==== b ====\nc")).isEmpty();
  }

  @DisplayName("should fix automatically a heading skipping a level")
  @Test
  void fixSkippedLevel() {
    final List<CheckError> errors = analyze("== a ==\n==== b ====\nc");

    Assertions.assertThat(errors)
        .containsExactly(
            new CheckError(ALGORITHM, 8, 19, List.of(new Replacement("=== b ===", true))));
  }

  @DisplayName("should fix to the level following the previous heading")
  @Test
  void fixSeveralSkippedLevels() {
    final List<CheckError> errors = analyze("== a ==\n===== b =====\n== c ==");

    Assertions.assertThat(errors)
        .containsExactly(
            new CheckError(ALGORITHM, 8, 21, List.of(new Replacement("=== b ===", true))));
  }

  @DisplayName("should fix a heading followed by a heading of the expected level")
  @Test
  void fixHeadingFollowedByExpectedLevel() {
    final List<CheckError> errors = analyze("== a ==\n==== b ====\n=== c ===");

    Assertions.assertThat(errors)
        .containsExactly(
            new CheckError(ALGORITHM, 8, 19, List.of(new Replacement("=== b ===", true))));
  }

  @DisplayName("should not fix a heading followed by sub headings")
  @Test
  void keepHeadingWithSubHeadings() {
    final List<CheckError> errors = analyze("== a ==\n==== b ====\n==== c ====");

    Assertions.assertThat(errors).containsExactly(new CheckError(ALGORITHM, 8, 19, List.of()));
  }

  @DisplayName("should report each heading skipping a level")
  @Test
  void reportSeveralHeadings() {
    final List<CheckError> errors = analyze("== a ==\n==== b ====\n== c ==\n==== d ====");

    Assertions.assertThat(errors)
        .containsExactly(
            new CheckError(ALGORITHM, 8, 19, List.of(new Replacement("=== b ===", true))),
            new CheckError(ALGORITHM, 28, 39, List.of(new Replacement("=== d ===", true))));
  }

  @DisplayName("should keep the text after the heading outside the fix")
  @Test
  void fixHeadingWithComment() {
    final List<CheckError> errors = analyze("== a ==\n==== b ==== <!-- c -->\nd");

    Assertions.assertThat(errors)
        .containsExactly(
            new CheckError(ALGORITHM, 8, 19, List.of(new Replacement("=== b ===", true))));
  }

  @DisplayName("should not fix automatically a heading inside a template")
  @Test
  void fixManuallyHeadingInsideTemplate() {
    final List<CheckError> errors = analyze("== a ==\n{{b|x=\n==== c ====\n}}");

    Assertions.assertThat(errors)
        .containsExactly(
            new CheckError(ALGORITHM, 15, 26, List.of(new Replacement("=== c ===", false))));
  }

  @DisplayName("should not fix automatically a heading with more equal signs at the end")
  @Test
  void fixManuallyIncoherentHeading() {
    final List<CheckError> errors = analyze("== a ==\n==== b =====");

    Assertions.assertThat(errors)
        .containsExactly(
            new CheckError(ALGORITHM, 8, 20, List.of(new Replacement("=== b = ===", false))));
  }

  @DisplayName("should not fix automatically a heading in a page with an include area")
  @Test
  void fixManuallyWithIncludeArea() {
    final List<CheckError> errors = analyze("== a ==\n==== b ====\n<noinclude>c</noinclude>");

    Assertions.assertThat(errors)
        .containsExactly(
            new CheckError(ALGORITHM, 8, 19, List.of(new Replacement("=== b ===", false))));
  }

  @DisplayName("should report headings in other namespaces than talk pages")
  @Test
  void reportOtherNamespaces() {
    Assertions.assertThat(
            algorithm.analyze(
                PageAnalysisTestFactory.createPageAnalysis(
                    "Utilisateur:Paris", "== a ==\n==== b ====")))
        .hasSize(1);
  }

  @DisplayName("should not report headings in talk pages")
  @Test
  void ignoreTalkPages() {
    Assertions.assertThat(
            algorithm.analyze(
                PageAnalysisTestFactory.createPageAnalysis(
                    "Discussion utilisateur:Paris", "== a ==\n==== b ====")))
        .isEmpty();
  }

  private List<CheckError> analyze(final String text) {
    return algorithm.analyze(PageAnalysisTestFactory.createPageAnalysis("Paris", text));
  }
}
