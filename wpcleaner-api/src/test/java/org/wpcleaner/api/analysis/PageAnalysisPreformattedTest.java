package org.wpcleaner.api.analysis;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.wpcleaner.api.analysis.paragraph.ParagraphElement;
import org.wpcleaner.api.analysis.preformatted.PreformattedElement;

class PageAnalysisPreformattedTest {

  @DisplayName("should analyze consecutive lines starting with a space as preformatted block")
  @Test
  void analyzePreformattedBlock() {
    // GIVEN
    final String text = "Text\n code\n  more\n\nafter";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getPreformattedBlocks())
        .containsExactly(new PreformattedElement(5, 17));
    Assertions.assertThat(analysis.getParagraphs())
        .containsExactly(new ParagraphElement(0, 4), new ParagraphElement(19, 24));
  }

  @DisplayName("should continue preformatted block on a line with only spaces")
  @Test
  void analyzePreformattedBlockWithSpaceLine() {
    // GIVEN
    final String text = " a\n \n b\n\nx\n \ny";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getPreformattedBlocks())
        .containsExactly(new PreformattedElement(0, 7));
    Assertions.assertThat(analysis.getParagraphs())
        .containsExactly(new ParagraphElement(9, 10), new ParagraphElement(13, 14));
  }

  @DisplayName("should not analyze line starting with a tab or a table as preformatted")
  @Test
  void analyzeNotPreformatted() {
    // GIVEN
    final String text = "\tx\n\n {|\n| a\n|}";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getPreformattedBlocks()).isEmpty();
    Assertions.assertThat(analysis.getParagraphs()).containsExactly(new ParagraphElement(0, 2));
  }

  @DisplayName("should not analyze preformatted block inside block tags until they are closed")
  @Test
  void analyzePreformattedInsideBlockTags() {
    // GIVEN
    final String text = "<table>\n a\n</table>\n<div>\n b\n</div>";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getPreformattedBlocks())
        .containsExactly(new PreformattedElement(26, 28));
    Assertions.assertThat(analysis.getParagraphs()).isEmpty();
  }
}
