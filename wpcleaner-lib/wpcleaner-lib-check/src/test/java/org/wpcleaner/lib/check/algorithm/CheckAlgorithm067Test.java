package org.wpcleaner.lib.check.algorithm;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.wpcleaner.api.analysis.PageAnalysisTestFactory;
import org.wpcleaner.api.repository.configuration.WPCleanerConfiguration;
import org.wpcleaner.api.repository.configuration.WPCleanerConfigurationRepository;
import org.wpcleaner.lib.check.CheckError;
import org.wpcleaner.lib.check.CheckSpringBootTest;
import org.wpcleaner.lib.check.Replacement;
import org.wpcleaner.lib.check.configuration.CheckAlgorithmParameters;
import org.wpcleaner.lib.check.configuration.CheckWikiConfiguration;
import org.wpcleaner.lib.check.configuration.CheckWikiConfigurationRepository;

@CheckSpringBootTest
class CheckAlgorithm067Test {

  private static final int ALGORITHM = 67;
  private static final String REF_A = "<ref>A</ref>";
  private static final String REF_B = "<ref name=\"b\" />";
  private static final String COMMENT = "<!-- c -->";
  private static final String SHORT_REFERENCE = "<ref>...</ref>";

  @Autowired private CheckAlgorithm067 algorithm;
  @Autowired private CheckWikiConfigurationRepository checkWikiRepository;
  @Autowired private WPCleanerConfigurationRepository wpcleanerRepository;

  @BeforeEach
  void setUp() {
    givenLegacyConfiguration(Map.of());
    wpcleanerRepository.setConfiguration(
        PageAnalysisTestFactory.WIKI, WPCleanerConfiguration.EMPTY);
  }

  @DisplayName("should move the punctuation before references after them")
  @Test
  void fixPunctuationBefore() {
    // GIVEN
    final String text =
        String.join(
            "\n",
            "Upper." + REF_A + " Suite",
            "lower," + REF_A + " suite",
            "other," + REF_A + " Suite",
            "spaces :" + REF_A + " suite",
            "spaced ." + REF_A + " Suite",
            "line :" + REF_A,
            "dots..." + REF_A,
            "comment. <!-- c -->" + REF_A + " Suite",
            "group." + REF_A + ", " + REF_B + " Suite",
            "link." + REF_A + " [[Paris]]",
            "tag." + REF_A + "<br />",
            "quote." + REF_A + " » suite",
            "italic." + REF_A + " ''Suite''",
            "image." + REF_A + "[[File:Lyon.png]]",
            "[[File:Lyon.png|thumb|Caption." + REF_A + "]]",
            "blockquote." + REF_A + "<blockquote>Suite</blockquote>",
            "gallery." + REF_A + "<gallery>File:Lyon.png</gallery>",
            "last." + REF_A);

    // WHEN
    final List<CheckError> errors = analyze("Lyon", text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(
            error(text, "." + REF_A, "Upper", replacement(REF_A + ".", true)),
            error(text, "," + REF_A, "lower", replacement(REF_A + ",", true)),
            error(text, "," + REF_A, "other", replacement(REF_A + ",", false)),
            error(text, " :" + REF_A, "spaces", replacement(REF_A + " :", false)),
            error(text, " ." + REF_A, "spaced", replacement(REF_A + ".", true)),
            error(text, " :" + REF_A, "line", replacement(REF_A + " :", true)),
            error(text, "..." + REF_A, "dots", replacement(REF_A + "...", true)),
            error(
                text,
                ". <!-- c -->" + REF_A,
                "comment",
                replacement("<!-- c -->" + REF_A + ".", true)),
            error(
                text, "." + REF_A + ", " + REF_B, "group", replacement(REF_A + REF_B + ".", true)),
            error(text, "." + REF_A, "link", replacement(REF_A + ".", true)),
            error(text, "." + REF_A, "tag", replacement(REF_A + ".", true)),
            error(text, "." + REF_A, "quote", replacement(REF_A + ".", true)),
            error(text, "." + REF_A, "italic", replacement(REF_A + ".", true)),
            error(text, "." + REF_A, "image", replacement(REF_A + ".", true)),
            error(text, "." + REF_A, "Caption", replacement(REF_A + ".", true)),
            error(text, "." + REF_A, "blockquote", replacement(REF_A + ".", true)),
            error(text, "." + REF_A, "gallery", replacement(REF_A + ".", true)),
            error(text, "." + REF_A, "last", replacement(REF_A + ".", true)));
  }

  @DisplayName("should handle the punctuation after references")
  @Test
  void handlePunctuationAfter() {
    // GIVEN
    final String text =
        String.join(
            "\n",
            "same." + REF_A + ". Suite",
            "comma," + REF_A + ", suite",
            "colon:" + REF_A + ": suite",
            "other," + REF_A + ". Suite",
            "between" + REF_A + "," + REF_B + " suite");

    // WHEN
    final List<CheckError> errors = analyze("Lyon", text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(
            error(text, "." + REF_A + ".", "same", replacement(REF_A + ".", true)),
            error(text, "," + REF_A + ",", "comma", replacement(REF_A + ",", true)),
            error(text, ":" + REF_A + ":", "colon", replacement(REF_A + ":", true)),
            error(
                text,
                "," + REF_A + ".",
                "other",
                replacement(REF_A + ",", false),
                replacement(REF_A + ".", false)),
            error(text, REF_A + "," + REF_B, "between", replacement(REF_A + REF_B, false)));
  }

  @DisplayName("should not report entities, definitions and references without punctuation")
  @Test
  void ignoreOtherCases() {
    // GIVEN
    final String text =
        String.join(
            "\n",
            "Space&nbsp;" + REF_A,
            "Decimal&#160;" + REF_A,
            "Hexadecimal&#xA0;" + REF_A,
            ";" + REF_A,
            "None" + REF_A + ". Suite",
            "<nowiki>Text." + REF_A + "</nowiki>");

    // WHEN
    final List<CheckError> errors = analyze("Lyon", text);

    // THEN
    Assertions.assertThat(errors).isEmpty();
  }

  @DisplayName("should not fix automatically in talk and user pages")
  @Test
  void fixManuallyOutsideArticles() {
    // GIVEN
    final String text = "Upper." + REF_A + " Suite";

    // WHEN
    final List<CheckError> talkErrors = analyze("Discussion utilisateur:Lyon", text);
    final List<CheckError> userErrors = analyze("Utilisateur:Lyon", text);

    // THEN
    final CheckError expected = error(text, "." + REF_A, "Upper", replacement(REF_A + ".", false));
    Assertions.assertThat(talkErrors).containsExactly(expected);
    Assertions.assertThat(userErrors).containsExactly(expected);
  }

  @DisplayName("should use the parameters, with the legacy configuration")
  @Test
  void useParametersWithLegacyConfiguration() {
    // GIVEN
    givenLegacyConfiguration(
        Map.of(
            "error_067_abbreviations_frwiki",
            "etc.\n  et al.",
            "error_067_safe_templates_frwiki",
            "Citation|1\n  Note|texte",
            "error_067_separator_frwiki",
            "{{,}}"));
    final String text =
        String.join(
            "\n",
            "Text etc." + REF_A,
            "Text et al." + REF_A,
            "Text tetc." + REF_A,
            "{{Citation|Text." + REF_A + "}}",
            "{{Note|texte=Text." + REF_A + "|suite}}",
            "{{Note|Text." + REF_A + "}}",
            "{{Citation|Pipe." + REF_A + "|suite}}",
            "{{Autre|{{Citation|Nested." + REF_A + "}}}}",
            "{{Note| texte =Trim." + REF_A + "}}",
            "Separator." + REF_A + "{{,}}" + REF_B + " Suite",
            "Double." + REF_A + "{{,}}{{,}}" + REF_B + " Suite");

    // WHEN
    final List<CheckError> errors = analyze("Lyon", text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(
            error(text, "." + REF_A, "Text tetc", replacement(REF_A + ".", true)),
            error(text, "." + REF_A, "{{Citation|Text", replacement(REF_A + ".", true)),
            error(text, "." + REF_A, "{{Note|texte=Text", replacement(REF_A + ".", true)),
            error(text, "." + REF_A, "{{Note|Text", replacement(REF_A + ".", false)),
            error(text, "." + REF_A, "Pipe", replacement(REF_A + ".", true)),
            error(text, "." + REF_A, "Nested", replacement(REF_A + ".", true)),
            error(text, "." + REF_A, "Trim", replacement(REF_A + ".", true)),
            error(
                text,
                "." + REF_A + "{{,}}" + REF_B,
                "Separator",
                replacement(REF_A + "{{,}}" + REF_B + ".", true)),
            error(text, "." + REF_A, "Double", replacement(REF_A + ".", false)));
  }

  @DisplayName("should use the parameters, with the YAML configuration")
  @Test
  void useParametersWithYamlConfiguration() {
    // GIVEN
    givenYamlConfiguration(
        """
        abbreviations:
          - etc.
        safe_templates:
          - template: Citation
            parameters: [1]
        separator: "{{,}}"
        """);
    final String text =
        String.join(
            "\n",
            "Text etc." + REF_A,
            "{{Citation|Text." + REF_A + "}}",
            "Separator." + REF_A + ", " + REF_B + " Suite");

    // WHEN
    final List<CheckError> errors = analyze("Lyon", text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(
            error(text, "." + REF_A, "{{Citation|Text", replacement(REF_A + ".", true)),
            error(
                text,
                "." + REF_A + ", " + REF_B,
                "Separator",
                replacement(REF_A + "{{,}}" + REF_B + ".", true)));
  }

  @DisplayName("should suggest the replacements of the general abbreviations")
  @Test
  void suggestGeneralAbbreviations() {
    // GIVEN
    wpcleanerRepository.setConfiguration(
        PageAnalysisTestFactory.WIKI,
        new WPCleanerConfiguration(
            Map.of(
                "general_abbreviations",
                """
                etc.|et cetera|{{abréviation discrète|etc.|et cetera|la}}
                etc.|et cætera|{{abréviation discrète|etc.|et cætera|la}}
                C.I.A. | Central Intelligence Agency | [[Central Intelligence Agency|C.I.A.]]
                J.-C.||{{J.-C.}}
                invalid.|no replacement
                |empty|{{empty}}
                """),
            Map.of("general_abbreviations", "al.|alii|{{et al.}}")));
    final String text =
        String.join(
            "\n",
            "etc." + REF_A + " suite",
            "Agency C.I.A." + REF_A + ". Suite",
            "Comment etc. <!-- c -->" + REF_A + " suite",
            "Authors al." + REF_A + " suite",
            "Christ J.-C." + REF_A + " suite",
            "Word tetc." + REF_A + " suite",
            "Text invalid." + REF_A + " suite");

    // WHEN
    final List<CheckError> errors = analyze("Lyon", text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(
            error(
                text,
                "etc." + REF_A,
                "",
                abbreviation("{{abréviation discrète|etc.|et cetera|la}}" + REF_A, "et cetera"),
                abbreviation("{{abréviation discrète|etc.|et cætera|la}}" + REF_A, "et cætera"),
                replacement("etc" + REF_A + ".", false)),
            error(
                text,
                "C.I.A." + REF_A + ".",
                "Agency ",
                abbreviation(
                    "[[Central Intelligence Agency|C.I.A.]]" + REF_A + ".",
                    "Central Intelligence Agency"),
                replacement("C.I.A" + REF_A + ".", true)),
            error(
                text,
                "etc. <!-- c -->" + REF_A,
                "Comment ",
                abbreviation(
                    "{{abréviation discrète|etc.|et cetera|la}}<!-- c -->" + REF_A, "et cetera"),
                abbreviation(
                    "{{abréviation discrète|etc.|et cætera|la}}<!-- c -->" + REF_A, "et cætera"),
                replacement("etc<!-- c -->" + REF_A + ".", false)),
            error(
                text,
                "al." + REF_A,
                "Authors ",
                abbreviation("{{et al.}}" + REF_A, "alii"),
                replacement("al" + REF_A + ".", false)),
            error(
                text,
                "J.-C." + REF_A,
                "Christ ",
                replacement("{{J.-C.}}" + REF_A, false),
                replacement("J.-C" + REF_A + ".", false)),
            error(text, "." + REF_A, "Word tetc", replacement(REF_A + ".", false)),
            error(text, "." + REF_A, "Text invalid", replacement(REF_A + ".", false)));
  }

  @DisplayName("should use only the YAML configuration when it exists")
  @Test
  void ignoreLegacyWithYaml() {
    // GIVEN
    checkWikiRepository.setConfiguration(
        PageAnalysisTestFactory.WIKI,
        CheckWikiConfiguration.of(
                Map.of("error_067_prio_frwiki", "2", "error_067_abbreviations_frwiki", "etc."),
                Map.of(),
                "frwiki")
            .withParameters(
                Map.of(ALGORITHM, CheckAlgorithmParameters.parse("abbreviations: [al.]"))));
    final String text = "Text etc." + REF_A;

    // WHEN
    final List<CheckError> errors = analyze("Lyon", text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(error(text, "." + REF_A, "Text etc", replacement(REF_A + ".", true)));
  }

  private List<CheckError> analyze(final String title, final String text) {
    return algorithm.analyze(PageAnalysisTestFactory.createPageAnalysis(title, text));
  }

  private static Replacement replacement(final String text, final boolean automatic) {
    return new Replacement(text, label(text), automatic);
  }

  private static Replacement abbreviation(final String text, final String meaning) {
    return new Replacement(text, label(text) + " (" + meaning + ")", false);
  }

  private static String label(final String text) {
    return text.replace(REF_A, SHORT_REFERENCE)
        .replace(REF_B, SHORT_REFERENCE)
        .replace(COMMENT, "");
  }

  private static CheckError error(
      final String text,
      final String reported,
      final String before,
      final Replacement... replacements) {
    final int begin = text.indexOf(before + reported) + before.length();
    return new CheckError(ALGORITHM, begin, begin + reported.length(), Arrays.asList(replacements));
  }

  private void givenLegacyConfiguration(final Map<String, String> parameters) {
    final Map<String, String> properties = new HashMap<>(parameters);
    properties.put("error_067_prio_frwiki", "2");
    checkWikiRepository.setConfiguration(
        PageAnalysisTestFactory.WIKI, CheckWikiConfiguration.of(properties, Map.of(), "frwiki"));
  }

  private void givenYamlConfiguration(final String yaml) {
    checkWikiRepository.setConfiguration(
        PageAnalysisTestFactory.WIKI,
        CheckWikiConfiguration.of(Map.of("error_067_prio_frwiki", "2"), Map.of(), "frwiki")
            .withParameters(Map.of(ALGORITHM, CheckAlgorithmParameters.parse(yaml))));
  }
}
