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
import org.wpcleaner.api.analysis.internallink.InternalLinkElement;
import org.wpcleaner.api.analysis.pagetitle.PageTitle;
import org.wpcleaner.api.analysis.parameter.ParameterElement;
import org.wpcleaner.api.analysis.template.TemplateElement;
import org.wpcleaner.api.analysis.template.TemplateParameter;

class PageAnalysisTemplateTest {

  @DisplayName("should analyze template without parameters")
  @Test
  void analyzeTemplate() {
    // GIVEN
    final String text = "{{ Hello }}";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getTemplates())
        .containsExactly(
            new TemplateElement(
                0, text.length(), "Hello", new PageTitle(10, "Hello"), List.of(), List.of()));
    Assertions.assertThat(analysis.getFunctions()).isEmpty();
  }

  @DisplayName("should analyze template with named and positional parameters")
  @Test
  void analyzeTemplateParameters() {
    // GIVEN
    final String text = "{{Infobox | name = Paris |France}}";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getTemplates())
        .containsExactly(
            new TemplateElement(
                0,
                text.length(),
                "Infobox",
                new PageTitle(10, "Infobox"),
                List.of(
                    new TemplateParameter(11, 25, "name", " Paris "),
                    new TemplateParameter(26, 32, null, "France")),
                List.of()));
  }

  @DisplayName("should split parameter name at the first equal sign only")
  @Test
  void analyzeTemplateParameterWithSeveralEqualSigns() {
    // GIVEN
    final String text = "{{T|a=b=c}}";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getTemplates())
        .containsExactly(
            new TemplateElement(
                0,
                text.length(),
                "T",
                new PageTitle(10, "T"),
                List.of(new TemplateParameter(4, 9, "a", "b=c")),
                List.of()));
  }

  @DisplayName("should not split parameters at a pipe inside an internal link")
  @Test
  void analyzeTemplateWithInternalLink() {
    // GIVEN
    final String text = "{{T|[[a|b]]}}";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getTemplates())
        .containsExactly(
            new TemplateElement(
                0,
                text.length(),
                "T",
                new PageTitle(10, "T"),
                List.of(new TemplateParameter(4, 11, null, "[[a|b]]")),
                List.of()));
    Assertions.assertThat(analysis.getInternalLinks())
        .containsExactly(
            new InternalLinkElement(4, 11, "a", new PageTitle(0, "A"), null, "b", false));
  }

  @DisplayName("should analyze nested templates")
  @Test
  void analyzeNestedTemplates() {
    // GIVEN
    final String text = "{{A|{{B|x}}}}";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getTemplates())
        .containsExactly(
            new TemplateElement(
                0,
                text.length(),
                "A",
                new PageTitle(10, "A"),
                List.of(new TemplateParameter(4, 11, null, "{{B|x}}")),
                List.of()),
            new TemplateElement(
                4,
                11,
                "B",
                new PageTitle(10, "B"),
                List.of(new TemplateParameter(8, 9, null, "x")),
                List.of()));
  }

  @DisplayName("should ignore comments in template name and pipes inside comments")
  @Test
  void analyzeTemplateWithComments() {
    // GIVEN
    final String text = "{{Hello<!-- c -->|a<!-- | -->b}}";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getTemplates())
        .containsExactly(
            new TemplateElement(
                0,
                text.length(),
                "Hello",
                new PageTitle(10, "Hello"),
                List.of(new TemplateParameter(18, 30, null, "a<!-- | -->b")),
                List.of()));
  }

  @DisplayName("should analyze templates inside tags with wiki content separately")
  @Test
  void analyzeTemplateWithRef() {
    // GIVEN
    final String text = "{{T|<ref>a|b</ref>}}<ref>{{X|y}}</ref>";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getTemplates())
        .containsExactly(
            new TemplateElement(
                0,
                20,
                "T",
                new PageTitle(10, "T"),
                List.of(new TemplateParameter(4, 18, null, "<ref>a|b</ref>")),
                List.of()),
            new TemplateElement(
                25,
                32,
                "X",
                new PageTitle(10, "X"),
                List.of(new TemplateParameter(29, 30, null, "y")),
                List.of()));
  }

  @DisplayName("should not analyze templates inside nowiki")
  @Test
  void analyzeTemplateInsideNowiki() {
    // GIVEN
    final String text = "<nowiki>{{T}}</nowiki>";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getTemplates()).isEmpty();
  }

  @DisplayName("should analyze only closed templates")
  @Test
  void analyzeUnclosedTemplate() {
    // GIVEN
    final String text = "{{A|{{B}}";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getTemplates())
        .containsExactly(
            new TemplateElement(
                4, text.length(), "B", new PageTitle(10, "B"), List.of(), List.of()));
  }

  @DisplayName("should not close a template inside an unclosed internal link")
  @Test
  void analyzeTemplateWithUnclosedInternalLink() {
    // GIVEN
    final String text = "{{A|[[b}}";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getTemplates()).isEmpty();
  }

  @DisplayName("should not analyze template with an empty name")
  @Test
  void analyzeTemplateWithEmptyName() {
    // GIVEN
    final String text = "{{ |x}}";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getTemplates()).isEmpty();
  }

  @DisplayName("should analyze parameter with default value")
  @Test
  void analyzeParameter() {
    // GIVEN
    final String text = "{{{1|default}}}{{{ name }}}";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getParameters())
        .containsExactly(
            new ParameterElement(0, 15, "1", "default"),
            new ParameterElement(15, text.length(), "name", null));
    Assertions.assertThat(analysis.getTemplates()).isEmpty();
  }

  @DisplayName("should analyze template named by a parameter")
  @Test
  void analyzeTemplateNamedByParameter() {
    // GIVEN
    final String text = "{{{{{1}}}}}";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getParameters())
        .containsExactly(new ParameterElement(2, 9, "1", null));
    Assertions.assertThat(analysis.getTemplates())
        .containsExactly(
            new TemplateElement(
                0, text.length(), "{{{1}}}", new PageTitle(10, "{{{1}}}"), List.of(), List.of()));
  }

  @DisplayName("should analyze parser function with a hash")
  @Test
  void analyzeFunctionWithHash() {
    // GIVEN
    final String text = "{{#if: x | yes | no }}";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getFunctions())
        .containsExactly(
            new FunctionElement(
                0,
                text.length(),
                "#if",
                "if",
                List.of(
                    new TemplateParameter(6, 9, null, " x "),
                    new TemplateParameter(10, 15, null, " yes "),
                    new TemplateParameter(16, 20, null, " no ")),
                List.of()));
    Assertions.assertThat(analysis.getTemplates()).isEmpty();
  }

  @DisplayName("should analyze parser function with a localized name")
  @Test
  void analyzeFunctionWithLocalizedName() {
    // GIVEN
    final String text = "{{#SI:x|y}}";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getFunctions())
        .singleElement()
        .extracting(FunctionElement::name, FunctionElement::magicWord)
        .containsExactly("#SI", "if");
  }

  @DisplayName("should analyze parser functions without a hash")
  @Test
  void analyzeFunctionWithoutHash() {
    // GIVEN
    final String text = "{{lc:ABC}}{{DEFAULTSORT:Key}}{{#LANGUAGE:fr}}";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getFunctions())
        .extracting(FunctionElement::magicWord)
        .containsExactly("lc", "defaultsort", "language");
    Assertions.assertThat(analysis.getTemplates()).isEmpty();
  }

  @DisplayName("should respect case sensitivity of parser functions")
  @Test
  void analyzeCaseSensitiveFunction() {
    // GIVEN
    final String text = "{{defaultsort:Key}}";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getFunctions()).isEmpty();
    Assertions.assertThat(analysis.getTemplates())
        .extracting(TemplateElement::name)
        .containsExactly("defaultsort:Key");
  }

  @DisplayName("should analyze variables only without parameters")
  @Test
  void analyzeVariable() {
    // GIVEN
    final String text = "{{PAGENAME}}{{NOMPAGE}}{{CURRENTYEAR|x}}";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getFunctions())
        .containsExactly(
            new FunctionElement(0, 12, "PAGENAME", "pagename", List.of(), List.of()),
            new FunctionElement(12, 23, "NOMPAGE", "pagename", List.of(), List.of()));
    Assertions.assertThat(analysis.getTemplates())
        .extracting(TemplateElement::name)
        .containsExactly("CURRENTYEAR");
  }

  @DisplayName("should analyze variable used as parser function")
  @Test
  void analyzeVariableUsedAsFunction() {
    // GIVEN
    final String text = "{{PAGENAME:Foo}}";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getFunctions())
        .containsExactly(
            new FunctionElement(
                0,
                text.length(),
                "PAGENAME",
                "pagename",
                List.of(new TemplateParameter(11, 14, null, "Foo")),
                List.of()));
  }
}
