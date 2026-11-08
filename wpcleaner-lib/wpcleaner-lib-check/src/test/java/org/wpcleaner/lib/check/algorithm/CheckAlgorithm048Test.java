package org.wpcleaner.lib.check.algorithm;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.wpcleaner.api.ApiSpringBootTest;
import org.wpcleaner.api.analysis.PageAnalysis;
import org.wpcleaner.api.analysis.PageAnalysisTestFactory;
import org.wpcleaner.lib.check.CheckError;
import org.wpcleaner.lib.check.Replacement;

@ApiSpringBootTest
class CheckAlgorithm048Test {

  private static final int ALGORITHM = 48;

  @Autowired private CheckAlgorithm048 algorithm;

  @DisplayName("should report links to the page itself, whatever their writing")
  @Test
  void reportLinksToPage() {
    // GIVEN
    final PageAnalysis analysis =
        PageAnalysisTestFactory.createPageAnalysis(
            "Tour Eiffel", "[[Tour Eiffel]] [[tour_Eiffel|tour]] [[Tour]] [[:Tour Eiffel]]");

    // WHEN
    final List<CheckError> errors = algorithm.analyze(analysis);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(
            new CheckError(ALGORITHM, 0, 15, textOrBold("Tour Eiffel", false)),
            new CheckError(ALGORITHM, 16, 36, textOrBold("tour", false)),
            new CheckError(ALGORITHM, 46, 62, textOrBold("Tour Eiffel", false)));
  }

  @DisplayName("should fix automatically links in bold or after the first title")
  @Test
  void fixLinksInBoldOrAfterFirstTitle() {
    // GIVEN
    final String text =
        "'''[[Paris]]''' est une ville.\n== Histoire ==\n[[Paris|La ville]] et l'[[Paris]]";

    // WHEN
    final List<CheckError> errors =
        algorithm.analyze(PageAnalysisTestFactory.createPageAnalysis("Paris", text));

    // THEN
    final int afterTitle = text.indexOf("[[Paris|");
    final int afterApostrophe = text.lastIndexOf("[[Paris]]");
    Assertions.assertThat(errors)
        .containsExactly(
            new CheckError(ALGORITHM, 3, 12, List.of(new Replacement("Paris", true))),
            new CheckError(ALGORITHM, afterTitle, afterTitle + 18, textOrBold("La ville", true)),
            new CheckError(
                ALGORITHM,
                afterApostrophe - 1,
                afterApostrophe + 9,
                List.of(new Replacement("'Paris", true), new Replacement("''''Paris'''", false))));
  }

  @DisplayName("should report links to a section of the page only in the main namespace")
  @Test
  void reportLinksToSection() {
    // GIVEN
    final String text = "[[Paris#Histoire|histoire]]";

    // WHEN
    final List<CheckError> mainErrors =
        algorithm.analyze(PageAnalysisTestFactory.createPageAnalysis("Paris", text));
    final List<CheckError> userErrors =
        algorithm.analyze(
            PageAnalysisTestFactory.createPageAnalysis(
                "Utilisateur:Paris", "[[Utilisateur:Paris#Histoire|histoire]]"));

    // THEN
    Assertions.assertThat(mainErrors)
        .containsExactly(
            new CheckError(
                ALGORITHM,
                0,
                text.length(),
                List.of(new Replacement("[[#Histoire|histoire]]", true))));
    Assertions.assertThat(userErrors).isEmpty();
  }

  @DisplayName("should not fix automatically links to footnotes or without text")
  @Test
  void fixLinksToSectionManually() {
    // GIVEN
    final String text = "[[Paris#cite_note-1|1]] [[Paris#Histoire]] [[Paris#]]";

    // WHEN
    final List<CheckError> errors =
        algorithm.analyze(PageAnalysisTestFactory.createPageAnalysis("Paris", text));

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(
            new CheckError(ALGORITHM, 0, 23, List.of(new Replacement("[[#cite_note-1|1]]", false))),
            new CheckError(
                ALGORITHM,
                24,
                42,
                List.of(
                    new Replacement("[[#Histoire|Histoire]]", false),
                    new Replacement("[[#Histoire|Paris#Histoire]]", false))),
            new CheckError(ALGORITHM, 43, 53, List.of()));
  }

  @DisplayName("should not report links in ignored areas")
  @Test
  void ignoreLinksInIgnoredAreas() {
    // GIVEN
    final String text =
        "<includeonly>[[Paris]]</includeonly><onlyinclude>[[Paris]]</onlyinclude>"
            + "<imagemap>Image:A.jpg\nrect 0 0 1 1 [[Paris]]</imagemap>"
            + "<noinclude>[[Paris]]</noinclude>";

    // WHEN
    final List<CheckError> errors =
        algorithm.analyze(PageAnalysisTestFactory.createPageAnalysis("Paris", text));

    // THEN
    final int noIncludeLink = text.lastIndexOf("[[Paris]]");
    Assertions.assertThat(errors)
        .containsExactly(
            new CheckError(
                ALGORITHM, noIncludeLink, noIncludeLink + 9, textOrBold("Paris", false)));
  }

  @DisplayName("should not report anything on ignored pages")
  @Test
  void ignorePages() {
    // GIVEN
    final List<PageAnalysis> analyses =
        List.of(
            PageAnalysisTestFactory.createPageAnalysis("Paris", "#REDIRECT [[Paris]]"),
            PageAnalysisTestFactory.createPageAnalysis(
                "Discussion utilisateur:Bob", "[[Discussion utilisateur:Bob]]"),
            PageAnalysisTestFactory.createPageAnalysis("Paris", "[[Paris]] {{#lst:Lyon|a}}"),
            PageAnalysisTestFactory.createPageAnalysis("Paris", "[[Paris]] <section begin=a />"));

    // WHEN
    final List<List<CheckError>> errors = analyses.stream().map(algorithm::analyze).toList();

    // THEN
    Assertions.assertThat(errors).containsOnly(List.of());
  }

  private static List<Replacement> textOrBold(final String text, final boolean automatic) {
    return List.of(new Replacement(text, automatic), new Replacement("'''" + text + "'''", false));
  }
}
