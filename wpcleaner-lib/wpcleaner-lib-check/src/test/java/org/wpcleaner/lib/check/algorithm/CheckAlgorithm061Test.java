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
import org.wpcleaner.lib.check.CheckError;
import org.wpcleaner.lib.check.CheckSpringBootTest;
import org.wpcleaner.lib.check.Replacement;
import org.wpcleaner.lib.check.configuration.CheckAlgorithmParameters;
import org.wpcleaner.lib.check.configuration.CheckWikiConfiguration;
import org.wpcleaner.lib.check.configuration.CheckWikiConfigurationRepository;

@CheckSpringBootTest
class CheckAlgorithm061Test {

  private static final int ALGORITHM = 61;
  private static final String REF_A = "<ref>A</ref>";
  private static final String REF_B = "<ref name=\"b\" />";
  private static final String TEMPLATE = "{{sfn|A}}";
  private static final String SHORT_REFERENCE = "<ref>...</ref>";

  @Autowired private CheckAlgorithm061 algorithm;
  @Autowired private CheckWikiConfigurationRepository checkWikiRepository;

  @BeforeEach
  void setUp() {
    givenLegacyConfiguration(Map.of());
  }

  @DisplayName("should move the punctuation after references before them")
  @Test
  void fixPunctuationAfter() {
    // GIVEN
    final String text =
        String.join(
            "\n",
            "Simple" + REF_A + ". Next",
            "Space " + REF_A + ",",
            "Group" + REF_A + " " + REF_B + "; next",
            "Brace{{Note|Text" + REF_A + ".}}",
            "Small" + REF_A + "<small>,</small>" + REF_B + ". Next",
            "Sub" + REF_A + "<sub>,</sub>" + REF_B + ". Next",
            "Multiple" + REF_A + "... next");

    // WHEN
    final List<CheckError> errors = analyze("Lyon", text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(
            error(text, REF_A + ".", "Simple", replacement("." + REF_A, true)),
            error(text, " " + REF_A + ",", "Space", replacement("," + REF_A, true)),
            error(text, REF_A + " " + REF_B + ";", "Group", replacement(";" + REF_A + REF_B, true)),
            error(text, REF_A + ".", "Text", replacement("." + REF_A, true)),
            error(
                text,
                REF_A + "<small>,</small>" + REF_B + ".",
                "Small",
                replacement("." + REF_A + REF_B, true)),
            error(
                text,
                REF_A + "<sub>,</sub>" + REF_B + ".",
                "Sub",
                replacement("." + REF_A + REF_B, true)),
            error(text, REF_A + "...", "Multiple", replacement("..." + REF_A, false)));
  }

  @DisplayName("should handle the punctuation before references")
  @Test
  void handlePunctuationBefore() {
    // GIVEN
    final String text =
        String.join(
            "\n",
            "Same." + REF_A + ". Next",
            "(Kept)" + REF_A + ". Next",
            "Other!" + REF_A + ". Next",
            "Glued." + REF_A + ".Next",
            "{{Note|Brace." + REF_A + ".}}",
            "Line." + REF_A + ".",
            "Next");

    // WHEN
    final List<CheckError> errors = analyze("Lyon", text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(
            error(text, "." + REF_A + ".", "Same", replacement("." + REF_A, true)),
            error(
                text,
                ")" + REF_A + ".",
                "(Kept",
                replacement(")." + REF_A, true),
                replacement("." + REF_A, false),
                replacement(")" + REF_A, false)),
            error(
                text,
                "!" + REF_A + ".",
                "Other",
                replacement("." + REF_A, false),
                replacement("!" + REF_A, false)),
            error(text, "." + REF_A + ".", "Brace", replacement("." + REF_A, false)),
            error(text, "." + REF_A + ".", "Line", replacement("." + REF_A, true)));
  }

  @DisplayName("should not report references not followed by a punctuation")
  @Test
  void ignoreOtherCases() {
    // GIVEN
    final String text =
        String.join(
            "\n",
            "Domain" + REF_A + ".com",
            "Line" + REF_A,
            ": indented",
            ";Term" + REF_A + ": definition",
            "Comment<!--" + REF_A + ". -->",
            "<nowiki>" + REF_A + ".</nowiki>",
            "<math>" + REF_A + ".</math>",
            "Question" + REF_A + "? Next");

    // WHEN
    final List<CheckError> errors = analyze("Lyon", text);

    // THEN
    Assertions.assertThat(errors).isEmpty();
  }

  @DisplayName("should not fix automatically outside articles")
  @Test
  void fixManuallyOutsideArticles() {
    // GIVEN
    final String text = "Simple" + REF_A + ". Next";

    // WHEN
    final List<CheckError> errors = analyze("Utilisateur:Lyon", text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(error(text, REF_A + ".", "Simple", replacement("." + REF_A, false)));
  }

  @DisplayName("should report punctuation between references, with the legacy configuration")
  @Test
  void reportPunctuationBetweenWithLegacyConfiguration() {
    // GIVEN
    givenLegacyConfiguration(
        Map.of("error_061_force_separator_frwiki", "true", "error_061_templates_frwiki", "Sfn"));
    final String text =
        String.join(
            "\n",
            "Before." + REF_A + ", " + REF_B + " next",
            "Spaced. " + REF_A + ", " + REF_B + " next",
            "None" + REF_A + "," + REF_B + " next",
            "Template{{sfn|A}}. Next",
            "Nested<ref>{{sfn|A}}</ref>. Next",
            "Space" + REF_A + "&nbsp;" + REF_B + ". Next");

    // WHEN
    final List<CheckError> errors = analyze("Lyon", text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(
            error(text, ", ", "Before." + REF_A, replacement("", true)),
            error(text, ", ", "Spaced. " + REF_A, replacement("", true)),
            error(text, ",", "None" + REF_A, replacement("", false)),
            error(text, "{{sfn|A}}.", "Template", replacement(".{{sfn|A}}", true)),
            error(
                text,
                "<ref>{{sfn|A}}</ref>.",
                "Nested",
                replacement(".<ref>{{sfn|A}}</ref>", true)),
            error(
                text,
                REF_A + "&nbsp;" + REF_B + ".",
                "Space",
                replacement("." + REF_A + REF_B, true)));
  }

  @DisplayName("should use the separator, with the YAML configuration")
  @Test
  void useSeparatorWithYamlConfiguration() {
    // GIVEN
    givenYamlConfiguration("separator: \"{{,}}\"\ntemplates:\n  - sfn");
    final String text =
        String.join(
            "\n",
            "Before." + REF_A + ", " + REF_B + " next",
            "Separator." + REF_A + "{{,}}" + REF_B + " next",
            "Single" + REF_A + ". Next",
            "Group" + REF_A + " " + REF_B + "{{sfn|A}}. Next");

    // WHEN
    final List<CheckError> errors = analyze("Lyon", text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(
            error(text, ", ", "Before." + REF_A, replacement("{{,}}", false)),
            error(text, REF_A + ".", "Single", replacement("." + REF_A, true)),
            error(
                text,
                REF_A + " " + REF_B + "{{sfn|A}}.",
                "Group",
                replacement(
                    "." + REF_A + "{{,}}" + REF_B + "{{,}}{{sfn|A}}",
                    "." + SHORT_REFERENCE + "{{,}}...{{,}}" + SHORT_REFERENCE,
                    true)));
  }

  @DisplayName("should group references separated by a short separator")
  @Test
  void groupWithShortSeparator() {
    // GIVEN
    givenYamlConfiguration("separator: /");
    final String text = "Slash" + REF_A + "/" + REF_B + ". Next";

    // WHEN
    final List<CheckError> errors = analyze("Lyon", text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(
            error(
                text,
                REF_A + "/" + REF_B + ".",
                "Slash",
                replacement("." + REF_A + "/" + REF_B, true)));
  }

  @DisplayName("should use only the YAML configuration when it exists")
  @Test
  void ignoreLegacyWithYaml() {
    // GIVEN
    checkWikiRepository.setConfiguration(
        PageAnalysisTestFactory.WIKI,
        CheckWikiConfiguration.of(
                Map.of("error_061_prio_frwiki", "3", "error_061_templates_frwiki", "Sfn"),
                Map.of(),
                "frwiki")
            .withParameters(Map.of(ALGORITHM, CheckAlgorithmParameters.parse("separator: ''"))));

    // WHEN
    final List<CheckError> errors = analyze("Lyon", "Template{{sfn|A}}. Next");

    // THEN
    Assertions.assertThat(errors).isEmpty();
  }

  private List<CheckError> analyze(final String title, final String text) {
    return algorithm.analyze(PageAnalysisTestFactory.createPageAnalysis(title, text));
  }

  private static Replacement replacement(final String text, final boolean automatic) {
    return replacement(
        text,
        text.replace("<ref>" + TEMPLATE + "</ref>", SHORT_REFERENCE)
            .replace(TEMPLATE, SHORT_REFERENCE)
            .replace(REF_A, SHORT_REFERENCE)
            .replace(REF_B, SHORT_REFERENCE),
        automatic);
  }

  private static Replacement replacement(
      final String text, final String label, final boolean automatic) {
    return new Replacement(text, label, automatic);
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
    properties.put("error_061_prio_frwiki", "3");
    checkWikiRepository.setConfiguration(
        PageAnalysisTestFactory.WIKI, CheckWikiConfiguration.of(properties, Map.of(), "frwiki"));
  }

  private void givenYamlConfiguration(final String yaml) {
    checkWikiRepository.setConfiguration(
        PageAnalysisTestFactory.WIKI,
        CheckWikiConfiguration.of(Map.of("error_061_prio_frwiki", "3"), Map.of(), "frwiki")
            .withParameters(Map.of(ALGORITHM, CheckAlgorithmParameters.parse(yaml))));
  }
}
