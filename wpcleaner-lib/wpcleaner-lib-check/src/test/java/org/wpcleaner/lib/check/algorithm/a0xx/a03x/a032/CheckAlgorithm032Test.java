package org.wpcleaner.lib.check.algorithm.a0xx.a03x.a032;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.wpcleaner.api.analysis.PageAnalysisTestFactory;
import org.wpcleaner.lib.check.CheckError;
import org.wpcleaner.lib.check.CheckSpringBootTest;
import org.wpcleaner.lib.check.Replacement;

@CheckSpringBootTest
class CheckAlgorithm032Test {

  private static final int ALGORITHM = 32;

  @Autowired private CheckAlgorithm032 algorithm;

  @DisplayName("should suggest each text and a template when the texts differ")
  @Test
  void differentTexts() {
    // GIVEN
    final String link = "[[Paris|Ville|Capitale]]";
    final String text = "Texte " + link + " fin";

    // WHEN
    final List<CheckError> errors = analyze(text);

    // THEN
    final int begin = text.indexOf(link);
    Assertions.assertThat(errors)
        .containsExactly(
            new CheckError(
                ALGORITHM,
                begin,
                begin + link.length(),
                List.of(
                    new Replacement("[[Paris|Ville]]", false),
                    new Replacement("[[Paris|Capitale]]", false),
                    new Replacement("{{Paris|Ville|Capitale}}", false))));
  }

  @DisplayName("should fix automatically when only one text is not empty")
  @Test
  void singleText() {
    // GIVEN
    final String text = "[[Paris|Ville|]] [[Lyon||Ville]] [[Nice| |Ville]]";

    // WHEN
    final List<CheckError> errors = analyze(text);

    // THEN
    Assertions.assertThat(errors)
        .extracting(error -> error.replacements().getFirst())
        .containsExactly(
            new Replacement("[[Paris|Ville]]", true),
            new Replacement("[[Lyon|Ville]]", true),
            new Replacement("[[Nice|Ville]]", true));
  }

  @DisplayName("should fix automatically when the texts are identical")
  @Test
  void identicalTexts() {
    // GIVEN
    final String text = "[[Paris|Ville|Ville]]";

    // WHEN
    final List<CheckError> errors = analyze(text);

    // THEN
    Assertions.assertThat(errors)
        .flatExtracting(CheckError::replacements)
        .containsExactly(
            new Replacement("[[Paris|Ville]]", true),
            new Replacement("{{Paris|Ville|Ville}}", false));
  }

  @DisplayName("should keep the anchor and the leading colon")
  @Test
  void anchorAndColon() {
    // GIVEN
    final String text = "[[:Paris#Histoire|Histoire|]] [[#Section|Section|]]";

    // WHEN
    final List<CheckError> errors = analyze(text);

    // THEN
    Assertions.assertThat(errors)
        .extracting(error -> error.replacements().getFirst())
        .containsExactly(
            new Replacement("[[:Paris#Histoire|Histoire]]", true),
            new Replacement("[[#Section|Section]]", true));
  }

  @DisplayName("should report a link to a file with a leading colon")
  @Test
  void fileLinkWithColon() {
    // GIVEN
    final String text = "[[:Fichier:A.jpg|Image|]]";

    // WHEN
    final List<CheckError> errors = analyze(text);

    // THEN
    Assertions.assertThat(errors)
        .extracting(error -> error.replacements().getFirst())
        .containsExactly(new Replacement("[[:Fichier:A.jpg|Image]]", true));
  }

  @DisplayName("should ignore pipes inside nested elements, comments and non-wiki tags")
  @ParameterizedTest
  @ValueSource(
      strings = {
        "[[Paris]]",
        "[[Paris|Ville]]",
        "[[Paris|{{lang|fr|Ville}}]]",
        "[[Paris|{{#if:a|b|c}}]]",
        "[[Paris|{{{1|Ville}}}]]",
        "[[Paris|Ville<!-- a|b -->]]",
        "[[Paris|<nowiki>a|b</nowiki>]]",
        "[[Paris|<span style=\"a|b\">Ville</span>]]",
        "[[Fichier:A.jpg|vignette|Légende]]",
        "[[Catégorie:Paris|a|b]]",
        "[[en:Paris|a|b]]",
      })
  void ignoredPipes(final String text) {
    // WHEN
    final List<CheckError> errors = analyze(text);

    // THEN
    Assertions.assertThat(errors).isEmpty();
  }

  @DisplayName("should report a pipe outside nested elements")
  @Test
  void pipeOutsideNestedElement() {
    // GIVEN
    final String text = "[[Paris|{{lang|fr|Ville}}|Capitale]]";

    // WHEN
    final List<CheckError> errors = analyze(text);

    // THEN
    Assertions.assertThat(errors)
        .flatExtracting(CheckError::replacements)
        .containsExactly(
            new Replacement("[[Paris|{{lang|fr|Ville}}]]", false),
            new Replacement("[[Paris|Capitale]]", false),
            new Replacement("{{Paris|{{lang|fr|Ville}}|Capitale}}", false));
  }

  @DisplayName("should report a link inside a template")
  @Test
  void linkInsideTemplate() {
    // GIVEN
    final String text = "{{Modèle|[[Paris|Ville|]]}}";

    // WHEN
    final List<CheckError> errors = analyze(text);

    // THEN
    Assertions.assertThat(errors)
        .extracting(error -> error.replacements().getFirst())
        .containsExactly(new Replacement("[[Paris|Ville]]", true));
  }

  @DisplayName("should not fix automatically a link with an empty target")
  @Test
  void emptyTarget() {
    // GIVEN
    final String text = "[[ |Paris|Ville]]";

    // WHEN
    final List<CheckError> errors = analyze(text);

    // THEN
    Assertions.assertThat(errors)
        .flatExtracting(CheckError::replacements)
        .containsExactly(
            new Replacement("[[Paris|Ville]]", false),
            new Replacement("[[|Paris]]", false),
            new Replacement("[[|Ville]]", false),
            new Replacement("{{ |Paris|Ville}}", false));
  }

  @DisplayName("should not fix automatically a link with an empty target and a single text")
  @Test
  void emptyTargetSingleText() {
    // GIVEN
    final String text = "[[ |Paris|]]";

    // WHEN
    final List<CheckError> errors = analyze(text);

    // THEN
    Assertions.assertThat(errors)
        .flatExtracting(CheckError::replacements)
        .containsExactly(
            new Replacement("[[Paris|]]", false),
            new Replacement("[[|Paris]]", false),
            new Replacement("{{ |Paris|}}", false));
  }

  private List<CheckError> analyze(final String text) {
    return algorithm.analyze(PageAnalysisTestFactory.createPageAnalysis(text));
  }
}
