package org.wpcleaner.lib.check.algorithm.a0xx.a01x.a016;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.HashMap;
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
import org.wpcleaner.lib.check.Information;
import org.wpcleaner.lib.check.Replacement;
import org.wpcleaner.lib.check.configuration.CheckAlgorithmParameters;
import org.wpcleaner.lib.check.configuration.CheckWikiConfiguration;
import org.wpcleaner.lib.check.configuration.CheckWikiConfigurationRepository;

@CheckSpringBootTest
class CheckAlgorithm016Test {

  private static final int ALGORITHM = 16;
  private static final String TITLE = "Paris";
  private static final String REMOVE = "Remove all control characters";
  private static final String LEFT_TO_RIGHT_MARK = "‎";
  private static final String RIGHT_TO_LEFT_MARK = "‏";
  private static final String ZERO_WIDTH_SPACE = "​";
  private static final String NON_BREAKING_SPACE = " ";
  private static final String SOFT_HYPHEN = "­";
  private static final String CYRILLIC = "ж";
  private static final Information LEFT_TO_RIGHT_MARK_INFORMATION =
      new Information("U+200E – Left-to-right mark");
  private static final Information NON_BREAKING_SPACE_INFORMATION =
      new Information("U+00A0 – Non-breaking space");

  @Autowired private CheckAlgorithm016 algorithm;
  @Autowired private CheckWikiConfigurationRepository checkWikiRepository;

  @BeforeEach
  void setUp() {
    givenConfiguration(Map.of());
  }

  @DisplayName("should report control characters with the characters around them")
  @Test
  void reportControlCharacters() {
    // GIVEN
    final String text =
        LEFT_TO_RIGHT_MARK
            + "ab c"
            + LEFT_TO_RIGHT_MARK
            + RIGHT_TO_LEFT_MARK
            + "d e"
            + LEFT_TO_RIGHT_MARK
            + "f"
            + LEFT_TO_RIGHT_MARK;

    // WHEN
    final List<CheckError> errors = analyze(text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(
            new CheckError(
                ALGORITHM, 0, 2, List.of(LEFT_TO_RIGHT_MARK_INFORMATION, remove("a", true))),
            new CheckError(
                ALGORITHM,
                4,
                8,
                List.of(
                    LEFT_TO_RIGHT_MARK_INFORMATION,
                    new Information("U+200F – Right-to-left mark"),
                    remove("cd", true))),
            new CheckError(
                ALGORITHM, 9, 13, List.of(LEFT_TO_RIGHT_MARK_INFORMATION, remove("ef", true))));
  }

  @DisplayName("should report control characters outside the basic multilingual plane")
  @Test
  void reportSupplementaryControlCharacters() {
    // GIVEN
    final String text = "a" + Character.toString(0xF0000) + "b";

    // WHEN
    final List<CheckError> errors = analyze(text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(
            new CheckError(
                ALGORITHM, 0, 4, List.of(new Information("U+F0000 – Private use area A"))));
  }

  @DisplayName("should fix automatically only if characters around or control characters are safe")
  @Test
  void fixSafeCharactersAutomatically() {
    // GIVEN
    final String unsafe = CYRILLIC + LEFT_TO_RIGHT_MARK + CYRILLIC;
    final String safeCharacters = "a" + LEFT_TO_RIGHT_MARK + "b";
    final String zeroWidthSpaceAfterSpace = " " + ZERO_WIDTH_SPACE + CYRILLIC;
    final String zeroWidthSpaceBeforePipe = CYRILLIC + ZERO_WIDTH_SPACE + "|";
    final String zeroWidthSpaceInWord = CYRILLIC + ZERO_WIDTH_SPACE + CYRILLIC;

    // WHEN
    final List<CheckError> errors =
        analyze(
            String.join(
                "\n",
                unsafe,
                safeCharacters,
                zeroWidthSpaceAfterSpace,
                zeroWidthSpaceBeforePipe,
                zeroWidthSpaceInWord));

    // THEN
    Assertions.assertThat(errors)
        .extracting(CheckError::replacements)
        .containsExactly(
            List.of(remove(CYRILLIC + CYRILLIC, false)),
            List.of(remove("ab", true)),
            List.of(remove(" " + CYRILLIC, true)),
            List.of(remove(CYRILLIC + "|", true)),
            List.of(remove(CYRILLIC + CYRILLIC, false)));
  }

  @DisplayName("should keep non-breaking spaces except near French punctuation")
  @Test
  void keepNonBreakingSpaces() {
    // GIVEN
    final String text =
        "a" + NON_BREAKING_SPACE + "b «" + NON_BREAKING_SPACE + "c d" + NON_BREAKING_SPACE + ":";

    // WHEN
    final List<CheckError> errors = analyze(text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(
            new CheckError(
                ALGORITHM,
                0,
                3,
                List.of(
                    NON_BREAKING_SPACE_INFORMATION,
                    new Replacement("a b", false),
                    new Replacement("a&nbsp;b", false))),
            new CheckError(
                ALGORITHM, 4, 7, List.of(NON_BREAKING_SPACE_INFORMATION, remove("« c", true))),
            new CheckError(
                ALGORITHM, 8, 11, List.of(NON_BREAKING_SPACE_INFORMATION, remove("d :", true))));
  }

  @DisplayName("should not fix automatically non-breaking spaces kept among unsafe characters")
  @Test
  void fixManuallyKeptNonBreakingSpaces() {
    // GIVEN
    final String text = CYRILLIC + NON_BREAKING_SPACE + "«" + NON_BREAKING_SPACE + CYRILLIC;

    // WHEN
    final List<CheckError> errors = analyze(text);

    // THEN
    Assertions.assertThat(errors)
        .extracting(CheckError::replacements)
        .containsExactly(
            List.of(
                remove(CYRILLIC + NON_BREAKING_SPACE + "« " + CYRILLIC, false),
                new Replacement(CYRILLIC + " « " + CYRILLIC, false),
                new Replacement(CYRILLIC + "&nbsp;«&nbsp;" + CYRILLIC, false)));
  }

  @DisplayName("should suggest alternatives when the fix is not automatic")
  @Test
  void suggestAlternatives() {
    // GIVEN
    final String softHyphen = CYRILLIC + SOFT_HYPHEN + CYRILLIC;
    final String punctuationSpace = "a b";
    final String checkMark = "ab";

    // WHEN
    final List<CheckError> errors =
        analyze(String.join(" ", softHyphen, punctuationSpace, checkMark));

    // THEN
    Assertions.assertThat(errors)
        .extracting(CheckError::replacements)
        .containsExactly(
            List.of(
                remove(CYRILLIC + CYRILLIC, false),
                new Replacement(CYRILLIC + "&shy;" + CYRILLIC, false),
                new Replacement(CYRILLIC + "-" + CYRILLIC, false)),
            List.of(new Replacement("a b", false), new Replacement("ab", false)),
            List.of(new Replacement("a*b", false), new Replacement("a✓b", false)));
  }

  @DisplayName("should not fix automatically control characters also in the page title")
  @Test
  void fixManuallyControlCharactersInTitle() {
    // GIVEN
    final String text = "a" + LEFT_TO_RIGHT_MARK + "b c" + RIGHT_TO_LEFT_MARK + "d";

    // WHEN
    final List<CheckError> errors =
        algorithm.analyze(
            PageAnalysisTestFactory.createPageAnalysis(TITLE + LEFT_TO_RIGHT_MARK, text));

    // THEN
    Assertions.assertThat(errors)
        .extracting(CheckError::replacements)
        .containsExactly(List.of(remove("ab", false)), List.of(remove("cd", true)));
  }

  @DisplayName("should fix automatically in redirects only categories and some redirect targets")
  @Test
  void fixRedirectsCarefully() {
    // GIVEN
    final String zeroWidthSpace = "#REDIRECTION [[Pa" + ZERO_WIDTH_SPACE + "ris]]";
    final String softHyphen = "#REDIRECTION [[Pa" + SOFT_HYPHEN + "ris]]";
    final String otherText = "#REDIRECTION [[Paris]] a" + LEFT_TO_RIGHT_MARK + "b";
    final String category = "#REDIRECTION [[Paris]]\n[[Catégorie:Vi" + SOFT_HYPHEN + "lle]]";

    // WHEN
    final List<CheckError> zeroWidthSpaceErrors = analyze(zeroWidthSpace);
    final List<CheckError> softHyphenErrors = analyze(softHyphen);
    final List<CheckError> otherTextErrors = analyze(otherText);
    final List<CheckError> categoryErrors = analyze(category);

    // THEN
    Assertions.assertThat(zeroWidthSpaceErrors)
        .extracting(CheckError::replacements)
        .containsExactly(List.of(remove("ar", true)));
    Assertions.assertThat(softHyphenErrors)
        .extracting(CheckError::replacements)
        .containsExactly(
            List.of(
                remove("ar", false),
                new Replacement("a&shy;r", false),
                new Replacement("a-r", false)));
    Assertions.assertThat(otherTextErrors)
        .extracting(CheckError::replacements)
        .containsExactly(List.of(remove("ab", false)));
    Assertions.assertThat(categoryErrors)
        .extracting(CheckError::replacements)
        .containsExactly(List.of(remove("il", true)));
  }

  @DisplayName("should not fix automatically control characters in link anchors")
  @Test
  void fixManuallyLinkAnchors() {
    // GIVEN
    final String anchorWithText = "[[Paris#Hi" + LEFT_TO_RIGHT_MARK + "stoire|histoire]]";
    final String anchorWithoutText = "[[Paris#Hi" + LEFT_TO_RIGHT_MARK + "stoire]]";
    final String anchorEnd = "[[Paris#Histoire" + LEFT_TO_RIGHT_MARK + "|histoire]]";
    final String anchorEndWithoutText = "[[Paris#Histoire" + LEFT_TO_RIGHT_MARK + "]]";
    final String linkText = "[[Paris#Histoire|hi" + LEFT_TO_RIGHT_MARK + "stoire]]";
    final String targetWithoutAnchor = "[[Pa" + LEFT_TO_RIGHT_MARK + "ris]]";

    // WHEN
    final List<CheckError> errors =
        analyze(
            String.join(
                " ",
                anchorWithText,
                anchorWithoutText,
                anchorEnd,
                anchorEndWithoutText,
                linkText,
                targetWithoutAnchor));

    // THEN
    Assertions.assertThat(errors)
        .extracting(CheckError::replacements)
        .containsExactly(
            List.of(remove("is", false)),
            List.of(remove("is", false)),
            List.of(remove("e|", true)),
            List.of(remove("e]", true)),
            List.of(remove("is", true)),
            List.of(remove("ar", true)));
  }

  @DisplayName("should ignore control characters disabled in the legacy configuration")
  @Test
  void ignoreDisabledControlCharacters() {
    // GIVEN
    givenConfiguration(Map.of("error_016_use_200E_frwiki", "false"));
    final String text = "a" + LEFT_TO_RIGHT_MARK + "b c" + RIGHT_TO_LEFT_MARK + "d";

    // WHEN
    final List<CheckError> errors = analyze(text);

    // THEN
    Assertions.assertThat(errors).extracting(CheckError::begin).containsExactly(4);
  }

  @DisplayName("should ignore control characters disabled in the YAML configuration")
  @Test
  void ignoreDisabledControlCharactersFromYaml() {
    // GIVEN
    givenYamlConfiguration(Map.of(), "use_200F: false");
    final String text = "a" + LEFT_TO_RIGHT_MARK + "b c" + RIGHT_TO_LEFT_MARK + "d";

    // WHEN
    final List<CheckError> errors = analyze(text);

    // THEN
    Assertions.assertThat(errors).extracting(CheckError::begin).containsExactly(0);
  }

  @DisplayName("should report only in outermost templates with the legacy configuration")
  @Test
  void reportOnlyInTemplates() {
    // GIVEN
    givenConfiguration(Map.of("error_016_only_templates_frwiki", "true"));
    final String text =
        "a"
            + LEFT_TO_RIGHT_MARK
            + "b {{X|c"
            + LEFT_TO_RIGHT_MARK
            + "d|{{Y|e"
            + LEFT_TO_RIGHT_MARK
            + "f}}}} {{"
            + LEFT_TO_RIGHT_MARK
            + "Z}}";

    // WHEN
    final List<CheckError> errors = analyze(text);

    // THEN
    final int lastTemplate = text.lastIndexOf("{{");
    Assertions.assertThat(errors)
        .extracting(CheckError::begin)
        .containsExactly(
            text.indexOf("c" + LEFT_TO_RIGHT_MARK),
            text.indexOf("e" + LEFT_TO_RIGHT_MARK),
            lastTemplate + 1);
  }

  @DisplayName("should report only in templates with the YAML configuration")
  @Test
  void reportOnlyInTemplatesFromYaml() {
    // GIVEN
    givenYamlConfiguration(Map.of(), "only_templates: true");
    final String text = "a" + LEFT_TO_RIGHT_MARK + "b {{X|c" + LEFT_TO_RIGHT_MARK + "d}}";

    // WHEN
    final List<CheckError> errors = analyze(text);

    // THEN
    Assertions.assertThat(errors)
        .extracting(CheckError::begin)
        .containsExactly(text.indexOf("c" + LEFT_TO_RIGHT_MARK));
  }

  @DisplayName("should use only the YAML configuration when it exists")
  @Test
  void ignoreLegacyParametersWithYaml() {
    // GIVEN
    givenYamlConfiguration(
        Map.of(
            "error_016_only_templates_frwiki", "true",
            "error_016_use_200E_frwiki", "false"),
        "use_A0: false");
    final String text = "a" + LEFT_TO_RIGHT_MARK + "b c" + NON_BREAKING_SPACE + "d";

    // WHEN
    final List<CheckError> errors = analyze(text);

    // THEN
    Assertions.assertThat(errors).extracting(CheckError::begin).containsExactly(0);
  }

  private List<CheckError> analyze(final String text) {
    return algorithm.analyze(PageAnalysisTestFactory.createPageAnalysis(TITLE, text));
  }

  private void givenConfiguration(final Map<String, String> properties) {
    checkWikiRepository.setConfiguration(
        PageAnalysisTestFactory.WIKI,
        CheckWikiConfiguration.of(withPriority(properties), Map.of(), "frwiki"));
  }

  private void givenYamlConfiguration(final Map<String, String> properties, final String yaml) {
    checkWikiRepository.setConfiguration(
        PageAnalysisTestFactory.WIKI,
        CheckWikiConfiguration.of(withPriority(properties), Map.of(), "frwiki")
            .withParameters(Map.of(ALGORITHM, CheckAlgorithmParameters.parse(yaml))));
  }

  private static Map<String, String> withPriority(final Map<String, String> properties) {
    final Map<String, String> result = new HashMap<>(properties);
    result.put("error_016_prio_frwiki", "2");
    return Map.copyOf(result);
  }

  private static Replacement remove(final String text, final boolean automatic) {
    return new Replacement(text, REMOVE, automatic);
  }
}
