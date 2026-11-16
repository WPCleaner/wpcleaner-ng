package org.wpcleaner.lib.check.algorithm.a5xx.a56x.a566;

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
class CheckAlgorithm566Test {

  private static final int ALGORITHM = 566;

  @Autowired private CheckAlgorithm566 algorithm;
  @Autowired private CheckWikiConfigurationRepository checkWikiRepository;

  @BeforeEach
  void setUp() {
    givenLegacyConfiguration(Map.of());
  }

  @DisplayName("should report complete abbr tags and suggest to keep only the abbreviation")
  @Test
  void reportCompleteTags() {
    // GIVEN
    final String text = "a <abbr title=\"b\">c</abbr> d <ABBR>e</abbr> f";

    // WHEN
    final List<CheckError> errors = analyze("Paris", text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(
            error(text, "<abbr title=\"b\">c</abbr>", new Replacement("c", false)),
            error(text, "<ABBR>e</abbr>", new Replacement("e", false)));
  }

  @DisplayName("should suggest to remove abbr tags that are not complete")
  @Test
  void reportIncompleteTags() {
    // GIVEN
    final String text = "a</abbr>b ! c<abbr/>d ! e<abbr title=\"f\">g";

    // WHEN
    final List<CheckError> errors = analyze("Paris", text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(
            error(text, "a</abbr>b", new Replacement("ab", false)),
            error(text, "c<abbr/>d", new Replacement("cd", false)),
            error(text, "e<abbr title=\"f\">g", new Replacement("eg", false)));
  }

  @DisplayName("should extend the error to the letters and digits around the tag")
  @Test
  void extendToSurroundingWord() {
    // GIVEN
    final String text = "a é1<abbr>b</abbr>2Ɏ, ŉ<abbr>c</abbr>µ αd<abbr>e</abbr>fα";

    // WHEN
    final List<CheckError> errors = analyze("Paris", text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(
            error(text, "é1<abbr>b</abbr>2Ɏ", new Replacement("é1b2Ɏ", false)),
            error(text, "<abbr>c</abbr>", new Replacement("c", false)),
            error(text, "d<abbr>e</abbr>f", new Replacement("def", false)));
  }

  @DisplayName("should move spaces around the abbreviation outside of the replacements")
  @Test
  void moveSurroundingSpaces() {
    // GIVEN
    givenLegacyConfiguration(
        Map.of(
            "error_566_replacements_frwiki", "b|true|{{b}}",
            "error_566_templates_frwiki", "Abréviation|1|2"));
    final String text = "a<abbr title=\"t\"> &nbsp; b&nbsp; </abbr>c <abbr> </abbr>";

    // WHEN
    final List<CheckError> errors = analyze("Paris", text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(
            error(
                text,
                "a<abbr title=\"t\"> &nbsp; b&nbsp; </abbr>c",
                new Replacement("a &nbsp; {{b}}&nbsp; c", false),
                new Replacement("a &nbsp; {{Abréviation|b|t}}&nbsp; c", false),
                new Replacement("a &nbsp; b&nbsp; c", false)),
            error(
                text,
                "<abbr> </abbr>",
                new Replacement(" {{Abréviation||}}", false),
                new Replacement(" ", false)));
  }

  @DisplayName("should suggest replacements based on the abbreviation value")
  @Test
  void replaceByValue() {
    // GIVEN
    givenLegacyConfiguration(
        Map.of(
            "error_566_replacements_frwiki",
            "†|true|{{décédé}}\n1er|false|{{1er}}\n2e|true|{{old}}\n2e|true|{{e|2|x}}\n)|true|)\n"
                + "|true|{{empty}}\nincomplete|true"));
    final String text =
        "<abbr>†</abbr> <abbr class=\"x\" title=\"\">2e</abbr> <abbr>1er</abbr> <abbr>)</abbr>"
            + " <abbr title=\"t\">†</abbr> <abbr lang=\"fr\">†</abbr> <abbr>x</abbr> <abbr></abbr>"
            + " <abbr>incomplete</abbr>";

    // WHEN
    final List<CheckError> errors = analyze("Paris", text);
    final List<CheckError> otherErrors = analyze("Utilisateur:Paris", text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(
            error(
                text,
                "<abbr>†</abbr>",
                new Replacement("{{décédé}}", true),
                new Replacement("†", false)),
            error(
                text,
                "<abbr class=\"x\" title=\"\">2e</abbr>",
                new Replacement("{{e|2|x}}", true),
                new Replacement("2e", false)),
            error(
                text,
                "<abbr>1er</abbr>",
                new Replacement("{{1er}}", false),
                new Replacement("1er", false)),
            error(text, "<abbr>)</abbr>", new Replacement(")", true)),
            error(
                text,
                "<abbr title=\"t\">†</abbr>",
                new Replacement("{{décédé}}", false),
                new Replacement("†", false)),
            error(
                text,
                "<abbr lang=\"fr\">†</abbr>",
                new Replacement("{{décédé}}", false),
                new Replacement("†", false)),
            error(text, "<abbr>x</abbr>", new Replacement("x", false)),
            error(text, "<abbr></abbr>", new Replacement("", false)),
            error(text, "<abbr>incomplete</abbr>", new Replacement("incomplete", false)));
    Assertions.assertThat(otherErrors)
        .hasSize(9)
        .allSatisfy(
            error -> Assertions.assertThat(error.replacements()).noneMatch(Replacement::automatic));
  }

  @DisplayName("should suggest replacements based on the abbreviation value and title")
  @Test
  void replaceByValueAndTitle() {
    // GIVEN
    givenLegacyConfiguration(
        Map.of(
            "error_566_replacements_frwiki",
            "janv.|true|{{janv.}}",
            "error_566_replacements_title_frwiki",
            "janv.|janvier|true|{{Abréviation|janv.|janvier}}\njanv.|january|false|{{en}}\n"
                + "incomplete|title|true"));
    final String text =
        "<abbr title=\" janvier \">janv.</abbr> <abbr title=\"january\">janv.</abbr>"
            + " <abbr title=\"other\">janv.</abbr> <abbr title=\"janvier\">fév.</abbr>"
            + " <abbr title=\"title\">incomplete</abbr>";

    // WHEN
    final List<CheckError> errors = analyze("Paris", text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(
            error(
                text,
                "<abbr title=\" janvier \">janv.</abbr>",
                new Replacement("{{Abréviation|janv.|janvier}}", true),
                new Replacement("janv.", false)),
            error(
                text,
                "<abbr title=\"january\">janv.</abbr>",
                new Replacement("{{en}}", false),
                new Replacement("janv.", false)),
            error(
                text,
                "<abbr title=\"other\">janv.</abbr>",
                new Replacement("{{janv.}}", false),
                new Replacement("janv.", false)),
            error(text, "<abbr title=\"janvier\">fév.</abbr>", new Replacement("fév.", false)),
            error(
                text,
                "<abbr title=\"title\">incomplete</abbr>",
                new Replacement("incomplete", false)));
  }

  @DisplayName("should suggest templates which can replace an abbreviation")
  @Test
  void replaceWithTemplates() {
    // GIVEN
    givenLegacyConfiguration(
        Map.of(
            "error_566_templates_frwiki",
            "Abréviation|1|2|<!-- Signification -->\nAbbr|text|meaning\nIncomplete|1\n|1|2"));
    final String text = "<abbr title=\" b \">a</abbr> <abbr class=\"x\">c</abbr>";

    // WHEN
    final List<CheckError> errors = analyze("Paris", text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(
            error(
                text,
                "<abbr title=\" b \">a</abbr>",
                new Replacement("{{Abréviation|a|b}}", false),
                new Replacement("{{Abbr|text=a|meaning=b}}", false),
                new Replacement("a", false)),
            error(
                text,
                "<abbr class=\"x\">c</abbr>",
                new Replacement("{{Abréviation|c|<!-- Signification -->}}", false),
                new Replacement("{{Abbr|text=c|meaning=}}", false),
                new Replacement("c", false)));
  }

  @DisplayName("should read parameters from YAML instead of the legacy configuration")
  @Test
  void readYamlParameters() {
    // GIVEN
    final String yaml =
        """
        replacements:
          - value: "†"
            automatic: true
            replacement: "{{décédé}}"
          - value: 1er
            automatic: false
            replacement: "{{1er}}"
          - value: incomplete
            automatic: true
        replacements_title:
          - value: janv.
            title: janvier
            automatic: true
            replacement: "{{Abréviation|janv.|janvier}}"
        templates:
          - template: Abréviation
            abbreviation: 1
            meaning: 2
            default_meaning: "<!-- Signification -->"
        """;
    checkWikiRepository.setConfiguration(
        PageAnalysisTestFactory.WIKI,
        CheckWikiConfiguration.of(
                Map.of(
                    "error_566_replacements_frwiki", "x|true|{{legacy}}",
                    "error_566_templates_frwiki", "Legacy|1|2"),
                Map.of(),
                "frwiki")
            .withParameters(Map.of(ALGORITHM, CheckAlgorithmParameters.parse(yaml))));
    final String text =
        "<abbr>†</abbr> <abbr>1er</abbr> <abbr title=\"janvier\">janv.</abbr> <abbr>x</abbr>"
            + " <abbr>incomplete</abbr>";

    // WHEN
    final List<CheckError> errors = analyze("Paris", text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(
            error(
                text,
                "<abbr>†</abbr>",
                new Replacement("{{décédé}}", true),
                new Replacement("{{Abréviation|†|<!-- Signification -->}}", false),
                new Replacement("†", false)),
            error(
                text,
                "<abbr>1er</abbr>",
                new Replacement("{{1er}}", false),
                new Replacement("{{Abréviation|1er|<!-- Signification -->}}", false),
                new Replacement("1er", false)),
            error(
                text,
                "<abbr title=\"janvier\">janv.</abbr>",
                new Replacement("{{Abréviation|janv.|janvier}}", true),
                new Replacement("janv.", false)),
            error(
                text,
                "<abbr>x</abbr>",
                new Replacement("{{Abréviation|x|<!-- Signification -->}}", false),
                new Replacement("x", false)),
            error(
                text,
                "<abbr>incomplete</abbr>",
                new Replacement("{{Abréviation|incomplete|<!-- Signification -->}}", false),
                new Replacement("incomplete", false)));
  }

  @DisplayName("should not report abbr tags in talk namespaces, comments and non wiki content")
  @Test
  void ignoreTags() {
    // GIVEN
    final String text = "<abbr>a</abbr>";
    final String ignoredText =
        "<span>a</span> <!-- <abbr>a</abbr> --> <nowiki><abbr>a</abbr></nowiki>"
            + " <source lang=\"x\"><abbr>a</abbr></source>"
            + " <syntaxhighlight lang=\"x\"><abbr>a</abbr></syntaxhighlight>";

    // WHEN
    final List<CheckError> talkErrors = analyze("Discussion utilisateur:Paris", text);
    final List<CheckError> errors = analyze("Paris", ignoredText);

    // THEN
    Assertions.assertThat(talkErrors).isEmpty();
    Assertions.assertThat(errors).isEmpty();
  }

  private List<CheckError> analyze(final String title, final String text) {
    return algorithm.analyze(PageAnalysisTestFactory.createPageAnalysis(title, text));
  }

  private void givenLegacyConfiguration(final Map<String, String> properties) {
    checkWikiRepository.setConfiguration(
        PageAnalysisTestFactory.WIKI, CheckWikiConfiguration.of(properties, Map.of(), "frwiki"));
  }

  private static CheckError error(
      final String text, final String found, final Replacement... replacements) {
    final int begin = text.indexOf(found);
    return new CheckError(ALGORITHM, begin, begin + found.length(), List.of(replacements));
  }
}
