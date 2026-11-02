package org.wpcleaner.api.analysis;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.wpcleaner.api.analysis.listitem.ListItemElement;
import org.wpcleaner.api.analysis.paragraph.ParagraphElement;
import org.wpcleaner.api.analysis.title.TitleElement;

class PageAnalysisLineEndTest {

  @DisplayName("should analyze Windows line ends as new lines, like MediaWiki")
  @Test
  void analyzeWindowsLineEnds() {
    // GIVEN
    final String text = "== T ==\r\n* a\r\n* b\r\n\r\nc";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getText()).isEqualTo("== T ==\n* a\n* b\n\nc");
    Assertions.assertThat(analysis.getTitles()).containsExactly(new TitleElement(0, 7, 2, "T"));
    Assertions.assertThat(analysis.getListItems())
        .containsExactly(
            new ListItemElement(8, 11, "*", "a"), new ListItemElement(12, 15, "*", "b"));
    Assertions.assertThat(analysis.getParagraphs()).containsExactly(new ParagraphElement(17, 18));
  }

  @DisplayName("should not analyze a carriage return alone as a new line, like MediaWiki")
  @Test
  void analyzeCarriageReturnAlone() {
    // GIVEN
    final String text = "* a\r* b";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getText()).isEqualTo(text);
    Assertions.assertThat(analysis.getListItems())
        .containsExactly(new ListItemElement(0, 7, "*", "a\r* b"));
  }
}
