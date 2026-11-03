package org.wpcleaner.api.analysis;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.wpcleaner.api.analysis.internallink.InternalLinkElement;
import org.wpcleaner.api.analysis.tag.CompleteTagElement;

class ElementTest {

  @DisplayName("should tell whether an element is inside another one")
  @Test
  void isInside() {
    // GIVEN
    final PageAnalysis analysis =
        PageAnalysisTestFactory.createPageAnalysis("<ref>[[A]]</ref> [[B]]");
    final CompleteTagElement reference = analysis.getCompleteTags().getFirst();
    final List<InternalLinkElement> links = analysis.getInternalLinks();

    // WHEN
    final List<Boolean> insideReference =
        links.stream().map(link -> link.isInside(reference)).toList();

    // THEN
    Assertions.assertThat(insideReference).containsExactly(true, false);
    Assertions.assertThat(reference.isInside(reference)).isTrue();
    Assertions.assertThat(reference.isInside(links.getFirst())).isFalse();
  }

  @DisplayName("should tell whether an element is inside one of several elements")
  @Test
  void isInsideOne() {
    // GIVEN
    final PageAnalysis analysis =
        PageAnalysisTestFactory.createPageAnalysis("<ref>[[A]]</ref> [[B]] <ref>c</ref>");
    final List<CompleteTagElement> references = analysis.getCompleteTags();

    // WHEN
    final List<Boolean> insideOneReference =
        analysis.getInternalLinks().stream().map(link -> link.isInsideOne(references)).toList();

    // THEN
    Assertions.assertThat(insideOneReference).containsExactly(true, false);
    Assertions.assertThat(references.getFirst().isInsideOne(List.of())).isFalse();
  }
}
