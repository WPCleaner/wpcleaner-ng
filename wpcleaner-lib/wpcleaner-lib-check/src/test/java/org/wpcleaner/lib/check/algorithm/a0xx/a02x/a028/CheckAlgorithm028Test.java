package org.wpcleaner.lib.check.algorithm.a0xx.a02x.a028;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.wpcleaner.api.analysis.PageAnalysisTestFactory;
import org.wpcleaner.lib.check.CheckError;
import org.wpcleaner.lib.check.CheckSpringBootTest;
import org.wpcleaner.lib.check.configuration.CheckAlgorithmParameters;
import org.wpcleaner.lib.check.configuration.CheckWikiConfiguration;
import org.wpcleaner.lib.check.configuration.CheckWikiConfigurationRepository;

@CheckSpringBootTest
class CheckAlgorithm028Test {

  private static final int ALGORITHM = 28;

  @Autowired private CheckAlgorithm028 algorithm;
  @Autowired private CheckWikiConfigurationRepository checkWikiRepository;

  @BeforeEach
  void setUp() {
    givenConfiguration(Map.of());
  }

  @DisplayName("should report a table without end")
  @Test
  void reportTableWithoutEnd() {
    // GIVEN
    final String text = "Texte\n{| class=\"wikitable\"\n|a\n|b\n";

    // WHEN
    final List<CheckError> errors = analyze(text);

    // THEN
    Assertions.assertThat(errors).containsExactly(new CheckError(ALGORITHM, 6, 8, List.of()));
  }

  @DisplayName("should not report tables with an end")
  @ParameterizedTest
  @ValueSource(
      strings = {
        "{|\n|a\n|}",
        "{|\n|a\n{|\n|b\n|}\n|}",
        "<table><tr><td>a</td></tr></table>",
        "{|\n|a\n</table>",
        "<table><tr><td>a</td></tr>\n|}",
        "{{{|safesubst:}}}Modèle",
        "Texte sans tableau"
      })
  void ignoreClosedTables(final String text) {
    // WHEN
    final List<CheckError> errors = analyze(text);

    // THEN
    Assertions.assertThat(errors).isEmpty();
  }

  @DisplayName("should report the outer table when only the inner table has an end")
  @Test
  void reportOuterTable() {
    // GIVEN
    final String text = "{|\n|a\n{|\n|b\n|}\n";

    // WHEN
    final List<CheckError> errors = analyze(text);

    // THEN
    Assertions.assertThat(errors).containsExactly(new CheckError(ALGORITHM, 0, 2, List.of()));
  }

  @DisplayName("should report a table start after the table end")
  @Test
  void reportStartAfterEnd() {
    // GIVEN
    final String text = "|}\n{|\n|a\n";

    // WHEN
    final List<CheckError> errors = analyze(text);

    // THEN
    Assertions.assertThat(errors).containsExactly(new CheckError(ALGORITHM, 3, 5, List.of()));
  }

  @DisplayName("should report a table start with an end right after it")
  @Test
  void reportStartFollowedByEnd() {
    // WHEN
    final List<CheckError> errors = analyze("{||}");

    // THEN
    Assertions.assertThat(errors).containsExactly(new CheckError(ALGORITHM, 0, 2, List.of()));
  }

  @DisplayName("should report an HTML table without end")
  @Test
  void reportHtmlTableWithoutEnd() {
    // GIVEN
    final String text = "Texte <table class=\"x\"><tr><td>a</td></tr>";

    // WHEN
    final List<CheckError> errors = analyze(text);

    // THEN
    Assertions.assertThat(errors).containsExactly(new CheckError(ALGORITHM, 6, 23, List.of()));
  }

  @DisplayName("should report table starts in the order of the text")
  @Test
  void reportStartsInTextOrder() {
    // GIVEN
    final String text = "<table><tr><td>a</td></tr>\n{|\n|b\n";

    // WHEN
    final List<CheckError> errors = analyze(text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(
            new CheckError(ALGORITHM, 0, 7, List.of()),
            new CheckError(ALGORITHM, 27, 29, List.of()));
  }

  @DisplayName("should not count the end of templates, functions and parameters as a table end")
  @ParameterizedTest
  @ValueSource(strings = {"{|\n|{{Modèle|}}\n", "{|\n|{{#if:a|}}\n", "{|\n|{{{1|}}}\n"})
  void ignoreTemplateClosing(final String text) {
    // WHEN
    final List<CheckError> errors = analyze(text);

    // THEN
    Assertions.assertThat(errors).containsExactly(new CheckError(ALGORITHM, 0, 2, List.of()));
  }

  @DisplayName("should ignore table starts in comments, code and non wiki tags")
  @ParameterizedTest
  @ValueSource(
      strings = {
        "<!-- {| -->",
        "<nowiki>{|</nowiki>",
        "<code>{|</code>",
        "<pre>{|</pre>",
        "<math>{|</math>",
        "<syntaxhighlight lang=\"text\">{|</syntaxhighlight>",
        "<nowiki><table></nowiki>",
        "<code><table></code>"
      })
  void ignoreStartsInExcludedAreas(final String text) {
    // WHEN
    final List<CheckError> errors = analyze(text);

    // THEN
    Assertions.assertThat(errors).isEmpty();
  }

  @DisplayName("should ignore table ends in comments, code and non wiki tags")
  @ParameterizedTest
  @ValueSource(
      strings = {
        "{|\n|a\n<!-- |} -->",
        "{|\n|a\n<nowiki>|}</nowiki>",
        "{|\n|a\n<code>|}</code>",
        "{|\n|a\n<code></table></code>"
      })
  void ignoreEndsInExcludedAreas(final String text) {
    // WHEN
    final List<CheckError> errors = analyze(text);

    // THEN
    Assertions.assertThat(errors).containsExactly(new CheckError(ALGORITHM, 0, 2, List.of()));
  }

  @DisplayName("should accept templates ending a table from the legacy configuration")
  @Test
  void endTablesWithLegacyTemplates() {
    // GIVEN
    givenConfiguration(
        Map.of(
            "error_028_templates_frwiki",
            "{{Fin}}\n{{Fin box @@HasNewline@@\n{{End}} @@HasNewline@@"));
    final String text = "{|\n|a\n{{fin}}\n{|\n|b\n{{Fin box}}\n{|\n|c\n{{End}}\n{|\n|d\n{{Autre}}";

    // WHEN
    final List<CheckError> errors = analyze(text);

    // THEN
    final int lastTable = text.lastIndexOf("{|");
    Assertions.assertThat(errors)
        .containsExactly(new CheckError(ALGORITHM, lastTable, lastTable + 2, List.of()));
  }

  @DisplayName("should accept templates ending a table from the YAML configuration only")
  @Test
  void endTablesWithYamlTemplates() {
    // GIVEN
    checkWikiRepository.setConfiguration(
        PageAnalysisTestFactory.WIKI,
        CheckWikiConfiguration.of(
                Map.of("error_028_prio_frwiki", "1", "error_028_templates_frwiki", "Autre"),
                Map.of(),
                "frwiki")
            .withParameters(
                Map.of(ALGORITHM, CheckAlgorithmParameters.parse("templates:\n  - Fin"))));
    final String text = "{|\n|a\n{{Fin}}\n{|\n|b\n{{Autre}}";

    // WHEN
    final List<CheckError> errors = analyze(text);

    // THEN
    final int lastTable = text.lastIndexOf("{|");
    Assertions.assertThat(errors)
        .containsExactly(new CheckError(ALGORITHM, lastTable, lastTable + 2, List.of()));
  }

  private List<CheckError> analyze(final String text) {
    return algorithm.analyze(PageAnalysisTestFactory.createPageAnalysis("Paris", text));
  }

  private void givenConfiguration(final Map<String, String> checkWikiProperties) {
    checkWikiRepository.setConfiguration(
        PageAnalysisTestFactory.WIKI,
        CheckWikiConfiguration.of(checkWikiProperties, Map.of(), "frwiki"));
  }
}
