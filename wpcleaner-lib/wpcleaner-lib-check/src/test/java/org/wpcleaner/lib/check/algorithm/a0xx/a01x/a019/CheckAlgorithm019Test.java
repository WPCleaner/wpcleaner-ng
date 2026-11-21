package org.wpcleaner.lib.check.algorithm.a0xx.a01x.a019;

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
class CheckAlgorithm019Test {

  private static final int ALGORITHM = 19;

  @Autowired private CheckAlgorithm019 algorithm;

  @DisplayName("should not report a page without titles of level 1")
  @Test
  void ignoreTitlesOfOtherLevels() {
    Assertions.assertThat(analyze("a\n== b ==\nc\n=== d ===\n==")).isEmpty();
  }

  @DisplayName("should fix automatically a single title of level 1")
  @Test
  void fixSingleTitle() {
    final List<CheckError> errors = analyze("a\n=b=\nc");

    Assertions.assertThat(errors)
        .containsExactly(
            new CheckError(ALGORITHM, 2, 5, List.of(new Replacement("== b ==", true))));
  }

  @DisplayName("should fix automatically the last title when it is of level 1")
  @Test
  void fixLastTitle() {
    final List<CheckError> errors = analyze("== a ==\nb\n= c = <!-- d -->\ne");

    Assertions.assertThat(errors)
        .containsExactly(
            new CheckError(ALGORITHM, 10, 15, List.of(new Replacement("== c ==", true))));
  }

  @DisplayName("should delete automatically a first title equal to the page title")
  @Test
  void deletePageTitle() {
    final List<CheckError> errors = analyze("= paris =\na\n== b ==\nc");

    Assertions.assertThat(errors)
        .containsExactly(new CheckError(ALGORITHM, 0, 9, List.of(new Replacement("", true))));
  }

  @DisplayName("should not delete a first title different from the page title")
  @Test
  void keepOtherFirstTitle() {
    final List<CheckError> errors = analyze("= Lyon =\na\n== b ==\nc");

    Assertions.assertThat(errors).containsExactly(new CheckError(ALGORITHM, 0, 8, List.of()));
  }

  @DisplayName("should not delete a title equal to the page title when it is not the first one")
  @Test
  void keepPageTitleAfterOtherTitle() {
    final List<CheckError> errors = analyze("== a ==\n= Paris =\nb\n== c ==");

    Assertions.assertThat(errors).containsExactly(new CheckError(ALGORITHM, 8, 17, List.of()));
  }

  @DisplayName("should report every title of level 1 but fix only the first one")
  @Test
  void reportSeveralTitles() {
    final List<CheckError> errors = analyze("= Paris =\na\n= b =\nc\n= d =");

    Assertions.assertThat(errors)
        .containsExactly(
            new CheckError(ALGORITHM, 0, 9, List.of(new Replacement("", true))),
            new CheckError(ALGORITHM, 12, 17, List.of()),
            new CheckError(ALGORITHM, 20, 25, List.of()));
  }

  @DisplayName("should not fix automatically a title inside a template")
  @Test
  void fixManuallyTitleInsideTemplate() {
    final List<CheckError> errors = analyze("{{a|x=\n= b =\n}}");

    Assertions.assertThat(errors)
        .containsExactly(
            new CheckError(ALGORITHM, 7, 12, List.of(new Replacement("== b ==", false))));
  }

  @DisplayName("should not delete automatically the page title inside a template")
  @Test
  void deleteManuallyPageTitleInsideTemplate() {
    final List<CheckError> errors = analyze("{{a|x=\n= Paris =\n}}\n== b ==");

    Assertions.assertThat(errors)
        .containsExactly(new CheckError(ALGORITHM, 7, 16, List.of(new Replacement("", false))));
  }

  @DisplayName("should not fix automatically a title with more equal signs at the end")
  @Test
  void fixManuallyIncoherentTitle() {
    final List<CheckError> errors = analyze("= b ==");

    Assertions.assertThat(errors)
        .containsExactly(
            new CheckError(ALGORITHM, 0, 6, List.of(new Replacement("== b = ==", false))));
  }

  @DisplayName("should not fix automatically a title in a page with an include area")
  @Test
  void fixManuallyWithIncludeArea() {
    final List<CheckError> errors = analyze("= b =\n<noinclude>c</noinclude>");

    Assertions.assertThat(errors)
        .containsExactly(
            new CheckError(ALGORITHM, 0, 5, List.of(new Replacement("== b ==", false))));
  }

  @DisplayName("should report titles in other namespaces than talk pages")
  @Test
  void reportOtherNamespaces() {
    Assertions.assertThat(
            algorithm.analyze(
                PageAnalysisTestFactory.createPageAnalysis("Utilisateur:Paris", "= b =")))
        .hasSize(1);
  }

  @DisplayName("should not report titles in talk pages")
  @Test
  void ignoreTalkPages() {
    Assertions.assertThat(
            algorithm.analyze(
                PageAnalysisTestFactory.createPageAnalysis(
                    "Discussion utilisateur:Paris", "= b =")))
        .isEmpty();
  }

  private List<CheckError> analyze(final String text) {
    return algorithm.analyze(PageAnalysisTestFactory.createPageAnalysis("Paris", text));
  }
}
