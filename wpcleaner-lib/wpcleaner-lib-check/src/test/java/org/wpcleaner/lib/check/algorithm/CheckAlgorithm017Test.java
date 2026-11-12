package org.wpcleaner.lib.check.algorithm;

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
class CheckAlgorithm017Test {

  private static final int ALGORITHM = 17;
  private static final String PARIS = "[[Catégorie:Paris]]";

  @Autowired private CheckAlgorithm017 algorithm;
  @Autowired private CheckWikiConfigurationRepository checkWikiRepository;

  @BeforeEach
  void setUp() {
    givenLegacyConfiguration(Map.of());
  }

  @DisplayName("should automatically delete the second occurrence of a category on its own line")
  @Test
  void deleteDuplicateCategory() {
    // GIVEN
    final String text = "Texte.\n" + PARIS + "\n[[Catégorie:Lyon]]\n  [[catégorie:paris]] \nFin";

    // WHEN
    final List<CheckError> errors = analyze(text);

    // THEN
    final int duplicate = text.indexOf("\n  [[catégorie") + 1;
    Assertions.assertThat(errors)
        .containsExactly(
            new CheckError(
                ALGORITHM, duplicate, text.indexOf("Fin"), List.of(new Replacement("", true))));
  }

  @DisplayName("should delete the spaces before a duplicate category at the beginning of a line")
  @Test
  void deleteSpacesAtLineBeginning() {
    // GIVEN
    final String text = PARIS + "\n  " + PARIS + " texte";

    // WHEN
    final List<CheckError> errors = analyze(text);

    // THEN
    final int lineBegin = PARIS.length() + 1;
    Assertions.assertThat(errors)
        .extracting(CheckError::begin, CheckError::end)
        .containsExactly(Assertions.tuple(lineBegin, lineBegin + 2 + PARIS.length()));
  }

  @DisplayName("should not report categories present only once")
  @Test
  void ignoreSingleCategories() {
    // GIVEN
    final String text = PARIS + " [[Catégorie:Lyon]] [[Paris]]";

    // WHEN
    final List<CheckError> errors = analyze(text);

    // THEN
    Assertions.assertThat(errors).isEmpty();
  }

  @DisplayName("should keep the category after the last title, preferably with a sort key")
  @Test
  void keepCategoryAfterLastTitle() {
    // GIVEN
    final String text =
        "[[Catégorie:Paris|Clé]] Texte.\n== Titre ==\n" + PARIS + " [[Catégorie:Paris|Clé]]";

    // WHEN
    final List<CheckError> errors = analyze(text);

    // THEN
    final int second = text.indexOf(PARIS);
    Assertions.assertThat(errors)
        .containsExactly(
            new CheckError(ALGORITHM, 0, "[[Catégorie:Paris|Clé]]".length(), deletion(false)),
            new CheckError(ALGORITHM, second, second + PARIS.length(), deletion(false)));
  }

  @DisplayName(
      "should keep the first category, or before titles the one with a sort key or the last")
  @Test
  void keepFirstCategoryOrBeforeTitles() {
    // GIVEN
    final String withoutTitle = PARIS + " " + PARIS;
    final String withSortKey = "[[Catégorie:Paris|Clé]] " + PARIS + "\n== Titre ==";
    final String withoutSortKey = PARIS + " " + PARIS + "\n== Titre ==";

    // WHEN
    final List<CheckError> errorsWithoutTitle = analyze(withoutTitle);
    final List<CheckError> errorsWithSortKey = analyze(withSortKey);
    final List<CheckError> errorsWithoutSortKey = analyze(withoutSortKey);

    // THEN
    Assertions.assertThat(errorsWithoutTitle)
        .containsExactly(
            new CheckError(ALGORITHM, PARIS.length() + 1, withoutTitle.length(), deletion(true)));
    final int withSortKeyBegin = withSortKey.indexOf(PARIS);
    Assertions.assertThat(errorsWithSortKey)
        .containsExactly(
            new CheckError(
                ALGORITHM, withSortKeyBegin, withSortKeyBegin + PARIS.length(), deletion(false)));
    Assertions.assertThat(errorsWithoutSortKey)
        .containsExactly(new CheckError(ALGORITHM, 0, PARIS.length(), deletion(false)));
  }

  @DisplayName("should fix manually when text or a different sort key is between the categories")
  @Test
  void fixManually() {
    // GIVEN
    final String text =
        "== Titre ==\n"
            + PARIS
            + " Texte "
            + PARIS
            + "\n[[Catégorie:Lyon|A]] [[Catégorie:Lyon|B]]"
            + "\n{{#if:x|[[Catégorie:Nice]] [[Catégorie:Nice]]}}";

    // WHEN
    final List<CheckError> errors = analyze(text);

    // THEN
    Assertions.assertThat(errors)
        .extracting(CheckError::replacements)
        .containsOnly(deletion(false))
        .hasSize(3);
  }

  @DisplayName("should not count the target of a redirect")
  @Test
  void ignoreRedirectTarget() {
    // GIVEN
    final String text = "#REDIRECT " + PARIS + "\n" + PARIS;

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
        Map.of("error_017_ignore_templates_frwiki", "Suivi\nSuivi catégorie|catégorie|2"));

    // WHEN
    final String text = createTextWithTemplates();
    final List<CheckError> errors = analyze(text);

    // THEN
    Assertions.assertThat(errors)
        .extracting(CheckError::begin)
        .containsExactly(text.lastIndexOf(PARIS));
  }

  @DisplayName("should ignore categories in the templates and parameters given by YAML")
  @Test
  void ignoreTemplatesFromYaml() {
    // GIVEN
    givenYamlConfiguration(
        """
        ignore_templates:
          - template: Suivi
        ignore_parameters:
          - template: Suivi catégorie
            parameters: [catégorie, 2]
        """);

    // WHEN
    final String text = createTextWithTemplates();
    final List<CheckError> errors = analyze(text);

    // THEN
    Assertions.assertThat(errors)
        .extracting(CheckError::begin)
        .containsExactly(text.lastIndexOf(PARIS));
  }

  @DisplayName("should use only the YAML configuration when it exists")
  @Test
  void ignoreLegacyWithYaml() {
    // GIVEN
    checkWikiRepository.setConfiguration(
        PageAnalysisTestFactory.WIKI,
        CheckWikiConfiguration.of(
                Map.of("error_017_prio_frwiki", "3", "error_017_ignore_templates_frwiki", "Suivi"),
                Map.of(),
                "frwiki")
            .withParameters(Map.of(ALGORITHM, CheckAlgorithmParameters.parse("imagemap: true"))));

    // WHEN
    final List<CheckError> errors = analyze(PARIS + " {{Suivi|" + PARIS + "}}");

    // THEN
    Assertions.assertThat(errors).hasSize(1);
  }

  private static String createTextWithTemplates() {
    return PARIS
        + " {{Suivi|"
        + PARIS
        + "}} {{Suivi catégorie|catégorie="
        + PARIS
        + "|x|"
        + PARIS
        + "}} {{Autre|{{Suivi|"
        + PARIS
        + "}}}} {{Suivi catégorie|autre="
        + PARIS
        + "}}";
  }

  private List<CheckError> analyze(final String text) {
    return algorithm.analyze(PageAnalysisTestFactory.createPageAnalysis("Lyon", text));
  }

  private static List<Replacement> deletion(final boolean automatic) {
    return List.of(new Replacement("", automatic));
  }

  private void givenLegacyConfiguration(final Map<String, String> properties) {
    checkWikiRepository.setConfiguration(
        PageAnalysisTestFactory.WIKI, CheckWikiConfiguration.of(properties, Map.of(), "frwiki"));
  }

  private void givenYamlConfiguration(final String yaml) {
    checkWikiRepository.setConfiguration(
        PageAnalysisTestFactory.WIKI,
        CheckWikiConfiguration.of(Map.of("error_017_prio_frwiki", "3"), Map.of(), "frwiki")
            .withParameters(Map.of(ALGORITHM, CheckAlgorithmParameters.parse(yaml))));
  }
}
