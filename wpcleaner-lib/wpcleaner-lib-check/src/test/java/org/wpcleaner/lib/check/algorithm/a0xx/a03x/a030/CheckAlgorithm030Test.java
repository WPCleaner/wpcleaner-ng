package org.wpcleaner.lib.check.algorithm.a0xx.a03x.a030;

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
class CheckAlgorithm030Test {

  private static final int ALGORITHM = 30;

  @Autowired private CheckAlgorithm030 algorithm;

  @DisplayName("should report images without description, without suggesting a replacement")
  @Test
  void reportImageWithoutDescription() {
    // GIVEN
    final String text = "a [[File:A.jpg]] b [[Fichier:B.jpg|thumb|200px|droite]] c";

    // WHEN
    final List<CheckError> errors = analyze("Paris", text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(
            error(text, "[[File:A.jpg]]"), error(text, "[[Fichier:B.jpg|thumb|200px|droite]]"));
  }

  @DisplayName("should report images with a blank description or a blank alternate description")
  @Test
  void reportBlankDescriptions() {
    // GIVEN
    final String text = "a [[File:A.jpg|thumb| ]] b [[File:B.jpg|thumb|alt= ]]";

    // WHEN
    final List<CheckError> errors = analyze("Paris", text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(
            error(text, "[[File:A.jpg|thumb| ]]"), error(text, "[[File:B.jpg|thumb|alt= ]]"));
  }

  @DisplayName("should not report images with a description or an alternate description")
  @Test
  void ignoreImagesWithDescription() {
    // GIVEN
    final String text = "a [[File:A.jpg|thumb|A caption]] b [[File:B.jpg|thumb|alt=A text]]";

    // WHEN
    final List<CheckError> errors = analyze("Paris", text);

    // THEN
    Assertions.assertThat(errors).isEmpty();
  }

  @DisplayName("should report images only in the main namespace")
  @Test
  void ignoreOtherNamespaces() {
    // GIVEN
    final String text = "a [[File:A.jpg]]";

    // WHEN
    final List<CheckError> talkErrors = analyze("Discussion utilisateur:Paris", text);
    final List<CheckError> templateErrors = analyze("Modèle:Paris", text);
    final List<CheckError> categoryErrors = analyze("Catégorie:Paris", text);

    // THEN
    Assertions.assertThat(talkErrors).isEmpty();
    Assertions.assertThat(templateErrors).isEmpty();
    Assertions.assertThat(categoryErrors).isEmpty();
  }

  @DisplayName(
      "should not report links to files, images in galleries, comments or non wiki content")
  @Test
  void ignoreOtherFileUses() {
    // GIVEN
    final String text =
        "a [[:File:A.jpg]] <gallery>\nFile:B.jpg\n</gallery> <!-- [[File:C.jpg]] -->"
            + " <nowiki>[[File:D.jpg]]</nowiki>";

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
