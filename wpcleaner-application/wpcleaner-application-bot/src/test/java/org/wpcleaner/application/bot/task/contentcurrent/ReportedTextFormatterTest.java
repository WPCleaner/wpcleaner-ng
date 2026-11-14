package org.wpcleaner.application.bot.task.contentcurrent;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ReportedTextFormatterTest {

  @DisplayName("should display the reported text between nowiki tags")
  @Test
  void formatText() {
    // WHEN
    final String formatted = ReportedTextFormatter.format("[[Paris]] {{x}} ''y''");

    // THEN
    Assertions.assertThat(formatted).isEqualTo("<nowiki>[[Paris]] {{x}} ''y''</nowiki>");
  }

  @DisplayName("should escape the characters that would change the displayed text")
  @Test
  void formatSpecialCharacters() {
    // WHEN
    final String formatted = ReportedTextFormatter.format("a&amp;b <ref>c</nowiki>");

    // THEN
    Assertions.assertThat(formatted)
        .isEqualTo("<nowiki>a&amp;amp;b &lt;ref>c&lt;/nowiki></nowiki>");
  }

  @DisplayName("should replace the line breaks with a symbol")
  @Test
  void formatLineBreaks() {
    // WHEN
    final String formatted = ReportedTextFormatter.format("a\nb\r\nc\rd");

    // THEN
    Assertions.assertThat(formatted).isEqualTo("<nowiki>a↵b↵c↵d</nowiki>");
  }
}
