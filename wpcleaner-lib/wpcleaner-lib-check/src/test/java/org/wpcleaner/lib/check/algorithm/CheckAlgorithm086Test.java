package org.wpcleaner.lib.check.algorithm;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Arrays;
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
class CheckAlgorithm086Test {

  private static final int ALGORITHM = 86;

  @Autowired private CheckAlgorithm086 algorithm;

  @DisplayName("should suggest a single bracket for external links with two brackets")
  @Test
  void fixDoubleBrackets() {
    // GIVEN
    final String text =
        "[[https://example.org Ex]] [[https://example.org]] [[https://example.org Ex]";

    // WHEN
    final List<CheckError> errors = analyze(text);

    // THEN
    final int second = text.indexOf("[[", 1);
    final int third = text.lastIndexOf("[[");
    Assertions.assertThat(errors)
        .containsExactly(
            new CheckError(
                ALGORITHM,
                0,
                second - 1,
                List.of(new Replacement("[https://example.org Ex]", false))),
            new CheckError(
                ALGORITHM,
                second,
                third - 1,
                List.of(new Replacement("[https://example.org]", false))),
            new CheckError(
                ALGORITHM,
                third,
                text.length(),
                List.of(new Replacement("[https://example.org Ex]", false))));
  }

  @DisplayName("should replace the first pipe by a space for links written like internal links")
  @Test
  void fixPipe() {
    // GIVEN
    final String text = "[[https://example.org|Ex|Ex2]]";

    // WHEN
    final List<CheckError> errors = analyze(text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(
            new CheckError(
                ALGORITHM,
                0,
                text.length(),
                List.of(
                    new Replacement("[https://example.org Ex|Ex2]", false),
                    new Replacement("[https://example.org|Ex|Ex2]", false))));
  }

  @DisplayName("should suggest an internal link for a URL of the wiki")
  @Test
  void fixArticleUrls() {
    // GIVEN
    final String image =
        "[[//fr.wikipedia.org/wiki/Fichier:Flag_of_Spain.svg|20x20px|Drapeau de l'Espagne]]";
    final String withComment =
        "[<!-- c -->[https://fr.wikipedia.org/wiki/Tour_Eiffel Tour Eiffel]]";
    final String withAttributes = "[[https://fr.wikipedia.org/wiki/Lyon?action=edit|Lyon]]";
    final String withText = "[[https://fr.wikipedia.org/wiki/Paris la capitale]]";
    final String text = String.join(" ", image, withComment, withAttributes, withText);

    // WHEN
    final List<CheckError> errors = analyze(text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(
            error(
                text,
                image,
                "[[Fichier:Flag of Spain.svg|20x20px|Drapeau de l'Espagne]]",
                "[//fr.wikipedia.org/wiki/Fichier:Flag_of_Spain.svg 20x20px|Drapeau de l'Espagne]",
                "[//fr.wikipedia.org/wiki/Fichier:Flag_of_Spain.svg|20x20px|Drapeau de l'Espagne]"),
            error(
                text,
                withComment,
                "<!-- c -->[[Tour Eiffel]]",
                "<!-- c -->[https://fr.wikipedia.org/wiki/Tour_Eiffel Tour Eiffel]"),
            error(
                text,
                withAttributes,
                "[https://fr.wikipedia.org/wiki/Lyon?action=edit Lyon]",
                "[https://fr.wikipedia.org/wiki/Lyon?action=edit|Lyon]"),
            error(
                text,
                withText,
                "[[Paris|la capitale]]",
                "[https://fr.wikipedia.org/wiki/Paris la capitale]"));
  }

  @DisplayName("should not report external links with one bracket or none")
  @Test
  void ignoreOtherLinks() {
    // GIVEN
    final String text = "[https://example.org Ex] x[https://example.org] https://example.org";

    // WHEN
    final List<CheckError> errors = analyze(text);

    // THEN
    Assertions.assertThat(errors).isEmpty();
  }

  @DisplayName("should not report a bare URL after a bracket")
  @Test
  void ignoreBareUrlAfterBracket() {
    // GIVEN
    final String text = "[[ https://example.org Ex]] [[https://example.org Ex";

    // WHEN
    final List<CheckError> errors = analyze(text);

    // THEN
    Assertions.assertThat(errors).isEmpty();
  }

  @DisplayName("should keep the comments between the two brackets")
  @Test
  void fixDoubleBracketsWithComments() {
    // GIVEN
    final String text = "a [<!-- b --><!-- c -->[https://example.org|Ex] d";

    // WHEN
    final List<CheckError> errors = analyze(text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(
            new CheckError(
                ALGORITHM,
                2,
                text.indexOf(" d"),
                List.of(
                    new Replacement("<!-- b --><!-- c -->[https://example.org Ex]", false),
                    new Replacement("<!-- b --><!-- c -->[https://example.org|Ex]", false))));
  }

  private static CheckError error(
      final String text, final String link, final String... replacements) {
    return new CheckError(
        ALGORITHM,
        text.indexOf(link),
        text.indexOf(link) + link.length(),
        Arrays.stream(replacements)
            .map(replacement -> new Replacement(replacement, false))
            .toList());
  }

  private List<CheckError> analyze(final String text) {
    return algorithm.analyze(PageAnalysisTestFactory.createPageAnalysis("Lyon", text));
  }
}
