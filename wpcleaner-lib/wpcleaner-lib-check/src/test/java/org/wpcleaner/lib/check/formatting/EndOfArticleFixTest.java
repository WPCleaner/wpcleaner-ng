package org.wpcleaner.lib.check.formatting;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.wpcleaner.lib.check.CheckSpringBootTest;

@CheckSpringBootTest
class EndOfArticleFixTest {

  @Autowired private EndOfArticleFix fix;

  @DisplayName("should remove whitespace at the end of the article, keeping one line end")
  @Test
  void removeTrailingWhitespace() {
    assertFixed("Text\n \n\n", "Text\n");
    assertFixed("Text  ", "Text");
  }

  @DisplayName("should leave a single trailing character unchanged")
  @Test
  void unchangedSingleTrailingCharacter() {
    assertFixed("Text\n", "Text\n");
    assertFixed("Text", "Text");
    assertFixed("\n\n", "\n\n");
  }

  private void assertFixed(final String text, final String expected) {
    Assertions.assertThat(FormattingTestHelper.fix(fix, text)).isEqualTo(expected);
  }
}
