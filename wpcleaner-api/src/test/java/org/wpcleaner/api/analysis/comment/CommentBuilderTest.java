package org.wpcleaner.api.analysis.comment;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.wpcleaner.api.analysis.PageAnalysis;
import org.wpcleaner.api.analysis.PageAnalysisTestFactory;

class CommentBuilderTest {

  @DisplayName("should build a comment surrounded by spaces")
  @Test
  void buildComment() {
    // WHEN
    final String comment = CommentBuilder.from(" [[Category:Paris]]\n").build();

    // THEN
    Assertions.assertThat(comment).isEqualTo("<!-- [[Category:Paris]] -->");
    assertSingleComment(comment);
  }

  @DisplayName("should build an empty comment")
  @Test
  void buildEmptyComment() {
    // WHEN
    final String comment = CommentBuilder.from(" ").build();

    // THEN
    Assertions.assertThat(comment).isEqualTo("<!-- -->");
    assertSingleComment(comment);
  }

  @DisplayName("should neutralize comment tokens inside the comment")
  @Test
  void escapeCommentTokens() {
    // WHEN
    final String comment = CommentBuilder.from("A <!-- B --> C").build();

    // THEN
    Assertions.assertThat(comment).isEqualTo("<!-- A < !-- B -- > C -->");
    assertSingleComment(comment);
  }

  private static void assertSingleComment(final String comment) {
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis("A" + comment + "B");
    Assertions.assertThat(analysis.getComments())
        .containsExactly(new CommentElement(1, 1 + comment.length()));
  }
}
