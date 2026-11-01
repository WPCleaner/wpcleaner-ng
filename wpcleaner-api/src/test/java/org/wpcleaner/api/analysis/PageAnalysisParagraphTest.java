package org.wpcleaner.api.analysis;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.wpcleaner.api.analysis.paragraph.ParagraphElement;

class PageAnalysisParagraphTest {

  @DisplayName("should analyze paragraphs separated by blank lines")
  @Test
  void analyzeParagraphs() {
    // GIVEN
    final String text = "First line\nsecond line\n\nThird";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getParagraphs())
        .containsExactly(new ParagraphElement(0, 22), new ParagraphElement(24, 29));
  }

  @DisplayName("should end paragraphs at titles, list items, preformatted lines, rules and tables")
  @Test
  void analyzeParagraphsBetweenOtherBlocks() {
    // GIVEN
    final String text = "a\n== T ==\nb\n* c\nd\n e\nf\n----\ng\n{|\n| h\n|}\ni";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getParagraphs())
        .containsExactly(
            new ParagraphElement(0, 1),
            new ParagraphElement(10, 11),
            new ParagraphElement(16, 17),
            new ParagraphElement(21, 22),
            new ParagraphElement(28, 29),
            new ParagraphElement(40, 41));
  }

  @DisplayName("should not end paragraph at a line with only a comment")
  @Test
  void analyzeParagraphWithCommentLine() {
    // GIVEN
    final String text = "a\n<!-- c -->\nb";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getParagraphs())
        .containsExactly(new ParagraphElement(0, text.length()));
  }

  @DisplayName("should analyze paragraph starting with a tag")
  @Test
  void analyzeParagraphStartingWithTag() {
    // GIVEN
    final String text = "<nowiki>*</nowiki> a";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getParagraphs())
        .containsExactly(new ParagraphElement(0, text.length()));
    Assertions.assertThat(analysis.getListItems()).isEmpty();
  }
}
