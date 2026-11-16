package org.wpcleaner.lib.check.algorithm.a0xx.a00x.a009;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.wpcleaner.api.analysis.PageAnalysisTestFactory;
import org.wpcleaner.lib.check.CheckError;
import org.wpcleaner.lib.check.CheckSpringBootTest;
import org.wpcleaner.lib.check.Replacement;
import org.wpcleaner.lib.check.configuration.CheckAlgorithmParameters;
import org.wpcleaner.lib.check.configuration.CheckWikiConfiguration;
import org.wpcleaner.lib.check.configuration.CheckWikiConfigurationRepository;

@CheckSpringBootTest
class CheckAlgorithm009Test {

  private static final int ALGORITHM = 9;
  private static final String PARIS = "[[Catégorie:Paris]]";
  private static final String LYON = "[[Catégorie:Lyon|clé]]";
  private static final String NICE = "[[catégorie:Nice]]";
  private static final String TWO_CATEGORIES = PARIS + " " + LYON;
  private static final String TWO_LINES = PARIS + "\n" + LYON;
  private static final String TWO_LABEL = "[[...]]↵[[...]]";
  private static final String MORE_LABEL = "[[...]]↵...↵[[...]]";

  @Autowired private CheckAlgorithm009 algorithm;
  @Autowired private CheckWikiConfigurationRepository checkWikiRepository;

  @BeforeEach
  void setUp() {
    givenLegacyConfiguration(Map.of());
  }

  @DisplayName("should automatically put two categories of the same line on separate lines")
  @Test
  void separateTwoCategories() {
    // GIVEN
    final String text = "Texte.\n" + TWO_CATEGORIES + "\nFin";

    // WHEN
    final List<CheckError> errors = analyze(text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(error(text, TWO_CATEGORIES, TWO_LINES, TWO_LABEL, true));
  }

  @DisplayName("should put each category of a line on its own line, whatever the spaces")
  @Test
  void separateSeveralCategories() {
    // GIVEN
    final String categories = PARIS + LYON + "   " + NICE;
    final String text = "Texte.\n\n" + categories + " \n";

    // WHEN
    final List<CheckError> errors = analyze(text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(error(text, categories, TWO_LINES + "\n" + NICE, MORE_LABEL, true));
  }

  @DisplayName("should report each line with several categories")
  @Test
  void reportEachLine() {
    // GIVEN
    final String secondLine = NICE + " " + PARIS;
    final String text = TWO_CATEGORIES + "\n{{Portail}} " + secondLine;

    // WHEN
    final List<CheckError> errors = analyze(text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(
            error(text, TWO_CATEGORIES, TWO_LINES, TWO_LABEL, true),
            error(text, secondLine, NICE + "\n" + PARIS, TWO_LABEL, true));
  }

  @DisplayName("should not report categories on separate lines")
  @Test
  void ignoreSeparateLines() {
    // GIVEN
    final String text = "Texte " + PARIS + " texte\n" + LYON + "<!--\n-->" + NICE + "\n\n" + PARIS;

    // WHEN
    final List<CheckError> errors = analyze(text);

    // THEN
    Assertions.assertThat(errors).isEmpty();
  }

  @DisplayName("should keep the text between categories and fix manually")
  @Test
  void keepTextBetweenCategories() {
    // GIVEN
    final String categories = PARIS + " texte <!-- c -->" + LYON + " " + NICE;
    final String text = "Texte " + categories + " fin";

    // WHEN
    final List<CheckError> errors = analyze(text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(
            error(
                text,
                categories,
                PARIS + " texte <!-- c -->\n" + LYON + "\n" + NICE,
                MORE_LABEL,
                false));
  }

  @DisplayName("should fix manually categories inside a template or a function")
  @Test
  void fixManuallyInsideTemplateOrFunction() {
    // GIVEN
    final String text = "{{Modèle|" + TWO_CATEGORIES + "}}\n{{#si:x|" + NICE + " " + PARIS + "}}";

    // WHEN
    final List<CheckError> errors = analyze(text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(
            error(text, TWO_CATEGORIES, TWO_LINES, TWO_LABEL, false),
            error(text, NICE + " " + PARIS, NICE + "\n" + PARIS, TWO_LABEL, false));
  }

  @DisplayName("should add an empty line between a template and the first categories")
  @Test
  void addEmptyLineAfterTemplate() {
    // GIVEN
    final String sameLine = "Texte\n{{Portail|x}}" + " " + TWO_CATEGORIES;
    final String nextLine = "Texte\n{{Portail|x}}" + " \n" + TWO_CATEGORIES;

    // WHEN
    final List<CheckError> sameLineErrors = analyze(sameLine);
    final List<CheckError> nextLineErrors = analyze(nextLine);

    // THEN
    Assertions.assertThat(sameLineErrors)
        .containsExactly(
            error(sameLine, " " + TWO_CATEGORIES, "\n\n" + TWO_LINES, TWO_LABEL, true));
    Assertions.assertThat(nextLineErrors)
        .containsExactly(
            error(nextLine, " \n" + TWO_CATEGORIES, "\n\n" + TWO_LINES, TWO_LABEL, true));
  }

  @DisplayName("should put the default sort key on its own line before the first categories")
  @Test
  void separateDefaultSort() {
    // GIVEN
    final String defaultSort = "{{DEFAULTSORT:Clé}}";
    final String afterText = "Texte. \n" + defaultSort + " " + TWO_CATEGORIES;
    final String pageBegin = defaultSort + TWO_CATEGORIES;

    // WHEN
    final List<CheckError> afterTextErrors = analyze(afterText);
    final List<CheckError> pageBeginErrors = analyze(pageBegin);

    // THEN
    Assertions.assertThat(afterTextErrors)
        .containsExactly(
            error(
                afterText,
                " \n" + defaultSort + " " + TWO_CATEGORIES,
                "\n\n" + defaultSort + "\n" + TWO_LINES,
                TWO_LABEL,
                true));
    Assertions.assertThat(pageBeginErrors)
        .containsExactly(
            error(pageBegin, pageBegin, defaultSort + "\n" + TWO_LINES, TWO_LABEL, true));
  }

  @DisplayName("should start a new line between another function and the first categories")
  @Test
  void addLineBreakAfterFunction() {
    // GIVEN
    final String text = "{{lc:ABC}} " + TWO_CATEGORIES;

    // WHEN
    final List<CheckError> errors = analyze(text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(error(text, " " + TWO_CATEGORIES, "\n" + TWO_LINES, TWO_LABEL, true));
  }

  @DisplayName("should add an empty line between a redirect and categories on its line")
  @Test
  void addEmptyLineAfterRedirect() {
    // GIVEN
    final String sameLine = "#REDIRECTION [[Paris]] " + TWO_CATEGORIES;
    final String nextLine = "#REDIRECTION [[Paris]]\n" + TWO_CATEGORIES;

    // WHEN
    final List<CheckError> sameLineErrors = analyze(sameLine);
    final List<CheckError> nextLineErrors = analyze(nextLine);

    // THEN
    Assertions.assertThat(sameLineErrors)
        .containsExactly(
            error(sameLine, " " + TWO_CATEGORIES, "\n\n" + TWO_LINES, TWO_LABEL, true));
    Assertions.assertThat(nextLineErrors)
        .containsExactly(error(nextLine, TWO_CATEGORIES, TWO_LINES, TWO_LABEL, true));
  }

  @DisplayName("should keep only the text before the first categories of the page")
  @Test
  void keepTextBeforeOtherCategories() {
    // GIVEN
    final String text = NICE + "\n{{Portail}} " + TWO_CATEGORIES + "\nTexte " + TWO_CATEGORIES;

    // WHEN
    final List<CheckError> errors = analyze(text);

    // THEN
    final int second = text.lastIndexOf(TWO_CATEGORIES);
    Assertions.assertThat(errors)
        .containsExactly(
            error(text, TWO_CATEGORIES, TWO_LINES, TWO_LABEL, true),
            new CheckError(
                ALGORITHM,
                second,
                second + TWO_CATEGORIES.length(),
                List.of(new Replacement(TWO_LINES, TWO_LABEL, true))));
  }

  @DisplayName("should not count a redirect to a category as a category of the line")
  @Test
  void ignoreRedirectToCategory() {
    // GIVEN
    final String text = "#REDIRECTION " + PARIS + " " + NICE;

    // WHEN
    final List<CheckError> errors = analyze(text);

    // THEN
    Assertions.assertThat(errors).isEmpty();
  }

  @DisplayName(
      "should ignore categories in the templates and parameters given by legacy properties")
  @Test
  void ignoreTemplatesFromLegacy() {
    // GIVEN
    givenLegacyConfiguration(
        Map.of("error_009_ignore_templates_frwiki", "Suivi\nSuivi catégorie|catégorie|2"));

    // WHEN
    final String text = createTextWithTemplates();
    final List<CheckError> errors = analyze(text);

    // THEN
    Assertions.assertThat(errors)
        .extracting(CheckError::begin)
        .containsExactly(
            text.indexOf(TWO_CATEGORIES, text.indexOf("{{Autre|")),
            text.lastIndexOf(TWO_CATEGORIES));
  }

  @DisplayName("should ignore categories in the templates and parameters given by YAML")
  @Test
  void ignoreTemplatesFromYaml() {
    // GIVEN
    final String yaml =
        """
        ignore_templates:
          - template: Suivi
        ignore_parameters:
          - template: Suivi catégorie
            parameters: [catégorie, 2]
        """;
    checkWikiRepository.setConfiguration(
        PageAnalysisTestFactory.WIKI,
        CheckWikiConfiguration.of(Map.of("error_009_prio_frwiki", "3"), Map.of(), "frwiki")
            .withParameters(Map.of(ALGORITHM, CheckAlgorithmParameters.parse(yaml))));

    // WHEN
    final String text = createTextWithTemplates();
    final List<CheckError> errors = analyze(text);

    // THEN
    Assertions.assertThat(errors)
        .extracting(CheckError::begin)
        .containsExactly(
            text.indexOf(TWO_CATEGORIES, text.indexOf("{{Autre|")),
            text.lastIndexOf(TWO_CATEGORIES));
  }

  @DisplayName("should use only the YAML configuration when it exists")
  @Test
  void ignoreLegacyWithYaml() {
    // GIVEN
    checkWikiRepository.setConfiguration(
        PageAnalysisTestFactory.WIKI,
        CheckWikiConfiguration.of(
                Map.of("error_009_prio_frwiki", "3", "error_009_ignore_templates_frwiki", "Suivi"),
                Map.of(),
                "frwiki")
            .withParameters(Map.of(ALGORITHM, CheckAlgorithmParameters.parse("imagemap: true"))));

    // WHEN
    final List<CheckError> errors = analyze("{{Suivi|" + TWO_CATEGORIES + "}}");

    // THEN
    Assertions.assertThat(errors).hasSize(1);
  }

  private static String createTextWithTemplates() {
    return "{{Suivi|"
        + TWO_CATEGORIES
        + "}}\n{{Suivi catégorie|catégorie="
        + TWO_CATEGORIES
        + "}}\n{{Suivi catégorie|x|"
        + TWO_CATEGORIES
        + "}}\n{{Autre|"
        + TWO_CATEGORIES
        + "}}\n{{Autre|{{Suivi|"
        + TWO_CATEGORIES
        + "}}}}\n{{Suivi catégorie|catégorie=x|autre="
        + TWO_CATEGORIES
        + "}}";
  }

  private List<CheckError> analyze(final String text) {
    return algorithm.analyze(PageAnalysisTestFactory.createPageAnalysis("Lyon", text));
  }

  private static CheckError error(
      final String text,
      final String reported,
      final String replacement,
      final String label,
      final boolean automatic) {
    final int begin = text.indexOf(reported);
    return new CheckError(
        ALGORITHM,
        begin,
        begin + reported.length(),
        List.of(new Replacement(replacement, label, automatic)));
  }

  private void givenLegacyConfiguration(final Map<String, String> properties) {
    checkWikiRepository.setConfiguration(
        PageAnalysisTestFactory.WIKI, CheckWikiConfiguration.of(properties, Map.of(), "frwiki"));
  }
}
