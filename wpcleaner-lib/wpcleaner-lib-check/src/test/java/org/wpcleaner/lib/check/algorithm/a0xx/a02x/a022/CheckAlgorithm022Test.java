package org.wpcleaner.lib.check.algorithm.a0xx.a02x.a022;

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
class CheckAlgorithm022Test {

  private static final int ALGORITHM = 22;

  @Autowired private CheckAlgorithm022 algorithm;

  @DisplayName("should remove spaces around the namespace and the name")
  @Test
  void spacesAroundNamespaceAndName() {
    // GIVEN
    final String category = "[[ Catégorie : Paris ]]";
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

  @DisplayName("should report each kind of space in the namespace or the name")
  @Test
  void eachSpace() {
    // GIVEN
    final String text =
        "[[ Catégorie:A]] [[Catégorie :B]] [[Catégorie: C]] [[Catégorie:D ]] [[Catégorie:E\t|e]]";

    // WHEN
    final List<CheckError> errors = analyze(text);

    // THEN
    Assertions.assertThat(errors)
        .flatExtracting(CheckError::replacements)
        .containsExactly(
            new Replacement("[[Catégorie:A]]", true),
            new Replacement("[[Catégorie:B]]", true),
            new Replacement("[[Catégorie:C]]", true),
            new Replacement("[[Catégorie:D]]", true),
            new Replacement("[[Catégorie:E|e]]", true));
  }

  @DisplayName("should remove trailing spaces of the sort key and keep leading spaces")
  @Test
  void trailingSpacesInSortKey() {
    // GIVEN
    final String text = "[[Catégorie:Paris| Paris ]] [[Catégorie:Lyon|Lyon\t]]";

    // WHEN
    final List<CheckError> errors = analyze(text);

    // THEN
    Assertions.assertThat(errors)
        .flatExtracting(CheckError::replacements)
        .containsExactly(
            new Replacement("[[Catégorie:Paris| Paris]]", true),
            new Replacement("[[Catégorie:Lyon|Lyon]]", true));
  }

  @DisplayName("should keep a sort key made only of spaces")
  @Test
  void blankSortKey() {
    // GIVEN
    final String text =
        "[[Catégorie:Paris| ]] [[Catégorie:Lyon|  ]] [[Catégorie: Nice | ]] [[Catégorie:Caen|\t]]";

    // WHEN
    final List<CheckError> errors = analyze(text);

    // THEN
    Assertions.assertThat(errors)
        .flatExtracting(CheckError::replacements)
        .containsExactly(new Replacement("[[Catégorie:Nice| ]]", true));
  }

  @DisplayName("should keep the namespace as written and offer the local namespace manually")
  @Test
  void localNamespace() {
    // GIVEN
    final String text = "[[Category: paris|Paris ]]";

    // WHEN
    final List<CheckError> errors = analyze(text);

    // THEN
    Assertions.assertThat(errors)
        .flatExtracting(CheckError::replacements)
        .containsExactly(
            new Replacement("[[Category:paris|Paris]]", true),
            new Replacement("[[Catégorie:paris|Paris]]", false));
  }

  @DisplayName("should not report categories without spaces to remove")
  @Test
  void ignoreCategoriesWithoutSpaces() {
    // GIVEN
    final String text =
        "[[Catégorie:Paris]] [[Catégorie:Paris| Paris]] [[Catégorie:Lyon|]]"
            + " [[Catégorie:Élysée Palace]] [[:Catégorie: Paris ]] [[ Paris ]] [[Catégorie: ]]";

    // WHEN
    final List<CheckError> errors = analyze(text);

    // THEN
    Assertions.assertThat(errors).isEmpty();
  }

  @DisplayName("should not report categories containing a comment")
  @Test
  void ignoreComments() {
    // GIVEN
    final String text = "[[Catégorie: Paris<!-- ville -->]] [[Catégorie:Lyon| <!-- x --> ]]";

    // WHEN
    final List<CheckError> errors = analyze(text);

    // THEN
    Assertions.assertThat(errors).isEmpty();
  }

  @DisplayName("should report categories next to a comment")
  @Test
  void commentOutside() {
    // GIVEN
    final String text = "<!-- avant -->[[Catégorie: Paris]]<!-- après -->";

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
