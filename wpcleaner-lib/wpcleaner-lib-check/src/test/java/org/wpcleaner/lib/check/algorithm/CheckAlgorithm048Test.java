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
import org.wpcleaner.api.analysis.PageAnalysis;
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
class CheckAlgorithm048Test {

  private static final int ALGORITHM = 48;

  @Autowired private CheckAlgorithm048 algorithm;
  @Autowired private CheckWikiConfigurationRepository checkWikiRepository;
  @Autowired private WPCleanerConfigurationRepository wpcleanerRepository;

  @BeforeEach
  void setUp() {
    givenConfiguration(Map.of(), Map.of());
  }

  @DisplayName("should report links to the page itself, whatever their writing")
  @Test
  void reportLinksToPage() {
    // GIVEN
    final PageAnalysis analysis =
        PageAnalysisTestFactory.createPageAnalysis(
            "Tour Eiffel", "[[Tour Eiffel]] [[tour_Eiffel|tour]] [[Tour]] [[:Tour Eiffel]]");

    // WHEN
    final List<CheckError> errors = algorithm.analyze(analysis);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(
            new CheckError(ALGORITHM, 0, 15, textOrBold("Tour Eiffel", false)),
            new CheckError(ALGORITHM, 16, 36, textOrBold("tour", false)),
            new CheckError(ALGORITHM, 46, 62, textOrBold("Tour Eiffel", false)));
  }

  @DisplayName("should fix automatically links in bold or after the first title")
  @Test
  void fixLinksInBoldOrAfterFirstTitle() {
    // GIVEN
    final String text =
        "'''[[Paris]]''' est une ville.\n== Histoire ==\n[[Paris|La ville]] et l'[[Paris]]";

    // WHEN
    final List<CheckError> errors =
        algorithm.analyze(PageAnalysisTestFactory.createPageAnalysis("Paris", text));

    // THEN
    final int afterTitle = text.indexOf("[[Paris|");
    final int afterApostrophe = text.lastIndexOf("[[Paris]]");
    Assertions.assertThat(errors)
        .containsExactly(
            new CheckError(ALGORITHM, 3, 12, List.of(new Replacement("Paris", true))),
            new CheckError(ALGORITHM, afterTitle, afterTitle + 18, textOrBold("La ville", true)),
            new CheckError(
                ALGORITHM,
                afterApostrophe - 1,
                afterApostrophe + 9,
                List.of(new Replacement("'Paris", true), new Replacement("''''Paris'''", false))));
  }

  @DisplayName("should report links to a section of the page only in the main namespace")
  @Test
  void reportLinksToSection() {
    // GIVEN
    final String text = "[[Paris#Histoire|histoire]]";

    // WHEN
    final List<CheckError> mainErrors =
        algorithm.analyze(PageAnalysisTestFactory.createPageAnalysis("Paris", text));
    final List<CheckError> userErrors =
        algorithm.analyze(
            PageAnalysisTestFactory.createPageAnalysis(
                "Utilisateur:Paris", "[[Utilisateur:Paris#Histoire|histoire]]"));

    // THEN
    Assertions.assertThat(mainErrors)
        .containsExactly(
            new CheckError(
                ALGORITHM,
                0,
                text.length(),
                List.of(new Replacement("[[#Histoire|histoire]]", true))));
    Assertions.assertThat(userErrors).isEmpty();
  }

  @DisplayName("should not fix automatically links to footnotes or without text")
  @Test
  void fixLinksToSectionManually() {
    // GIVEN
    final String text = "[[Paris#cite_note-1|1]] [[Paris#Histoire]] [[Paris#]]";

    // WHEN
    final List<CheckError> errors =
        algorithm.analyze(PageAnalysisTestFactory.createPageAnalysis("Paris", text));

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(
            new CheckError(ALGORITHM, 0, 23, List.of(new Replacement("[[#cite_note-1|1]]", false))),
            new CheckError(
                ALGORITHM,
                24,
                42,
                List.of(
                    new Replacement("[[#Histoire|Histoire]]", false),
                    new Replacement("[[#Histoire|Paris#Histoire]]", false))),
            new CheckError(ALGORITHM, 43, 53, List.of()));
  }

  @DisplayName("should not report links in ignored areas")
  @Test
  void ignoreLinksInIgnoredAreas() {
    // GIVEN
    final String text =
        "<includeonly>[[Paris]]</includeonly><onlyinclude>[[Paris]]</onlyinclude>"
            + "<imagemap>Image:A.jpg\nrect 0 0 1 1 [[Paris]]</imagemap>"
            + "<noinclude>[[Paris]]</noinclude>";

    // WHEN
    final List<CheckError> errors =
        algorithm.analyze(PageAnalysisTestFactory.createPageAnalysis("Paris", text));

    // THEN
    final int noIncludeLink = text.lastIndexOf("[[Paris]]");
    Assertions.assertThat(errors)
        .containsExactly(
            new CheckError(
                ALGORITHM, noIncludeLink, noIncludeLink + 9, textOrBold("Paris", false)));
  }

  @DisplayName("should not report anything on ignored pages")
  @Test
  void ignorePages() {
    // GIVEN
    final List<PageAnalysis> analyses =
        List.of(
            PageAnalysisTestFactory.createPageAnalysis("Paris", "#REDIRECT [[Paris]]"),
            PageAnalysisTestFactory.createPageAnalysis(
                "Discussion utilisateur:Bob", "[[Discussion utilisateur:Bob]]"),
            PageAnalysisTestFactory.createPageAnalysis("Paris", "[[Paris]] {{#lst:Lyon|a}}"),
            PageAnalysisTestFactory.createPageAnalysis("Paris", "[[Paris]] <section begin=a />"));

    // WHEN
    final List<List<CheckError>> errors = analyses.stream().map(algorithm::analyze).toList();

    // THEN
    Assertions.assertThat(errors).containsOnly(List.of());
  }

  @DisplayName("should ignore links in tags without wiki content, but not in undeclared tags")
  @Test
  void ignoreLinksInNonWikiTags() {
    // GIVEN
    final String text =
        "<nowiki>[[Paris]]</nowiki> <mapframe width=200>[[Paris]]</mapframe>"
            + " <timeline>[[Paris]]</timeline> <undeclared>[[Paris]]</undeclared> [[Paris]]";

    // WHEN
    final List<CheckError> errors =
        algorithm.analyze(PageAnalysisTestFactory.createPageAnalysis("Paris", text));

    // THEN
    Assertions.assertThat(errors)
        .extracting(CheckError::begin)
        .containsExactly(
            text.indexOf("<undeclared>") + "<undeclared>".length(), text.lastIndexOf("[[Paris]]"));
  }

  @DisplayName("should ignore links in the templates to ignore, given by name or by group")
  @Test
  void ignoreLinksInIgnoredTemplates() {
    // GIVEN
    givenConfiguration(
        Map.of(
            "error_048_ignore_templates_frwiki",
            "+Liens, +Article\nouvrage",
            "error_048_template_groups_frwiki",
            "Liens|lien web|Lien"),
        Map.of("general_template_groups", "Article|Article|Cite journal"));
    final String text =
        "{{Lien web|titre=[[Paris]]}} {{Cite journal|[[Paris]]}} {{Ouvrage|[[Paris]]}}"
            + " {{Autre|[[Paris]]}} {{lien|1={{Autre|[[Paris]]}}}}";

    // WHEN
    final List<CheckError> errors =
        algorithm.analyze(PageAnalysisTestFactory.createPageAnalysis("Paris", text));

    // THEN
    final int otherLink = text.indexOf("{{Autre|[[Paris]]}}") + "{{Autre|".length();
    final int nestedLink = text.lastIndexOf("[[Paris]]");
    Assertions.assertThat(errors)
        .extracting(CheckError::begin)
        .containsExactly(otherLink, nestedLink);
  }

  @DisplayName(
      "should ignore links in the templates and groups to ignore from the YAML configuration, without + prefix")
  @Test
  void ignoreLinksInIgnoredTemplatesFromYaml() {
    // GIVEN
    givenYamlConfiguration(
        """
        ignore_templates:
          - ouvrage
          - +Article
        ignore_groups:
          - Liens
        template_groups:
          Liens: [lien web, Lien]
        """,
        Map.of("general_template_groups", "Article|Article|Cite journal"));
    final String text =
        "{{Lien web|titre=[[Paris]]}} {{Cite journal|[[Paris]]}} {{Ouvrage|[[Paris]]}}"
            + " {{Autre|[[Paris]]}} {{lien|1={{Autre|[[Paris]]}}}}";

    // WHEN
    final List<CheckError> errors =
        algorithm.analyze(PageAnalysisTestFactory.createPageAnalysis("Paris", text));

    // THEN
    final int citeJournalLink =
        text.indexOf("{{Cite journal|[[Paris]]}}") + "{{Cite journal|".length();
    final int otherLink = text.indexOf("{{Autre|[[Paris]]}}") + "{{Autre|".length();
    final int nestedLink = text.lastIndexOf("[[Paris]]");
    Assertions.assertThat(errors)
        .extracting(CheckError::begin)
        .containsExactly(citeJournalLink, otherLink, nestedLink);
  }

  @DisplayName("should use only the YAML configuration when it exists")
  @Test
  void ignoreLegacyParametersWithYaml() {
    // GIVEN
    checkWikiRepository.setConfiguration(
        PageAnalysisTestFactory.WIKI,
        CheckWikiConfiguration.of(
                Map.of(
                    "error_048_prio_frwiki", "3",
                    "error_048_imagemap_frwiki", "true",
                    "error_048_ignore_templates_frwiki", "Lien web"),
                Map.of(),
                "frwiki")
            .withParameters(Map.of(ALGORITHM, CheckAlgorithmParameters.parse("imagemap: false"))));
    wpcleanerRepository.setConfiguration(
        PageAnalysisTestFactory.WIKI, WPCleanerConfiguration.EMPTY);
    final String text =
        "<imagemap>\nImage:Carte.png|vignette\nrect 0 0 10 10 [[Paris]]\n</imagemap> {{Lien web|[[Paris]]}}";

    // WHEN
    final List<CheckError> errors =
        algorithm.analyze(PageAnalysisTestFactory.createPageAnalysis("Paris", text));

    // THEN
    Assertions.assertThat(errors)
        .extracting(CheckError::begin)
        .containsExactly(text.lastIndexOf("[[Paris]]"));
  }

  @DisplayName("should report the lines of links in image maps when configured")
  @Test
  void reportLinksInImagemap() {
    // GIVEN
    givenConfiguration(Map.of("error_048_imagemap_frwiki", "true"), Map.of());
    final String line = "rect 0 0 10 10 [[Paris]]";
    final String text = "<imagemap>\nImage:Carte.png|vignette\n" + line + "\n</imagemap>";

    // WHEN
    final List<CheckError> errors =
        algorithm.analyze(PageAnalysisTestFactory.createPageAnalysis("Paris", text));

    // THEN
    final int lineBegin = text.indexOf(line);
    Assertions.assertThat(errors)
        .containsExactly(
            new CheckError(
                ALGORITHM,
                lineBegin,
                lineBegin + line.length(),
                List.of(new Replacement("", false))));
  }

  @DisplayName("should suggest the apostrophe template for a link after a single apostrophe")
  @Test
  void suggestApostropheTemplate() {
    // GIVEN
    givenConfiguration(Map.of(), Map.of("general_apostrophe_template", "'"));
    final String text = "l'[[Paris]]";

    // WHEN
    final List<CheckError> errors =
        algorithm.analyze(PageAnalysisTestFactory.createPageAnalysis("Paris", text));

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(
            new CheckError(
                ALGORITHM,
                1,
                text.length(),
                List.of(
                    new Replacement("'Paris", false),
                    new Replacement("{{'}}'''Paris'''", false),
                    new Replacement("''''Paris'''", false))));
  }

  private void givenConfiguration(
      final Map<String, String> checkWikiProperties, final Map<String, String> generalProperties) {
    checkWikiRepository.setConfiguration(
        PageAnalysisTestFactory.WIKI,
        CheckWikiConfiguration.of(checkWikiProperties, Map.of(), "frwiki"));
    wpcleanerRepository.setConfiguration(
        PageAnalysisTestFactory.WIKI, new WPCleanerConfiguration(generalProperties, Map.of()));
  }

  private void givenYamlConfiguration(
      final String yaml, final Map<String, String> generalProperties) {
    checkWikiRepository.setConfiguration(
        PageAnalysisTestFactory.WIKI,
        CheckWikiConfiguration.of(Map.of("error_048_prio_frwiki", "3"), Map.of(), "frwiki")
            .withParameters(Map.of(ALGORITHM, CheckAlgorithmParameters.parse(yaml))));
    wpcleanerRepository.setConfiguration(
        PageAnalysisTestFactory.WIKI, new WPCleanerConfiguration(generalProperties, Map.of()));
  }

  private static List<Replacement> textOrBold(final String text, final boolean automatic) {
    return List.of(new Replacement(text, automatic), new Replacement("'''" + text + "'''", false));
  }
}
