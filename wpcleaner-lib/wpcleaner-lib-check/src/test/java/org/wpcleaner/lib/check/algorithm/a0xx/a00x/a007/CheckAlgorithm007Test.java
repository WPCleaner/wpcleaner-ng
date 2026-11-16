package org.wpcleaner.lib.check.algorithm.a0xx.a00x.a007;

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
class CheckAlgorithm007Test {

  private static final int ALGORITHM = 7;

  @Autowired private CheckAlgorithm007 algorithm;

  @DisplayName("should not report a page without titles")
  @Test
  void ignorePageWithoutTitles() {
    Assertions.assertThat(analyze("a\nb")).isEmpty();
  }

  @DisplayName("should not report a page with a title of level 2")
  @Test
  void ignoreTitleOfLevelTwo() {
    Assertions.assertThat(analyze("=== a ===\nb\n== c ==\nd\n==== e ====")).isEmpty();
  }

  @DisplayName("should not report a page with a title of level 1")
  @Test
  void ignoreTitleOfLevelOne() {
    Assertions.assertThat(analyze("=== a ===\nb\n= c =\nd")).isEmpty();
  }

  @DisplayName("should fix automatically a single title of level 3")
  @Test
  void fixSingleTitle() {
    final List<CheckError> errors = analyze("a\n===b===\nc");

    Assertions.assertThat(errors)
        .containsExactly(
            new CheckError(ALGORITHM, 2, 9, List.of(new Replacement("== b ==", true))));
  }

  @DisplayName("should fix a single title of a deeper level")
  @Test
  void fixSingleDeeperTitle() {
    final List<CheckError> errors = analyze("===== b =====");

    Assertions.assertThat(errors)
        .containsExactly(
            new CheckError(ALGORITHM, 0, 13, List.of(new Replacement("== b ==", true))));
  }

  @DisplayName("should keep what follows the title out of the fix")
  @Test
  void keepTextAfterTitle() {
    final List<CheckError> errors = analyze("=== b === <!-- c -->\nd");

    Assertions.assertThat(errors)
        .containsExactly(
            new CheckError(ALGORITHM, 0, 9, List.of(new Replacement("== b ==", true))));
  }

  @DisplayName("should fix a single title without text")
  @Test
  void fixSingleEmptyTitle() {
    final List<CheckError> errors = analyze("=== ===");

    Assertions.assertThat(errors)
        .containsExactly(new CheckError(ALGORITHM, 0, 7, List.of(new Replacement("== ==", true))));
  }

  @DisplayName("should report only the first title without fix when there are several titles")
  @Test
  void reportFirstOfSeveralTitles() {
    final List<CheckError> errors = analyze("a\n=== b ===\nc\n==== d ====\ne\n=== f ===");

    Assertions.assertThat(errors).containsExactly(new CheckError(ALGORITHM, 2, 11, List.of()));
  }

  @DisplayName("should not fix automatically a title with more equal signs at the beginning")
  @Test
  void fixManuallyTitleWithLongerBeginning() {
    final List<CheckError> errors = analyze("==== b ===");

    Assertions.assertThat(errors)
        .containsExactly(
            new CheckError(ALGORITHM, 0, 10, List.of(new Replacement("== = b ==", false))));
  }

  @DisplayName("should not fix automatically a title with more equal signs at the end")
  @Test
  void fixManuallyTitleWithLongerEnd() {
    final List<CheckError> errors = analyze("=== b ====");

    Assertions.assertThat(errors)
        .containsExactly(
            new CheckError(ALGORITHM, 0, 10, List.of(new Replacement("== b = ==", false))));
  }

  @DisplayName("should not fix automatically a title made only of equal signs")
  @Test
  void fixManuallyTitleOfEqualSigns() {
    final List<CheckError> errors = analyze("=======");

    Assertions.assertThat(errors)
        .containsExactly(
            new CheckError(ALGORITHM, 0, 7, List.of(new Replacement("== = ==", false))));
  }

  @DisplayName("should not fix automatically a title inside a template")
  @Test
  void fixManuallyTitleInsideTemplate() {
    final List<CheckError> errors = analyze("{{a|\n=== b ===\n}}");

    Assertions.assertThat(errors)
        .containsExactly(
            new CheckError(ALGORITHM, 5, 14, List.of(new Replacement("== b ==", false))));
  }

  @DisplayName("should fix automatically a title next to a template")
  @Test
  void fixTitleOutsideTemplate() {
    final List<CheckError> errors = analyze("{{a|c}}\n=== b ===");

    Assertions.assertThat(errors)
        .containsExactly(
            new CheckError(ALGORITHM, 8, 17, List.of(new Replacement("== b ==", true))));
  }

  @DisplayName("should not fix automatically a title in a page with an include area")
  @Test
  void fixManuallyWithIncludeArea() {
    final List<CheckError> errors = analyze("=== b ===\n<noinclude>c</noinclude>");

    Assertions.assertThat(errors)
        .containsExactly(
            new CheckError(ALGORITHM, 0, 9, List.of(new Replacement("== b ==", false))));
  }

  @DisplayName("should report titles in any namespace")
  @Test
  void reportOtherNamespaces() {
    Assertions.assertThat(
            algorithm.analyze(
                PageAnalysisTestFactory.createPageAnalysis("Utilisateur:Paris", "=== b ===")))
        .hasSize(1);
  }

  private List<CheckError> analyze(final String text) {
    return algorithm.analyze(PageAnalysisTestFactory.createPageAnalysis("Paris", text));
  }
}
