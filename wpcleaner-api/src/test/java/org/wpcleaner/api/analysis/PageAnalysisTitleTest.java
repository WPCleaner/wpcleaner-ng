package org.wpcleaner.api.analysis;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.wpcleaner.api.analysis.template.TemplateElement;
import org.wpcleaner.api.analysis.template.TemplateParameter;
import org.wpcleaner.api.analysis.title.TitleElement;

class PageAnalysisTitleTest {

  @DisplayName("should analyze title")
  @Test
  void analyzeTitle() {
    // GIVEN
    final String text = "== Title ==";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getTitles())
        .containsExactly(new TitleElement(0, text.length(), 2, "Title"));
  }

  @DisplayName("should analyze titles on consecutive lines")
  @Test
  void analyzeTitlesOnConsecutiveLines() {
    // GIVEN
    final String text = "Intro\n=== Sub ===\n== Next ==\ntext";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getTitles())
        .containsExactly(new TitleElement(6, 17, 3, "Sub"), new TitleElement(18, 28, 2, "Next"));
  }

  @DisplayName("should use the smallest count of equal signs as level")
  @Test
  void analyzeUnbalancedTitle() {
    // GIVEN
    final String text = "=== Title ==";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getTitles())
        .containsExactly(new TitleElement(0, text.length(), 2, "= Title"));
  }

  @DisplayName("should limit title level to 6")
  @Test
  void analyzeTitleWithTooManyEqualSigns() {
    // GIVEN
    final String text = "======= Title =======";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getTitles())
        .containsExactly(new TitleElement(0, text.length(), 6, "= Title ="));
  }

  @DisplayName("should ignore whitespace and comments after title")
  @Test
  void analyzeTitleFollowedByComment() {
    // GIVEN
    final String text = "== Title == <!-- a --><!-- b --> \ntext";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getTitles())
        .containsExactly(new TitleElement(0, 11, 2, "Title"));
  }

  @DisplayName("should analyze a line made only of equal signs")
  @Test
  void analyzeTitleOnlyEqualSigns() {
    // GIVEN
    final String text = "===\n==\n=====";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getTitles())
        .containsExactly(new TitleElement(0, 3, 1, "="), new TitleElement(7, 12, 2, "="));
  }

  @DisplayName("should not analyze title not at line start or followed by text")
  @Test
  void analyzeNotTitles() {
    // GIVEN
    final String text = "x == a ==\n == b ==\n== c == d\n<!--\n== e ==\n-->";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getTitles()).isEmpty();
  }

  @DisplayName("should analyze title inside a template parameter")
  @Test
  void analyzeTitleInsideTemplate() {
    // GIVEN
    final String text = "{{T|\n== a|b ==\n}}";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getTitles()).containsExactly(new TitleElement(5, 14, 2, "a|b"));
    Assertions.assertThat(analysis.getTemplates())
        .containsExactly(
            new TemplateElement(
                0,
                text.length(),
                "T",
                List.of(new TemplateParameter(4, 15, null, "\n== a|b ==\n")),
                List.of()));
  }

  @DisplayName("should use a single equal sign at line start as parameter name separator")
  @Test
  void analyzeEqualSignAtLineStartInsideTemplate() {
    // GIVEN
    final String text = "{{T|\n=a=\n}}";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getTitles()).isEmpty();
    Assertions.assertThat(analysis.getTemplates())
        .containsExactly(
            new TemplateElement(
                0,
                text.length(),
                "T",
                List.of(new TemplateParameter(4, 9, "", "a=\n")),
                List.of()));
  }
}
