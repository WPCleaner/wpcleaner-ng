package org.wpcleaner.lib.check.algorithm.a0xx.a00x.a003;

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
import org.wpcleaner.lib.check.CheckError;
import org.wpcleaner.lib.check.CheckSpringBootTest;
import org.wpcleaner.lib.check.Replacement;
import org.wpcleaner.lib.check.configuration.CheckAlgorithmParameters;
import org.wpcleaner.lib.check.configuration.CheckWikiConfiguration;
import org.wpcleaner.lib.check.configuration.CheckWikiConfigurationRepository;

@CheckSpringBootTest
class CheckAlgorithm003Test {

  private static final int ALGORITHM = 3;
  private static final String INSERT = "{{Références}}";
  private static final Map<String, String> INSERTION_PROPERTIES =
      Map.of(
          "error_003_insert_frwiki", INSERT,
          "error_003_titles_frwiki", "Notes et références\nRéférences",
          "error_003_before_titles_frwiki", "Liens externes\nVoir aussi",
          "error_003_before_templates_frwiki", "Portail");

  @Autowired private CheckAlgorithm003 algorithm;
  @Autowired private CheckWikiConfigurationRepository checkWikiRepository;

  @BeforeEach
  void setUp() {
    givenConfiguration(Map.of());
  }

  @DisplayName("should report the last reference when there's no references tag")
  @Test
  void reportLastReference() {
    // GIVEN
    final String text = "A<ref>a</ref> B<ref name=\"b\" /> C.";

    // WHEN
    final List<CheckError> errors = analyze(text);

    // THEN
    final int last = text.indexOf("<ref name");
    Assertions.assertThat(errors)
        .containsExactly(new CheckError(ALGORITHM, last, last + 16, List.of()));
  }

  @DisplayName("should not report pages without references or with a references tag")
  @Test
  void ignorePagesWithReferencesTag() {
    // GIVEN
    final List<String> texts =
        List.of(
            "No reference.",
            "A<ref>a</ref>\n<references />",
            "A<ref>a</ref>\n<REFERENCES/>",
            "A<ref>a</ref>\n<references></references>",
            "A<ref name=\"a\" />\n<references>\n<ref name=\"a\">a</ref>\n</references>",
            "<nowiki><ref>a</ref></nowiki> <!-- <ref>b</ref> -->");

    // WHEN
    final List<List<CheckError>> errors = texts.stream().map(this::analyze).toList();

    // THEN
    Assertions.assertThat(errors)
        .allSatisfy(pageErrors -> Assertions.assertThat(pageErrors).isEmpty());
  }

  @DisplayName("should report references in templates and ignore pages with a references template")
  @Test
  void useTemplates() {
    // GIVEN
    givenConfiguration(
        Map.of("error_003_ref_templates_frwiki", "Sfn", "error_003_templates_frwiki", "Reflist"));
    final String text = "A<ref>a</ref> B{{sfn|p=1}}.";

    // WHEN
    final List<CheckError> errors = analyze(text);
    final List<CheckError> withReflist = analyze(text + "\n{{reflist}}");

    // THEN
    final int last = text.indexOf("{{sfn");
    Assertions.assertThat(errors)
        .containsExactly(new CheckError(ALGORITHM, last, last + 11, List.of()));
    Assertions.assertThat(withReflist).isEmpty();
  }

  @DisplayName("should match references templates as regular expressions, like Check Wiki")
  @Test
  void useTemplateRegularExpressions() {
    // GIVEN
    givenConfiguration(
        Map.of(
            "error_003_templates_frwiki",
            "reflist\nRE\\s*[|}]\nSchubert's compositions \\(references\\)\nInvalid("));
    final List<String> ignoredTexts =
        List.of(
            "A<ref>a</ref>\n{{Reflist}}",
            "A<ref>a</ref>\n{{ reflist2}}",
            "A<ref>a</ref>\n{{Modèle:Reflist}}",
            "A<ref>a</ref>\n{{RE}}",
            "A<ref>a</ref>\n{{rE |a}}",
            "A<ref>a</ref>\n{{Schubert's compositions (references)}}");
    final List<String> reportedTexts =
        List.of("A<ref>a</ref>\n{{REF}}", "A<ref>a</ref>\n{{Re}}", "A<ref>a</ref>\n{{Invalid}}");

    // WHEN
    final List<List<CheckError>> ignoredErrors = ignoredTexts.stream().map(this::analyze).toList();
    final List<List<CheckError>> reportedErrors =
        reportedTexts.stream().map(this::analyze).toList();

    // THEN
    Assertions.assertThat(ignoredErrors)
        .allSatisfy(pageErrors -> Assertions.assertThat(pageErrors).isEmpty());
    Assertions.assertThat(reportedErrors)
        .allSatisfy(
            pageErrors ->
                Assertions.assertThat(pageErrors)
                    .containsExactly(new CheckError(ALGORITHM, 1, 13, List.of())));
  }

  @DisplayName("should read regular expressions of references templates from YAML")
  @Test
  void readYamlTemplateRegularExpressions() {
    // GIVEN
    final String yaml =
        """
        templates:
          - RE\\s*[|}]
        """;
    checkWikiRepository.setConfiguration(
        PageAnalysisTestFactory.WIKI,
        CheckWikiConfiguration.of(
                Map.of("error_003_templates_frwiki", "Reflist"), Map.of(), "frwiki")
            .withParameters(Map.of(ALGORITHM, CheckAlgorithmParameters.parse(yaml))));

    // WHEN
    final List<CheckError> withRe = analyze("A<ref>a</ref>\n{{RE|a}}");
    final List<CheckError> withReflist = analyze("A<ref>a</ref>\n{{Reflist}}");

    // THEN
    Assertions.assertThat(withRe).isEmpty();
    Assertions.assertThat(withReflist).containsExactly(new CheckError(ALGORITHM, 1, 13, List.of()));
  }

  @DisplayName("should use references templates only when templates is missing")
  @Test
  void useReferencesTemplates() {
    // GIVEN
    givenConfiguration(Map.of("error_003_references_templates_frwiki", "Reflist"));
    final String text = "A<ref>a</ref>\n{{Reflist}}";

    // WHEN
    final List<CheckError> errors = analyze(text);
    givenConfiguration(
        Map.of(
            "error_003_references_templates_frwiki", "Reflist",
            "error_003_templates_frwiki", "Références"));
    final List<CheckError> errorsWithTemplates = analyze(text);

    // THEN
    Assertions.assertThat(errors).isEmpty();
    Assertions.assertThat(errorsWithTemplates)
        .containsExactly(new CheckError(ALGORITHM, 1, 13, List.of()));
  }

  @DisplayName("should suggest to close an unclosed references tag")
  @Test
  void closeReferencesTag() {
    // GIVEN
    givenConfiguration(INSERTION_PROPERTIES);
    final String single = "A<ref>a</ref>\n<references>";
    final String twice = "A<ref>a</ref>\n<references>\n<references>";

    // WHEN
    final List<CheckError> singleErrors = analyze(single);
    final List<CheckError> twiceErrors = analyze(twice);

    // THEN
    Assertions.assertThat(singleErrors)
        .containsExactly(
            new CheckError(ALGORITHM, 1, 13, List.of()),
            new CheckError(ALGORITHM, 14, 26, List.of(new Replacement("<references />", false))));
    Assertions.assertThat(twiceErrors)
        .containsExactly(
            new CheckError(ALGORITHM, 1, 13, List.of()),
            new CheckError(ALGORITHM, 14, 26, List.of()),
            new CheckError(ALGORITHM, 27, 39, List.of()));
  }

  @DisplayName("should fix automatically an incorrect references end tag")
  @Test
  void fixIncorrectEndTag() {
    // GIVEN
    final String text =
        "A<ref name=\"a\" />\n<references>\n<ref name=\"a\">a</ref>\n</references/>";

    // WHEN
    final List<CheckError> errors = analyze(text);

    // THEN
    final int endTag = text.indexOf("</references/>");
    final int lastReference = text.indexOf("<ref name=\"a\">");
    Assertions.assertThat(errors)
        .containsExactly(
            new CheckError(ALGORITHM, lastReference, lastReference + 21, List.of()),
            new CheckError(ALGORITHM, 18, 30, List.of(new Replacement("<references />", false))),
            new CheckError(
                ALGORITHM, endTag, endTag + 14, List.of(new Replacement("</references>", true))));
  }

  @DisplayName("should suggest to insert references in sections, the first one automatically")
  @Test
  void suggestInsertions() {
    // GIVEN
    givenConfiguration(INSERTION_PROPERTIES);
    final String text =
        "A<ref>a</ref>\n\n== notes et références ==\n\n=== Liens externes ===\n"
            + "Text {{Portail|Paris}}\n[[Catégorie:Paris]]";

    // WHEN
    final List<CheckError> errors = analyze(text);

    // THEN
    final int title = text.indexOf("== notes");
    final int beforeTitle = text.indexOf("=== Liens");
    final int template = text.indexOf("{{Portail");
    Assertions.assertThat(errors)
        .containsExactly(
            new CheckError(ALGORITHM, 1, 13, List.of()),
            new CheckError(
                ALGORITHM,
                title,
                title + 25,
                List.of(new Replacement("== notes et références ==\n" + INSERT, true))),
            new CheckError(
                ALGORITHM,
                beforeTitle,
                beforeTitle + 22,
                List.of(
                    new Replacement(
                        "=== Notes et références ===\n" + INSERT + "\n\n=== Liens externes ===",
                        false))),
            new CheckError(
                ALGORITHM,
                template,
                template + 17,
                List.of(
                    new Replacement(
                        "\n\n== Notes et références ==\n" + INSERT + "\n\n{{Portail|Paris}}",
                        false))));
  }

  @DisplayName("should not insert references automatically before the last reference")
  @Test
  void suggestInsertionBeforeLastReference() {
    // GIVEN
    givenConfiguration(INSERTION_PROPERTIES);
    final String inTitle = "== Références ==\nA<ref>a</ref>";
    final String beforeTitle = "== Voir aussi ==\nA<ref>a</ref>";
    final String beforeTemplate = "{{Portail}}\nA<ref>a</ref>";

    // WHEN
    final List<CheckError> inTitleErrors = analyze(inTitle);
    final List<CheckError> beforeTitleErrors = analyze(beforeTitle);
    final List<CheckError> beforeTemplateErrors = analyze(beforeTemplate);

    // THEN
    Assertions.assertThat(inTitleErrors)
        .containsExactly(
            new CheckError(ALGORITHM, 18, 30, List.of()),
            new CheckError(
                ALGORITHM, 0, 16, List.of(new Replacement("== Références ==\n" + INSERT, false))));
    Assertions.assertThat(beforeTitleErrors)
        .containsExactly(
            new CheckError(ALGORITHM, 18, 30, List.of()),
            new CheckError(
                ALGORITHM,
                0,
                16,
                List.of(
                    new Replacement(
                        "== Notes et références ==\n" + INSERT + "\n\n== Voir aussi ==", false))));
    Assertions.assertThat(beforeTemplateErrors)
        .containsExactly(
            new CheckError(ALGORITHM, 13, 25, List.of()),
            new CheckError(
                ALGORITHM,
                0,
                11,
                List.of(
                    new Replacement(
                        "== Notes et références ==\n" + INSERT + "\n\n{{Portail}}", false))));
  }

  @DisplayName("should insert references automatically only in the main namespace")
  @Test
  void suggestInsertionInMainNamespace() {
    // GIVEN
    givenConfiguration(INSERTION_PROPERTIES);
    final String text = "A<ref>a</ref>\n== Voir aussi ==";

    // WHEN
    final List<CheckError> errors =
        algorithm.analyze(PageAnalysisTestFactory.createPageAnalysis("Utilisateur:A", text));

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(
            new CheckError(ALGORITHM, 1, 13, List.of()),
            new CheckError(
                ALGORITHM,
                14,
                30,
                List.of(
                    new Replacement(
                        "== Notes et références ==\n" + INSERT + "\n\n== Voir aussi ==", false))));
  }

  @DisplayName("should not insert references automatically before a template following a template")
  @Test
  void suggestInsertionBeforeTemplateAfterTemplate() {
    // GIVEN
    givenConfiguration(INSERTION_PROPERTIES);
    final String text = "A<ref>a</ref>\n{{Palette}} {{Portail|Paris}}";

    // WHEN
    final List<CheckError> errors = analyze(text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(
            new CheckError(ALGORITHM, 1, 13, List.of()),
            new CheckError(
                ALGORITHM,
                26,
                43,
                List.of(
                    new Replacement(
                        "\n\n== Notes et références ==\n" + INSERT + "\n\n{{Portail|Paris}}",
                        false))));
  }

  @DisplayName("should suggest to insert references before categories and default sort")
  @Test
  void suggestInsertionBeforeCategories() {
    // GIVEN
    givenConfiguration(INSERTION_PROPERTIES);
    final String withDefaultSort = "A<ref>a</ref>\n{{DEFAULTSORT:A}}\n[[Catégorie:B]]";
    final String withCategory = "A<ref>a</ref>\n[[Catégorie:B]]\n{{DEFAULTSORT:A}}";

    // WHEN
    final List<CheckError> defaultSortErrors = analyze(withDefaultSort);
    final List<CheckError> categoryErrors = analyze(withCategory);

    // THEN
    Assertions.assertThat(defaultSortErrors)
        .containsExactly(
            new CheckError(ALGORITHM, 1, 13, List.of()),
            new CheckError(
                ALGORITHM,
                14,
                31,
                List.of(
                    new Replacement(
                        "== Notes et références ==\n" + INSERT + "\n\n{{DEFAULTSORT:A}}", false))));
    Assertions.assertThat(categoryErrors)
        .containsExactly(
            new CheckError(ALGORITHM, 1, 13, List.of()),
            new CheckError(
                ALGORITHM,
                14,
                29,
                List.of(
                    new Replacement(
                        "== Notes et références ==\n" + INSERT + "\n\n[[Catégorie:B]]", false))));
  }

  @DisplayName("should insert references without a title when no title is configured")
  @Test
  void suggestInsertionWithoutTitle() {
    // GIVEN
    givenConfiguration(Map.of("error_003_insert_frwiki", INSERT));
    final String text = "A<ref>a</ref>\n[[Catégorie:B]]";

    // WHEN
    final List<CheckError> errors = analyze(text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(
            new CheckError(ALGORITHM, 1, 13, List.of()),
            new CheckError(
                ALGORITHM,
                14,
                29,
                List.of(new Replacement(INSERT + "\n\n[[Catégorie:B]]", false))));
  }

  @DisplayName("should read parameters from YAML instead of legacy properties")
  @Test
  void readYamlParameters() {
    // GIVEN
    final String yaml =
        """
        insert:
          - "{{Notes}}"
        titles:
          - Notes
        ref_templates:
          - Sfn
        templates:
          - Reflist
        before_titles: []
        before_templates: []
        """;
    checkWikiRepository.setConfiguration(
        PageAnalysisTestFactory.WIKI,
        CheckWikiConfiguration.of(INSERTION_PROPERTIES, Map.of(), "frwiki")
            .withParameters(Map.of(ALGORITHM, CheckAlgorithmParameters.parse(yaml))));
    final String text = "A{{Sfn|p=1}}\n== Notes ==\n== Voir aussi ==";

    // WHEN
    final List<CheckError> errors = analyze(text);
    final List<CheckError> withReflist = analyze(text + "\n{{Reflist}}");

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(
            new CheckError(ALGORITHM, 1, 12, List.of()),
            new CheckError(
                ALGORITHM, 13, 24, List.of(new Replacement("== Notes ==\n{{Notes}}", true))));
    Assertions.assertThat(withReflist).isEmpty();
  }

  private List<CheckError> analyze(final String text) {
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis("Paris", text);
    return algorithm.analyze(analysis);
  }

  private void givenConfiguration(final Map<String, String> properties) {
    checkWikiRepository.setConfiguration(
        PageAnalysisTestFactory.WIKI, CheckWikiConfiguration.of(properties, Map.of(), "frwiki"));
  }
}
