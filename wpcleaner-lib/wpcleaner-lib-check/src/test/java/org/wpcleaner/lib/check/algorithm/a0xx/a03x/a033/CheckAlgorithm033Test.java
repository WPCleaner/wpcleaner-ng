package org.wpcleaner.lib.check.algorithm.a0xx.a03x.a033;

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
import org.wpcleaner.lib.check.Replacement;
import org.wpcleaner.lib.check.configuration.CheckAlgorithmParameters;
import org.wpcleaner.lib.check.configuration.CheckWikiConfiguration;
import org.wpcleaner.lib.check.configuration.CheckWikiConfigurationRepository;

@CheckSpringBootTest
class CheckAlgorithm033Test {

  private static final int ALGORITHM = 33;
  private static final String PRIORITY = "error_033_prio_frwiki";
  private static final List<Replacement> DELETION = List.of(new Replacement("", false));

  @Autowired private CheckAlgorithm033 algorithm;
  @Autowired private CheckWikiConfigurationRepository checkWikiRepository;

  @BeforeEach
  void setUp() {
    givenLegacyConfiguration(Map.of());
  }

  @DisplayName("should suggest plain, italic or bold text for a complete tag")
  @Test
  void replaceCompleteTag() {
    // GIVEN
    final String text = "a <U class=\"x\">underlined text</u> c";

    // WHEN
    final List<CheckError> errors = analyze("Paris", text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(
            error(
                text,
                "<U class=\"x\">underlined text</u>",
                List.of(
                    new Replacement("underlined text", false),
                    new Replacement("''underlined text''", false),
                    new Replacement("'''underlined text'''", false))));
  }

  @DisplayName("should suggest deleting a complete tag with blank contents or a self closed tag")
  @Test
  void deleteBlankAndSelfClosedTags() {
    // GIVEN
    final String text = "a <u> </u> b <u/> c";

    // WHEN
    final List<CheckError> errors = analyze("Paris", text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(error(text, "<u> </u>", DELETION), error(text, "<u/>", DELETION));
  }

  @DisplayName("should report unclosed and unopened tags without replacement")
  @Test
  void reportIncompleteTags() {
    // GIVEN
    final String text = "a </u> b <u> c";

    // WHEN
    final List<CheckError> errors = analyze("Paris", text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(error(text, "</u>", List.of()), error(text, "<u>", List.of()));
  }

  @DisplayName("should suggest the templates of the legacy configuration")
  @Test
  void suggestTemplatesWithLegacyConfiguration() {
    // GIVEN
    givenLegacyConfiguration(Map.of("error_033_templates_frwiki", " Souligner\n \nUnderline"));
    final String text = "a <u>b=c</u> d";

    // WHEN
    final List<CheckError> errors = analyze("Paris", text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(
            error(
                text,
                "<u>b=c</u>",
                List.of(
                    new Replacement("b=c", false),
                    new Replacement("{{Souligner|1=b=c}}", false),
                    new Replacement("{{Underline|1=b=c}}", false),
                    new Replacement("''b=c''", false),
                    new Replacement("'''b=c'''", false))));
  }

  @DisplayName("should suggest the templates of the YAML configuration instead of the legacy ones")
  @Test
  void suggestTemplatesWithYamlConfiguration() {
    // GIVEN
    checkWikiRepository.setConfiguration(
        PageAnalysisTestFactory.WIKI,
        CheckWikiConfiguration.of(
                Map.of(PRIORITY, "3", "error_033_templates_frwiki", "Underline"),
                Map.of(),
                "frwiki")
            .withParameters(
                Map.of(ALGORITHM, CheckAlgorithmParameters.parse("templates:\n  - Souligner"))));
    final String text = "a <u>b</u> c";

    // WHEN
    final List<CheckError> errors = analyze("Paris", text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(
            error(
                text,
                "<u>b</u>",
                List.of(
                    new Replacement("b", false),
                    new Replacement("{{Souligner|b}}", false),
                    new Replacement("''b''", false),
                    new Replacement("'''b'''", false))));
  }

  @DisplayName("should report tags in subject namespaces, but not in talk pages")
  @Test
  void ignoreNamespaces() {
    // GIVEN
    final String text = "<u>a</u>";

    // WHEN
    final List<CheckError> userErrors = analyze("Utilisateur:Paris", text);
    final List<CheckError> talkErrors = analyze("Discussion utilisateur:Paris", text);

    // THEN
    Assertions.assertThat(userErrors).hasSize(1);
    Assertions.assertThat(talkErrors).isEmpty();
  }

  @DisplayName("should not report other tags, or tags in comments and non wiki content")
  @Test
  void ignoreOtherTags() {
    // GIVEN
    final String text =
        "<b>a</b> <ul><li>a</li></ul> <!-- <u>a</u> --> <nowiki><u>a</u></nowiki> <pre><u>a</u></pre> <math><u>a</u></math> <source lang=\"x\"><u>a</u></source> <syntaxhighlight lang=\"x\"><u>a</u></syntaxhighlight>";

    // WHEN
    final List<CheckError> errors = analyze("Paris", text);

    // THEN
    Assertions.assertThat(errors).isEmpty();
  }

  private List<CheckError> analyze(final String title, final String text) {
    return algorithm.analyze(PageAnalysisTestFactory.createPageAnalysis(title, text));
  }

  private void givenLegacyConfiguration(final Map<String, String> parameters) {
    final Map<String, String> properties = new HashMap<>(parameters);
    properties.put(PRIORITY, "3");
    checkWikiRepository.setConfiguration(
        PageAnalysisTestFactory.WIKI, CheckWikiConfiguration.of(properties, Map.of(), "frwiki"));
  }

  private static CheckError error(
      final String text, final String found, final List<Replacement> replacements) {
    final int begin = text.lastIndexOf(found);
    return new CheckError(ALGORITHM, begin, begin + found.length(), replacements);
  }
}
