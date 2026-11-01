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
import org.wpcleaner.api.analysis.pagetitle.PageTitle;
import org.wpcleaner.api.analysis.template.TemplateElement;
import org.wpcleaner.api.analysis.template.TemplateParameter;

class PageAnalysisTemplateModifierTest {

  @DisplayName("should analyze substituted template without the modifier in its name")
  @Test
  void analyzeSubstitutedTemplate() {
    // GIVEN
    final String text = "{{subst:Cite web|url=x}}";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getTemplates())
        .containsExactly(
            new TemplateElement(
                0,
                text.length(),
                "Cite web",
                new PageTitle(10, "Cite web"),
                List.of(new TemplateParameter(17, 22, "url", "x")),
                List.of("subst")));
  }

  @DisplayName("should analyze parser function after a modifier, whitespace and comments")
  @Test
  void analyzeSubstitutedFunction() {
    // GIVEN
    final String text = "{{ SAFESUBST: <!-- c --> #if: a | b }}";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getTemplates()).isEmpty();
    Assertions.assertThat(analysis.getFunctions())
        .containsExactly(
            new FunctionElement(
                0,
                text.length(),
                "#if",
                "if",
                List.of(
                    new TemplateParameter(29, 32, null, " a "),
                    new TemplateParameter(33, 36, null, " b ")),
                List.of("safesubst")));
  }

  @DisplayName("should analyze variables after subst but not after msgnw")
  @Test
  void analyzeModifiedVariables() {
    // GIVEN
    final String text = "{{subst:PAGENAME}}{{msgnw:PAGENAME}}";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getFunctions())
        .containsExactly(
            new FunctionElement(0, 18, "PAGENAME", "pagename", List.of(), List.of("subst")));
    Assertions.assertThat(analysis.getTemplates())
        .containsExactly(
            new TemplateElement(
                18,
                text.length(),
                "PAGENAME",
                new PageTitle(10, "PAGENAME"),
                List.of(),
                List.of("msgnw")));
  }

  @DisplayName("should analyze several modifiers in MediaWiki order, ignoring case")
  @Test
  void analyzeSeveralModifiers() {
    // GIVEN
    final String text = "{{Subst:msgnw:raw:T}}{{raw:subst:U}}{{substitute}}";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getTemplates())
        .containsExactly(
            new TemplateElement(
                0, 21, "T", new PageTitle(10, "T"), List.of(), List.of("subst", "msgnw", "raw")),
            new TemplateElement(
                21, 36, "subst:U", new PageTitle(10, "Subst:U"), List.of(), List.of("raw")),
            new TemplateElement(
                36,
                text.length(),
                "substitute",
                new PageTitle(10, "Substitute"),
                List.of(),
                List.of()));
  }
}
