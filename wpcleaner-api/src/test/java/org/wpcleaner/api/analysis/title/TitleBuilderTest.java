package org.wpcleaner.api.analysis.title;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.wpcleaner.api.analysis.PageAnalysis;
import org.wpcleaner.api.analysis.PageAnalysisTestFactory;

class TitleBuilderTest {

  @DisplayName("should build a title with spaces around its text")
  @Test
  void buildTitle() {
    final String title = TitleBuilder.from(2, "Paris").build();

    Assertions.assertThat(title).isEqualTo("== Paris ==");
    assertTitle(title, 2, "Paris");
  }

  @DisplayName("should strip the text of the title")
  @Test
  void stripText() {
    final String title = TitleBuilder.from(3, "  Paris\t").build();

    Assertions.assertThat(title).isEqualTo("=== Paris ===");
    assertTitle(title, 3, "Paris");
  }

  @DisplayName("should build a title without text")
  @Test
  void buildEmptyTitle() {
    final String title = TitleBuilder.from(2, " ").build();

    Assertions.assertThat(title).isEqualTo("== ==");
    assertTitle(title, 2, "");
  }

  private static void assertTitle(final String text, final int level, final String title) {
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);
    Assertions.assertThat(analysis.getTitles())
        .containsExactly(new TitleElement(0, text.length(), level, title));
  }
}
