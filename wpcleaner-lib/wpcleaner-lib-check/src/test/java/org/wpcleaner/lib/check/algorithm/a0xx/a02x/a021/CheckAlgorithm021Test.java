package org.wpcleaner.lib.check.algorithm.a0xx.a02x.a021;

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
class CheckAlgorithm021Test {

  private static final int ALGORITHM = 21;

  @Autowired private CheckAlgorithm021 algorithm;

  @DisplayName("should offer the local namespace names for an English category")
  @Test
  void englishCategory() {
    // GIVEN
    final String category = "[[Category:Paris]]";
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
                List.of(
                    new Replacement("[[Catégorie:Paris]]", false),
                    new Replacement("[[Catégories:Paris]]", false))));
  }

  @DisplayName("should keep the sort key and remove spaces around namespace and name")
  @Test
  void keepSortKey() {
    // GIVEN
    final String text = "[[ category : Élysée | Élysée ]] [[CATEGORY:Lyon|]]";

    // WHEN
    final List<CheckError> errors = analyze(text);

    // THEN
    Assertions.assertThat(errors)
        .extracting(error -> error.replacements().getFirst())
        .containsExactly(
            new Replacement("[[Catégorie:Élysée| Élysée ]]", false),
            new Replacement("[[Catégorie:Lyon|]]", false));
  }

  @DisplayName("should not report local categories, links to categories and invalid names")
  @Test
  void ignoreOtherLinks() {
    // GIVEN
    final String text =
        "[[Catégorie:Paris]] [[Catégories:Lyon]] [[:Category:Paris]] [[Category:]]"
            + " [[Category:A|B]]";

    // WHEN
    final List<CheckError> errors = analyze(text);

    // THEN
    Assertions.assertThat(errors)
        .extracting(CheckError::begin)
        .containsExactly(text.indexOf("[[Category:A"));
  }

  @DisplayName("should not report categories when the local namespace is English")
  @Test
  void ignoreEnglishWiki() {
    // GIVEN
    final String text = "[[Category:Paris]]";

    // WHEN
    final List<CheckError> errors =
        algorithm.analyze(
            PageAnalysisTestFactory.createPageAnalysis(PageAnalysisTestFactory.ENGLISH_WIKI, text));

    // THEN
    Assertions.assertThat(errors).isEmpty();
  }

  private List<CheckError> analyze(final String text) {
    return algorithm.analyze(PageAnalysisTestFactory.createPageAnalysis(text));
  }
}
