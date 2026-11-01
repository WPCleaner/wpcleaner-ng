package org.wpcleaner.api.analysis;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.wpcleaner.api.analysis.behaviorswitch.BehaviorSwitchElement;

class PageAnalysisBehaviorSwitchTest {

  @DisplayName("should analyze behavior switch")
  @Test
  void analyzeBehaviorSwitch() {
    // GIVEN
    final String text = "Text __TOC__ more";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getBehaviorSwitches())
        .containsExactly(new BehaviorSwitchElement(5, 12, "__TOC__", "toc"));
  }

  @DisplayName("should analyze localized behavior switch ignoring case")
  @Test
  void analyzeLocalizedBehaviorSwitch() {
    // GIVEN
    final String text = "__sommaire__";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getBehaviorSwitches())
        .containsExactly(new BehaviorSwitchElement(0, text.length(), "__sommaire__", "toc"));
  }

  @DisplayName("should analyze behavior switches from left to right")
  @Test
  void analyzeOverlappingBehaviorSwitches() {
    // GIVEN
    final String text = "__NOTOC__TOC__ ___TOC__";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getBehaviorSwitches())
        .containsExactly(
            new BehaviorSwitchElement(0, 9, "__NOTOC__", "notoc"),
            new BehaviorSwitchElement(16, 23, "__TOC__", "toc"));
  }

  @DisplayName("should analyze behavior switches inside templates and links")
  @Test
  void analyzeBehaviorSwitchesInsideTemplateAndLink() {
    // GIVEN
    final String text = "{{T|__NOTOC__}} [[a|__TOC__]]";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getBehaviorSwitches())
        .containsExactly(
            new BehaviorSwitchElement(4, 13, "__NOTOC__", "notoc"),
            new BehaviorSwitchElement(20, 27, "__TOC__", "toc"));
  }

  @DisplayName("should not analyze behavior switches in comments, non-wiki tags or unknown")
  @Test
  void analyzeNotBehaviorSwitches() {
    // GIVEN
    final String text = "<!-- __TOC__ --><nowiki>__TOC__</nowiki>__FOO__";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getBehaviorSwitches()).isEmpty();
  }
}
