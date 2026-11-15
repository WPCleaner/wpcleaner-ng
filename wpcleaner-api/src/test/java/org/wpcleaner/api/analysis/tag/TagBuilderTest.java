package org.wpcleaner.api.analysis.tag;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.wpcleaner.api.analysis.PageAnalysis;
import org.wpcleaner.api.analysis.PageAnalysisTestFactory;

class TagBuilderTest {

  @DisplayName("should build start, end and self-closing tags analyzed as such")
  @Test
  void build() {
    // GIVEN
    final TagBuilder builder = TagBuilder.from("br");

    // WHEN
    final List<String> tags =
        List.of(
            builder.build(),
            builder.withKind(TagKind.END).build(),
            builder.withKind(TagKind.SELF_CLOSING).build());

    // THEN
    Assertions.assertThat(tags).containsExactly("<br>", "</br>", "<br />");
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(String.join("", tags));
    Assertions.assertThat(analysis.getTags())
        .extracting(TagElement::name, TagElement::kind)
        .containsExactly(
            Assertions.tuple("br", TagKind.START),
            Assertions.tuple("br", TagKind.END),
            Assertions.tuple("br", TagKind.SELF_CLOSING));
  }
}
