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
import org.springframework.boot.test.context.SpringBootTest;
import org.wpcleaner.api.analysis.PageAnalysis;
import org.wpcleaner.api.analysis.PageAnalysisTestFactory;
import org.wpcleaner.lib.check.CheckError;
import org.wpcleaner.lib.check.CheckSpringBootTestConfiguration;

@SpringBootTest(classes = CheckSpringBootTestConfiguration.class)
class CheckAlgorithm048Test {

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
            new CheckError(48, 0, 15), new CheckError(48, 16, 36), new CheckError(48, 46, 62));
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
    Assertions.assertThat(mainErrors).containsExactly(new CheckError(48, 0, text.length()));
    Assertions.assertThat(userErrors).isEmpty();
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
        .containsExactly(new CheckError(48, noIncludeLink, noIncludeLink + 9));
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
}
