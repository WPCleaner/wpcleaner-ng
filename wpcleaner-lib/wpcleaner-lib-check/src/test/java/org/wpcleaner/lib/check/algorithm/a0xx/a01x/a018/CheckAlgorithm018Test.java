package org.wpcleaner.lib.check.algorithm.a0xx.a01x.a018;

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
class CheckAlgorithm018Test {

  private static final int ALGORITHM = 18;

  @Autowired private CheckAlgorithm018 algorithm;

  @DisplayName("should capitalize the first letter of a category name")
  @Test
  void lowerCaseName() {
    // GIVEN
    final String category = "[[Catégorie:paris]]";
    final String text = "Texte " + category + " fin";

    // WHEN
    final List<CheckError> errors = analyze(text);

    // THEN
    final int begin = text.indexOf(category);
    Assertions.assertThat(errors)
        .containsExactly(
            new CheckError(
                ALGORITHM,
                begin,
                begin + category.length(),
                List.of(new Replacement("[[Catégorie:Paris]]", true))));
  }

  @DisplayName("should capitalize the namespace and offer the local namespace name manually")
  @Test
  void lowerCaseNamespace() {
    // GIVEN
    final String text = "[[category:Paris]]";

    // WHEN
    final List<CheckError> errors = analyze(text);

    // THEN
    Assertions.assertThat(errors)
        .flatExtracting(CheckError::replacements)
        .containsExactly(
            new Replacement("[[Category:Paris]]", true),
            new Replacement("[[Catégorie:Paris]]", false));
  }

  @DisplayName("should keep the sort key and remove spaces around namespace and name")
  @Test
  void keepSortKey() {
    // GIVEN
    final String text = "[[ catégorie : élysée | Élysée ]] [[catégorie:Lyon|]]";

    // WHEN
    final List<CheckError> errors = analyze(text);

    // THEN
    Assertions.assertThat(errors)
        .flatExtracting(CheckError::replacements)
        .containsExactly(
            new Replacement("[[Catégorie:Élysée| Élysée ]]", true),
            new Replacement("[[Catégorie:Lyon|]]", true));
  }

  @DisplayName("should not report categories starting with a capital letter or a non-letter")
  @Test
  void ignoreCapitalLetters() {
    // GIVEN
    final String text =
        "[[Catégorie:Paris|paris]] [[CATÉGORIE:Lyon]] [[Catégorie:1900]]"
            + " [[Catégorie:{{lc:X}}]] [[:catégorie:paris]] [[paris]] [[catégorie:]]";

    // WHEN
    final List<CheckError> errors = analyze(text);

    // THEN
    Assertions.assertThat(errors).isEmpty();
  }

  @DisplayName("should not report a lower case name when the category namespace is case-sensitive")
  @Test
  void ignoreCaseSensitiveNamespace() {
    // GIVEN
    final String text = "[[Catégorie:paris]] [[catégorie:Paris]]";

    // WHEN
    final List<CheckError> errors =
        algorithm.analyze(
            PageAnalysisTestFactory.createPageAnalysis(
                PageAnalysisTestFactory.CASE_SENSITIVE_WIKI, text));

    // THEN
    Assertions.assertThat(errors)
        .extracting(CheckError::begin)
        .containsExactly(text.indexOf("[[catégorie"));
  }

  @DisplayName("should report each category with a lower case first letter")
  @Test
  void severalCategories() {
    // GIVEN
    final String text = "[[catégorie:paris]] [[Catégorie:Lyon]] [[Catégorie:𝔞rt]]";

    // WHEN
    final List<CheckError> errors = analyze(text);

    // THEN
    Assertions.assertThat(errors)
        .flatExtracting(CheckError::replacements)
        .containsExactly(new Replacement("[[Catégorie:Paris]]", true));
  }

  private List<CheckError> analyze(final String text) {
    return algorithm.analyze(PageAnalysisTestFactory.createPageAnalysis(text));
  }
}
