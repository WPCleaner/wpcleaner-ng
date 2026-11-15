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
import org.wpcleaner.api.repository.configuration.WPCleanerConfiguration;
import org.wpcleaner.api.repository.configuration.WPCleanerConfigurationRepository;
import org.wpcleaner.lib.check.CheckError;
import org.wpcleaner.lib.check.CheckSpringBootTest;
import org.wpcleaner.lib.check.Replacement;
import org.wpcleaner.lib.check.configuration.CheckAlgorithmParameters;
import org.wpcleaner.lib.check.configuration.CheckWikiConfiguration;
import org.wpcleaner.lib.check.configuration.CheckWikiConfigurationRepository;

@CheckSpringBootTest
class CheckAlgorithm002Test {

  private static final int ALGORITHM = 2;
  private static final Replacement DELETE = new Replacement("", false);
  private static final Replacement BR_FULL = new Replacement("<br />", false);
  private static final Replacement BR_OPEN = new Replacement("<br>", false);

  @Autowired private CheckAlgorithm002 algorithm;
  @Autowired private CheckWikiConfigurationRepository checkWikiRepository;
  @Autowired private WPCleanerConfigurationRepository wpcleanerRepository;

  @BeforeEach
  void setUp() {
    givenLegacyConfiguration(Map.of());
    givenWPCleanerConfiguration(Map.of());
  }

  @DisplayName("should fix incorrect br and hr tags, automatically when the syntax is clear")
  @Test
  void fixIncorrectVoidTags() {
    // GIVEN
    final String text = "a</br>b <br.>c <\\br>d <hr,>e </br/>f <br g";

    // WHEN
    final List<CheckError> errors = analyze("Paris", text);
    final List<CheckError> otherErrors = analyze("Utilisateur:Paris", text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(
            error(text, "</br>", new Replacement("<br />", true)),
            error(text, "<br.>", new Replacement("<br />", true)),
            error(text, "<\\br>", new Replacement("<br />", true)),
            error(text, "<hr,>", new Replacement("<hr />", true)),
            error(text, "</br/>", new Replacement("<br />", true)),
            error(text, "<br ", new Replacement("<br />", false)));
    Assertions.assertThat(otherErrors)
        .extracting(error -> error.replacements().getFirst().automatic())
        .containsExactly(true, false, false, false, false, false);
  }

  @DisplayName("should report incorrect tags inside tags with wiki content")
  @Test
  void fixIncorrectTagsInWikiContent() {
    // GIVEN
    final String text = "a<ref>b</br>c</ref>";

    // WHEN
    final List<CheckError> errors = analyze("Paris", text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(error(text, "</br>", new Replacement("<br />", true)));
  }

  @DisplayName("should fix automatically br and hr tags with a line break inside")
  @Test
  void fixVoidTagsWithLineBreak() {
    // GIVEN
    final String text = "a<br \n>b <br\n/>c <hr\n>d <br / \n>e <\nbr>f <br>\ng <br />\nh";

    // WHEN
    final List<CheckError> errors = analyze("Paris", text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(
            error(text, "<br \n>", new Replacement("<br />", true)),
            error(text, "<br\n/>", new Replacement("<br />", true)),
            error(text, "<hr\n>", new Replacement("<hr />", true)),
            error(text, "<br / \n>", new Replacement("<br />", true)),
            error(text, "<\nbr>", new Replacement("<br />", true)));
  }

  @DisplayName("should use open br tags when self-closing tags are not wanted")
  @Test
  void fixIncorrectVoidTagsWithoutClosing() {
    // GIVEN
    givenWPCleanerConfiguration(Map.of("close_self_closing_tags", "false"));
    final String text = "a</br>b";

    // WHEN
    final List<CheckError> errors = analyze("Paris", text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(error(text, "</br>", new Replacement("<br>", true)));
  }

  @DisplayName("should not report correct tags, or tags in comments and non wiki content")
  @Test
  void ignoreCorrectTags() {
    // GIVEN
    final String text =
        "<br> <br/> <br /> <br / > <brx> <b>x</b> <hr> <span>x</span> <!-- </br> <span/> -->"
            + " <nowiki></br></span/></nowiki> <source lang=\"x\"></br><span/></source>"
            + " <ref name=\"a\"/> </spanx/> <i/x>";

    // WHEN
    final List<CheckError> errors = analyze("Paris", text);

    // THEN
    Assertions.assertThat(errors).isEmpty();
  }

  @DisplayName("should suggest clear templates for br tags with attributes")
  @Test
  void fixVoidTagAttributes() {
    // GIVEN
    givenLegacyConfiguration(
        Map.of(
            "error_002_clear_all_frwiki", "{{Clr}}",
            "error_002_clear_left_frwiki", "{{Clr|left}}",
            "error_002_clear_right_frwiki", "{{Clr|right}}"));
    final String text =
        "<br clear=\"all\"> <br style=\"clear: left;\"> <br break=\"right\"/>"
            + " <br clear=\"none\"> <<br>> <br class=\"x\"> <br style=\"float: left\">";

    // WHEN
    final List<CheckError> errors = analyze("Paris", text);
    final List<CheckError> otherErrors = analyze("Utilisateur:Paris", text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(
            error(text, "<br clear=\"all\">", new Replacement("{{Clr}}", true), BR_FULL, BR_OPEN),
            error(
                text,
                "<br style=\"clear: left;\">",
                new Replacement("{{Clr|left}}", true),
                BR_FULL,
                BR_OPEN),
            error(
                text,
                "<br break=\"right\"/>",
                new Replacement("{{Clr|right}}", true),
                BR_FULL,
                BR_OPEN),
            error(text, "<br clear=\"none\">", BR_FULL, BR_OPEN),
            error(text, "<<br>>", BR_FULL, BR_OPEN),
            error(text, "<br class=\"x\">", BR_FULL, BR_OPEN),
            error(text, "<br style=\"float: left\">", BR_FULL, BR_OPEN));
    Assertions.assertThat(otherErrors.getFirst().replacements().getFirst().automatic()).isFalse();
  }

  @DisplayName("should suggest replacements for tags that cannot be self-closing")
  @Test
  void fixSelfClosedTags() {
    // GIVEN
    givenYamlConfiguration(
        """
        anchor_templates:
          - template: Ancre
          - template: Anchor
            parameter: name
        clear_all: "{{Clr}}"
        """);
    final String text =
        "<span/> <span id=\" a=b \"/> <cite id=\"c\" class=\"x\"/> <div clear=\"both\"/>"
            + " <i>x<i/> <b>x</b><b/> <strong/> <u>x<u/>y</u> <p clear=\"all\"/>";

    // WHEN
    final List<CheckError> errors = analyze("Paris", text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(
            error(text, "<span/>", DELETE),
            error(
                text,
                "<span id=\" a=b \"/>",
                new Replacement("{{Ancre|1=a=b}}", false),
                new Replacement("{{Anchor|name=a=b}}", false),
                DELETE),
            error(text, "<cite id=\"c\" class=\"x\"/>", DELETE),
            error(text, "<div clear=\"both\"/>", new Replacement("{{Clr}}", false), DELETE),
            error(text, "<i/>", new Replacement("</i>", false), DELETE),
            error(text, "<b/>", DELETE),
            error(text, "<strong/>", DELETE),
            error(text, "<u/>", DELETE),
            error(text, "<p clear=\"all\"/>", DELETE));
  }

  @DisplayName("should use the general anchor templates when the algorithm has none")
  @Test
  void fixSelfClosedTagsWithGeneralAnchorTemplates() {
    // GIVEN
    givenWPCleanerConfiguration(Map.of("general_anchor_templates", "Ancre|nom|true\nAnchor"));
    final String text = "<span id=\"a\"/>";

    // WHEN
    final List<CheckError> errors = analyze("Paris", text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(
            error(
                text,
                text,
                new Replacement("{{Ancre|nom=a}}", false),
                new Replacement("{{Anchor|a}}", false),
                DELETE));
  }

  @DisplayName("should prefer the anchor templates of the algorithm, from legacy properties")
  @Test
  void fixSelfClosedTagsWithLegacyAnchorTemplates() {
    // GIVEN
    givenLegacyConfiguration(Map.of("error_002_anchor_templates_frwiki", "Anchor|name"));
    givenWPCleanerConfiguration(Map.of("general_anchor_templates", "Ancre|1|true"));
    final String text = "<div id=\"a\"/>";

    // WHEN
    final List<CheckError> errors = analyze("Paris", text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(error(text, text, new Replacement("{{Anchor|name=a}}", false), DELETE));
  }

  @DisplayName("should fix closing tags written as self-closing")
  @Test
  void fixIncorrectEndTags() {
    // GIVEN
    final String text = "<span>a</span/> <small>b</ small / > <b>c</b/>";

    // WHEN
    final List<CheckError> errors = analyze("Paris", text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(
            error(text, "</span/>", new Replacement("</span>", false)),
            error(text, "</ small / >", new Replacement("</small>", false)),
            error(text, "</b/>", new Replacement("</b>", false)));
  }

  private List<CheckError> analyze(final String title, final String text) {
    return algorithm.analyze(PageAnalysisTestFactory.createPageAnalysis(title, text));
  }

  private void givenLegacyConfiguration(final Map<String, String> properties) {
    checkWikiRepository.setConfiguration(
        PageAnalysisTestFactory.WIKI, CheckWikiConfiguration.of(properties, Map.of(), "frwiki"));
  }

  private void givenYamlConfiguration(final String yaml) {
    checkWikiRepository.setConfiguration(
        PageAnalysisTestFactory.WIKI,
        CheckWikiConfiguration.of(
                Map.of("error_002_clear_all_frwiki", "{{Legacy}}"), Map.of(), "frwiki")
            .withParameters(Map.of(ALGORITHM, CheckAlgorithmParameters.parse(yaml))));
  }

  private void givenWPCleanerConfiguration(final Map<String, String> properties) {
    wpcleanerRepository.setConfiguration(
        PageAnalysisTestFactory.WIKI, new WPCleanerConfiguration(properties, Map.of()));
  }

  private static CheckError error(
      final String text, final String found, final Replacement... replacements) {
    final int begin = text.indexOf(found);
    return new CheckError(ALGORITHM, begin, begin + found.length(), List.of(replacements));
  }
}
