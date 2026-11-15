package org.wpcleaner.api.analysis;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.wpcleaner.api.analysis.function.FunctionElement;
import org.wpcleaner.api.analysis.template.TemplateElement;

class PageAnalysisWithTextTest {

  @DisplayName("should analyze another text of the same page with the same configuration")
  @Test
  void analyzeOtherText() {
    // GIVEN
    final PageAnalysis analysis =
        PageAnalysisTestFactory.createPageAnalysis("Utilisateur:Paris", "{{Infobox}}");

    // WHEN
    final PageAnalysis other = analysis.withText("{{PAGENAME}} {{subst:Date}}");

    // THEN
    Assertions.assertThat(other.getWiki()).isEqualTo(analysis.getWiki());
    Assertions.assertThat(other.getTitle()).isEqualTo("Utilisateur:Paris");
    Assertions.assertThat(other.getText()).isEqualTo("{{PAGENAME}} {{subst:Date}}");
    Assertions.assertThat(other.getFunctions())
        .extracting(FunctionElement::magicWord)
        .containsExactly("pagename");
    Assertions.assertThat(other.getTemplates())
        .extracting(TemplateElement::name, TemplateElement::modifiers)
        .containsExactly(Assertions.tuple("Date", List.of("subst")));
  }
}
