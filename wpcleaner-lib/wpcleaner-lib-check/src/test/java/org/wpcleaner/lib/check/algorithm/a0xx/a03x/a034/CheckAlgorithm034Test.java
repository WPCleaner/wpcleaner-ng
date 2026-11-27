package org.wpcleaner.lib.check.algorithm.a0xx.a03x.a034;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.wpcleaner.api.analysis.PageAnalysisTestFactory;
import org.wpcleaner.lib.check.CheckError;
import org.wpcleaner.lib.check.CheckSpringBootTest;
import org.wpcleaner.lib.check.ExpandedReplacement;
import org.wpcleaner.lib.check.Replacement;
import org.wpcleaner.lib.check.Suggestion;

@CheckSpringBootTest
class CheckAlgorithm034Test {

  private static final int ALGORITHM = 34;
  private static final List<Replacement> DELETION = List.of(new Replacement("", false));

  @Autowired private CheckAlgorithm034 algorithm;

  @DisplayName("should not report anything in the template namespace")
  @Test
  void ignoreTemplateNamespace() {
    // GIVEN
    final String text = "{{#if:{{{1|}}}|a}} {{subst:X}} __FORCETOC__";

    // WHEN
    final List<CheckError> errors = analyze("Modèle:Paris", text);

    // THEN
    Assertions.assertThat(errors).isEmpty();
  }

  @DisplayName("should report parameters and suggest their default value")
  @Test
  void reportParameters() {
    // GIVEN
    final String text = "a {{{1|default}}} b {{{name}}} c {{{2|}}} d";

    // WHEN
    final List<CheckError> errors = analyze("Paris", text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(
            error(text, "{{{1|default}}}", List.of(new Replacement("default", false))),
            error(text, "{{{name}}}", List.of()),
            error(text, "{{{2|}}}", List.of(new Replacement("", false))));
  }

  @DisplayName("should report parameters inside templates but not the templates")
  @Test
  void reportParametersInsideTemplates() {
    // GIVEN
    final String text = "a {{Lang|en|{{{1}}}}} b";

    // WHEN
    final List<CheckError> errors = analyze("Paris", text);

    // THEN
    Assertions.assertThat(errors).containsExactly(error(text, "{{{1}}}", List.of()));
  }

  @DisplayName("should report three opening braces before a template")
  @Test
  void reportExtraBrace() {
    // GIVEN
    final String text = "a {{{Lang|en|b}} c";

    // WHEN
    final List<CheckError> errors = analyze("Paris", text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(new CheckError(ALGORITHM, 2, 5, List.of(new Replacement("{{", false))));
  }

  @DisplayName("should not report a template whose name is a template")
  @Test
  void ignoreTemplateInTemplateName() {
    // GIVEN
    final String text = "a {{{{Lang}}|b}} c";

    // WHEN
    final List<CheckError> errors = analyze("Paris", text);

    // THEN
    Assertions.assertThat(errors).isEmpty();
  }

  @DisplayName(
      "should suggest substituting and expanding parser functions, ignoring their contents")
  @Test
  void reportFunctions() {
    // GIVEN
    final String text = "a {{#if:{{{1|}}}|b|c}} d {{#tag:nowiki|e}} f";

    // WHEN
    final List<CheckError> errors = analyze("Paris", text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(
            error(
                text,
                "{{#if:{{{1|}}}|b|c}}",
                List.of(
                    new Replacement("{{subst:#if:{{{1|}}}|b|c}}", false),
                    expand("{{#if:{{{1|}}}|b|c}}"))),
            error(
                text,
                "{{#tag:nowiki|e}}",
                List.of(
                    new Replacement("{{subst:#tag:nowiki|e}}", false),
                    expand("{{#tag:nowiki|e}}"))));
  }

  @DisplayName("should not report sort keys, displayed titles, formatted numbers and tag refs")
  @Test
  void ignoreFunctions() {
    // GIVEN
    final String text =
        "{{formatnum:1234}} {{DISPLAYTITLE:''A''}} {{#tag:ref|b}} {{ #tag: REF |c}}"
            + " {{DEFAULTSORT:{{{1}}}}}";

    // WHEN
    final List<CheckError> errors = analyze("Paris", text);

    // THEN
    Assertions.assertThat(errors).containsExactly(error(text, "{{{1}}}", List.of()));
  }

  @DisplayName("should suggest the page name for a page name variable")
  @Test
  void replacePageName() {
    // GIVEN
    final String text = "a {{PAGENAME}} b";

    // WHEN
    final List<CheckError> errors = analyze("Utilisateur:Paris", text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(
            error(
                text,
                "{{PAGENAME}}",
                List.of(
                    new Replacement("Paris", false),
                    new Replacement("{{subst:PAGENAME}}", false),
                    expand("{{PAGENAME}}"))));
  }

  @DisplayName("should suggest each result of an expression test")
  @Test
  void replaceExpressionTest() {
    // GIVEN
    final String text = "a {{#ifexpr:1>2|yes|no}} b";

    // WHEN
    final List<CheckError> errors = analyze("Paris", text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(
            error(
                text,
                "{{#ifexpr:1>2|yes|no}}",
                List.of(
                    new Replacement("yes", false),
                    new Replacement("no", false),
                    new Replacement("{{subst:#ifexpr:1>2|yes|no}}", false),
                    expand("{{#ifexpr:1>2|yes|no}}"))));
  }

  @DisplayName("should suggest expanding module invocations, without substitution")
  @Test
  void reportInvoke() {
    // GIVEN
    final String text = "a {{#invoke:Module|main}} b";

    // WHEN
    final List<CheckError> errors = analyze("Paris", text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(
            error(text, "{{#invoke:Module|main}}", List.of(expand("{{#invoke:Module|main}}"))));
  }

  @DisplayName("should report substitutions without replacement, even of ignored functions")
  @Test
  void reportSubstitutions() {
    // GIVEN
    final String text =
        "a {{subst:Lang|{{{1}}}}} b {{safesubst:#if:x|y}} c {{subst:DEFAULTSORT:d}}";

    // WHEN
    final List<CheckError> errors = analyze("Paris", text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(
            error(text, "{{subst:Lang|{{{1}}}}}", List.of()),
            error(text, "{{safesubst:#if:x|y}}", List.of()),
            error(text, "{{subst:DEFAULTSORT:d}}", List.of()));
  }

  @DisplayName(
      "should not suggest a substitution nor an expansion inside references, galleries and include"
          + " only")
  @Test
  void noSubstitutionInsideProtectedAreas() {
    // GIVEN
    final String text =
        "a <ref>{{#if:x|y}}</ref> b <includeonly>{{#if:x|z}}</includeonly>"
            + " c <gallery>\nFile:A.jpg|{{#if:x|w}}\n</gallery>";

    // WHEN
    final List<CheckError> errors = analyze("Paris", text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(
            error(text, "{{#if:x|y}}", List.of()),
            error(text, "{{#if:x|z}}", List.of()),
            error(text, "{{#if:x|w}}", List.of()));
  }

  @DisplayName("should suggest deleting index and table of contents behavior switches")
  @Test
  void reportBehaviorSwitches() {
    // GIVEN
    final String text =
        "__FORCETOC__ a __INDEX__ b __NOINDEX__ c __NONEWSECTIONLINK__ d __NOTOC__ e __TOC__";

    // WHEN
    final List<CheckError> errors = analyze("Paris", text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(
            error(text, "__FORCETOC__", DELETION),
            error(text, "__INDEX__", DELETION),
            error(text, "__NOINDEX__", DELETION),
            error(text, "__NONEWSECTIONLINK__", DELETION));
  }

  private List<CheckError> analyze(final String title, final String text) {
    return algorithm.analyze(PageAnalysisTestFactory.createPageAnalysis(title, text));
  }

  private static CheckError error(
      final String text, final String found, final List<? extends Suggestion> suggestions) {
    final int begin = text.lastIndexOf(found);
    return new CheckError(ALGORITHM, begin, begin + found.length(), suggestions);
  }

  private static ExpandedReplacement expand(final String text) {
    return new ExpandedReplacement(text, "Expand");
  }
}
