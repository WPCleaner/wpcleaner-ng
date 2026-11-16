package org.wpcleaner.lib.check.algorithm.a0xx.a01x.a011;

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
import org.wpcleaner.lib.check.Replacement;

@CheckSpringBootTest
class CheckAlgorithm011Test {

  private static final int ALGORITHM = 11;

  @Autowired private CheckAlgorithm011 algorithm;

  @DisplayName("should automatically replace the entities by their character")
  @Test
  void replaceEntities() {
    // GIVEN
    final String text = "&eacute;t&eacute; &Agrave; &oelig; &frac12; &rArr; &quot;x&quot;";

    // WHEN
    final List<CheckError> errors = analyze(text);

    // THEN
    final int capital = text.indexOf("&Agrave;");
    Assertions.assertThat(errors)
        .startsWith(
            new CheckError(ALGORITHM, 0, "&eacute;".length(), automatic("é")),
            new CheckError(ALGORITHM, 9, 9 + "&eacute;".length(), automatic("é")),
            new CheckError(ALGORITHM, capital, capital + "&Agrave;".length(), automatic("À")))
        .extracting(CheckError::replacements)
        .containsExactly(
            automatic("é"),
            automatic("é"),
            automatic("À"),
            automatic("œ"),
            automatic("½"),
            automatic("⇒"),
            automatic("\""),
            automatic("\""));
  }

  @DisplayName("should not report entities handled by other algorithms or kept as entities")
  @Test
  void ignoreOtherEntities() {
    // GIVEN
    final String text =
        "&mdash; &ndash; &dagger; &Alpha; &amp; &nbsp; &lt; &gt; &shy; &eacute &unknown;"
            + " <!-- &eacute; --> <source>&eacute;</source> [http://example.org/?a&eacute;b x]";

    // WHEN
    final List<CheckError> errors = analyze(text);

    // THEN
    Assertions.assertThat(errors).isEmpty();
  }

  @DisplayName("should not report anything when the page has a math tag or a math template")
  @Test
  void ignorePagesWithMath() {
    // GIVEN
    final String withTag = "&eacute; <math>x</math>";
    final String withUnclosedTag = "&eacute; <math>";
    final String withTemplate = "&eacute; {{math|x}}";
    final String withTemplateNamespace = "&eacute; {{Modèle:Math|x}}";

    // WHEN
    final List<CheckError> errorsWithTag = analyze(withTag);
    final List<CheckError> errorsWithUnclosedTag = analyze(withUnclosedTag);
    final List<CheckError> errorsWithTemplate = analyze(withTemplate);
    final List<CheckError> errorsWithTemplateNamespace = analyze(withTemplateNamespace);

    // THEN
    Assertions.assertThat(errorsWithTag).isEmpty();
    Assertions.assertThat(errorsWithUnclosedTag).isEmpty();
    Assertions.assertThat(errorsWithTemplate).isEmpty();
    Assertions.assertThat(errorsWithTemplateNamespace).isEmpty();
  }

  @DisplayName("should report entities when math is only in nowiki, or in other templates")
  @Test
  void reportWithoutRealMath() {
    // GIVEN
    final String text = "&eacute; <nowiki><math>x</math></nowiki> {{maths|x}} [[math]]";

    // WHEN
    final List<CheckError> errors = analyze(text);

    // THEN
    Assertions.assertThat(errors).hasSize(1);
  }

  @DisplayName("should replace manually a quotation mark in a table, but not other entities")
  @Test
  void replaceManuallyQuotationMarkInTable() {
    // GIVEN
    final String text = "&quot;\n{|\n|-\n| &quot; &eacute;\n|}\n&quot;";

    // WHEN
    final List<CheckError> errors = analyze(text);

    // THEN
    Assertions.assertThat(errors)
        .extracting(CheckError::replacements)
        .containsExactly(
            automatic("\""),
            List.of(new Replacement("\"", false)),
            automatic("é"),
            automatic("\""));
  }

  @DisplayName("should not suggest to replace a quotation mark inside a tag")
  @Test
  void keepQuotationMarkInTag() {
    // GIVEN
    final String text = "<span title=\"&quot;a&eacute;\">&quot;</span>";

    // WHEN
    final List<CheckError> errors = analyze(text);

    // THEN
    Assertions.assertThat(errors)
        .extracting(CheckError::begin, CheckError::replacements)
        .containsExactly(
            Assertions.tuple(text.indexOf("&quot;"), List.of()),
            Assertions.tuple(text.indexOf("&eacute;"), automatic("é")),
            Assertions.tuple(text.lastIndexOf("&quot;"), automatic("\"")));
  }

  private static List<Replacement> automatic(final String text) {
    return List.of(new Replacement(text, true));
  }

  private List<CheckError> analyze(final String text) {
    return algorithm.analyze(PageAnalysisTestFactory.createPageAnalysis("Lyon", text));
  }
}
