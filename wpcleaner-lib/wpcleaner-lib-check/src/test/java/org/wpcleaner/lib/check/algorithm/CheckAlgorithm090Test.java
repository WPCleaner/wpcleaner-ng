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
import org.wpcleaner.api.repository.specialpage.SpecialPage;
import org.wpcleaner.api.repository.specialpage.SpecialPageRepository;
import org.wpcleaner.lib.check.CheckError;
import org.wpcleaner.lib.check.CheckSpringBootTest;
import org.wpcleaner.lib.check.Replacement;
import org.wpcleaner.lib.check.configuration.CheckAlgorithmParameters;
import org.wpcleaner.lib.check.configuration.CheckWikiConfiguration;
import org.wpcleaner.lib.check.configuration.CheckWikiConfigurationRepository;

@CheckSpringBootTest
class CheckAlgorithm090Test {

  private static final int ALGORITHM = 90;
  private static final String WIKI_URL = "https://fr.wikipedia.org/wiki/";
  private static final String INDEX_URL = "https://fr.wikipedia.org/w/index.php?title=";

  @Autowired private CheckAlgorithm090 algorithm;
  @Autowired private CheckWikiConfigurationRepository checkWikiRepository;
  @Autowired private SpecialPageRepository specialPageRepository;

  @BeforeEach
  void setUp() {
    givenLegacyConfiguration(Map.of());
    specialPageRepository.addSpecialPage(
        PageAnalysisTestFactory.WIKI,
        new SpecialPage("Prefixindex", List.of("Index", "PrefixIndex")));
  }

  @DisplayName("should automatically replace external links to articles by internal links")
  @Test
  void fixLinksWithText() {
    // GIVEN
    final String text =
        "["
            + WIKI_URL
            + "Paris la capitale] [["
            + WIKI_URL
            + "Tour_Eiffel Tour Eiffel]]"
            + " ["
            + WIKI_URL
            + "Paris Parisiens] ["
            + WIKI_URL
            + "Cat%C3%A9gorie:Lyon |Lyon]";

    // WHEN
    final List<CheckError> errors = analyze(text);

    // THEN
    final int second = text.indexOf("[[");
    final int third = text.indexOf("[" + WIKI_URL + "Paris Parisiens");
    final int fourth = text.lastIndexOf('[');
    Assertions.assertThat(errors)
        .containsExactly(
            new CheckError(
                ALGORITHM,
                0,
                second - 1,
                List.of(
                    new Replacement("[[Paris|la capitale]]", true),
                    new Replacement("la capitale", false))),
            new CheckError(
                ALGORITHM,
                second,
                third - 1,
                List.of(
                    new Replacement("[[Tour Eiffel]]", true),
                    new Replacement("Tour Eiffel", false))),
            new CheckError(
                ALGORITHM,
                third,
                fourth - 1,
                List.of(
                    new Replacement("[[Paris]]iens", true), new Replacement("Parisiens", false))),
            new CheckError(
                ALGORITHM,
                fourth,
                text.length(),
                List.of(
                    new Replacement("[[:Catégorie:Lyon|Lyon]]", true),
                    new Replacement("Lyon", false))));
  }

  @DisplayName("should fix manually links to articles in timelines")
  @Test
  void fixTimelineLinks() {
    // GIVEN
    final String embedded = "[" + WIKI_URL + "Tour_Eiffel|Tour]";
    final String withoutText = "[" + WIKI_URL + "Paris]";
    final String attribute = "link:" + WIKI_URL + "Nantes";
    final String withAttributes = "[" + WIKI_URL + "Lyon?action=edit|Lyon]";
    final String text =
        "<timeline>\n  bar:A text:\""
            + embedded
            + " "
            + withoutText
            + "\"\n  bar:B text:Nantes "
            + attribute
            + "\n  bar:C text:\""
            + withAttributes
            + " [[Rennes]] [https://example.org/|Ex]\"\n</timeline>";

    // WHEN
    final List<CheckError> errors = analyze(text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(
            error(text, embedded, new Replacement("[[Tour Eiffel|Tour]]", false)),
            error(text, withoutText, new Replacement("[[Paris]]", false)),
            error(text, attribute),
            error(text, withAttributes));
  }

  private static CheckError error(
      final String text, final String link, final Replacement... replacements) {
    return new CheckError(
        ALGORITHM, text.indexOf(link), text.indexOf(link) + link.length(), List.of(replacements));
  }

  @DisplayName("should recognize the URLs of the articles of the wiki")
  @Test
  void recognizeArticleUrls() {
    // GIVEN
    final String text =
        "["
            + INDEX_URL
            + "Tour__Eiffel A] ["
            + WIKI_URL
            + "Fichier:A.jpg A2] ["
            + WIKI_URL
            + "/Paris H] ["
            + INDEX_URL
            + "Paris&title=Lyon I] [//fr.wikipedia.org/wiki/Nantes J] ["
            + WIKI_URL
            + "Paris Paris-Nord] ["
            + WIKI_URL
            + "Paris#Histoire Paris] ["
            + WIKI_URL
            + "Paris paris] ["
            + WIKI_URL
            + "Catégorie:Lyon Catégorie:Lyon] ["
            + WIKI_URL
            + "Nice |] [https://FR.m.wikipedia.org/wiki/Paris#Histoire B]"
            + " [http:///fr.wikipedia.org/wiki/Lyon/ C] [https://en.wikipedia.org/wiki/Paris D]"
            + " [https://fr.wikipedia.org/w/index.php?oldid=1 E] [https://fr.wikipedia.org/wiki/ F]"
            + " [https://fr.wikipedia.org/autre/Paris G]";

    // WHEN
    final List<CheckError> errors = analyze(text);

    // THEN
    Assertions.assertThat(errors)
        .extracting(error -> error.replacements().getFirst().text())
        .containsExactly(
            "[[Tour Eiffel|A]]",
            "[[:Fichier:A.jpg|A2]]",
            "[[:/Paris|H]]",
            "[[Lyon|I]]",
            "[[Nantes|J]]",
            "[[Paris|Paris-Nord]]",
            "[[Paris#Histoire|Paris]]",
            "[[paris]]",
            "[[:Catégorie:Lyon|Catégorie:Lyon]]",
            "[[Nice]]",
            "[[Paris#Histoire|B]]",
            "[[Lyon|C]]");
  }

  @DisplayName("should fix manually links to the page itself or with attributes")
  @Test
  void fixManually() {
    // GIVEN
    final String text =
        "["
            + WIKI_URL
            + "Lyon A] ["
            + INDEX_URL
            + "Paris&action=edit B]"
            + " ["
            + WIKI_URL
            + "Paris#:~:text=x C] ["
            + INDEX_URL
            + "Paris&venotify=restored D]";

    // WHEN
    final List<CheckError> errors = analyze(text);

    // THEN
    Assertions.assertThat(errors)
        .extracting(error -> error.replacements().getFirst().automatic())
        .containsExactly(false, false, false, false);
  }

  @DisplayName("should fix automatically links to red links or created pages")
  @Test
  void fixRedLinks() {
    // GIVEN
    final String text =
        "[" + INDEX_URL + "Paris&redlink=1 A] [" + INDEX_URL + "Paris&venotify=created B]";

    // WHEN
    final List<CheckError> errors = analyze(text);

    // THEN
    Assertions.assertThat(errors)
        .extracting(error -> error.replacements().getFirst())
        .containsExactly(
            new Replacement("[[Paris|A]]", true), new Replacement("[[Paris|B]]", true));
  }

  @DisplayName("should fix manually in user pages and ignore talk pages and code")
  @Test
  void ignoreTalkPagesAndCode() {
    // GIVEN
    final String text =
        "["
            + WIKI_URL
            + "Paris A] <code>"
            + WIKI_URL
            + "Paris</code> [[Fichier:A.jpg|lien="
            + WIKI_URL
            + "Fichier:A.jpg]]";

    // WHEN
    final List<CheckError> userErrors = analyze("Utilisateur:Bob", text);
    final List<CheckError> talkErrors = analyze("Discussion utilisateur:Bob", text);
    final List<CheckError> specialErrors = analyze("Spécial:Test", text);

    // THEN
    Assertions.assertThat(userErrors)
        .extracting(CheckError::replacements)
        .containsExactly(
            List.of(new Replacement("[[Paris|A]]", false), new Replacement("A", false)),
            List.of(new Replacement("", false)));
    Assertions.assertThat(talkErrors).isEmpty();
    Assertions.assertThat(specialErrors).isEmpty();
  }

  @DisplayName("should use the text before a link without text")
  @Test
  void fixLinksWithoutText() {
    // GIVEN
    final String text = "Paris [" + WIKI_URL + "Paris] et " + WIKI_URL + "Lyon";

    // WHEN
    final List<CheckError> errors = analyze(text);

    // THEN
    final int second = text.lastIndexOf("https");
    Assertions.assertThat(errors)
        .containsExactly(
            new CheckError(
                ALGORITHM, 0, text.indexOf(" et"), List.of(new Replacement("[[Paris]]", true))),
            new CheckError(
                ALGORITHM, second, text.length(), List.of(new Replacement("[[Lyon]]", false))));
  }

  @DisplayName("should delete links of images to their own page")
  @Test
  void fixImageLinks() {
    // GIVEN
    final String text =
        "[[Fichier:A.jpg|vignette|lien="
            + WIKI_URL
            + "Fichier:A.jpg|A]] [[Fichier:B.jpg|lien="
            + WIKI_URL
            + "Paris]] [[Fichier:C.jpg|lien="
            + INDEX_URL
            + "Fichier:C.jpg&action=history]] [[Fichier:D.jpg|lien="
            + WIKI_URL
            + "Fichier:D.jpg#x]]";

    // WHEN
    final List<CheckError> errors = analyze(text);

    // THEN
    final int first = text.indexOf("|lien");
    final int second = text.indexOf("|lien", first + 1);
    Assertions.assertThat(errors)
        .extracting(CheckError::begin, CheckError::end, CheckError::replacements)
        .containsExactly(
            Assertions.tuple(first, text.indexOf("|A]]"), List.of(new Replacement("", true))),
            Assertions.tuple(
                second, text.indexOf("]] [[Fichier:C"), List.of(new Replacement("", false))),
            Assertions.tuple(
                text.indexOf("|lien", second + 1),
                text.indexOf("]] [[Fichier:D"),
                List.of(new Replacement("", false))),
            Assertions.tuple(
                text.lastIndexOf("|lien"), text.length() - 2, List.of(new Replacement("", false))));
  }

  @DisplayName("should report internal links to the wiki host without replacement")
  @Test
  void reportInternalLinksToHost() {
    // GIVEN
    final String text = "[" + WIKI_URL + "Paris A] [[fr.wikipedia.org/wiki/Paris]] [[Paris]]";

    // WHEN
    final List<CheckError> errors = analyze(text);

    // THEN
    Assertions.assertThat(errors)
        .extracting(CheckError::begin, CheckError::replacements)
        .containsExactly(
            Assertions.tuple(
                0, List.of(new Replacement("[[Paris|A]]", true), new Replacement("A", false))),
            Assertions.tuple(text.indexOf("[["), List.of()));
  }

  @DisplayName("should delete references repeating the link or the text before")
  @Test
  void fixReferences() {
    // GIVEN
    final String text =
        "[[Paris]] <ref>["
            + WIKI_URL
            + "Paris]</ref> Lyon<ref>"
            + WIKI_URL
            + "Lyon</ref>"
            + " Texte<ref>["
            + WIKI_URL
            + "Nice Nice] </ref> [[Lyon]] <ref>["
            + WIKI_URL
            + "Marseille Marseille]</ref><ref>Voir ["
            + WIKI_URL
            + "Toulon Toulon]</ref><ref>["
            + WIKI_URL
            + "Brest Brest], p. 2</ref>";

    // WHEN
    final List<CheckError> errors = analyze(text);

    // THEN
    final int second = text.indexOf("Lyon<ref>");
    final int third = text.indexOf("[" + WIKI_URL + "Nice");
    Assertions.assertThat(errors.subList(0, 3))
        .containsExactly(
            new CheckError(
                ALGORITHM, 0, text.indexOf(" Lyon"), List.of(new Replacement("[[Paris]]", false))),
            new CheckError(
                ALGORITHM,
                second,
                text.indexOf(" Texte"),
                List.of(new Replacement("[[Lyon]]", false))),
            new CheckError(
                ALGORITHM,
                third,
                text.indexOf(" </ref> [[Lyon]]"),
                List.of(new Replacement("[[Nice]]", false), new Replacement("Nice", false))));
    Assertions.assertThat(errors.subList(3, errors.size()))
        .extracting(CheckError::replacements)
        .containsExactly(
            List.of(new Replacement("[[Marseille]]", false), new Replacement("Marseille", false)),
            List.of(new Replacement("[[Toulon]]", true), new Replacement("Toulon", false)),
            List.of(new Replacement("[[Brest]]", true), new Replacement("Brest", false)));
  }

  @DisplayName("should use the templates given by legacy properties")
  @Test
  void useTemplatesFromLegacy() {
    // GIVEN
    givenLegacyConfiguration(
        Map.of(
            "error_090_history_templates_frwiki", "Historique||2",
            "error_090_oldid_templates_frwiki", "Permalien|page|version|texte",
            "error_090_prefix_index_templates_frwiki", "Commence par|1|texte",
            "error_090_link_templates_frwiki", "Lien|1|2\nLien web|url|titre"));

    // WHEN
    final List<CheckError> errors = analyze(createTextWithTemplates());

    // THEN
    assertTemplateReplacements(errors);
  }

  @DisplayName("should use the templates given by YAML")
  @Test
  void useTemplatesFromYaml() {
    // GIVEN
    givenYamlConfiguration(
        """
        history_templates:
          - template: Historique
            article: 1
            text: 2
        oldid_templates:
          - template: Permalien
            article: page
            oldid: version
            text: texte
        prefix_index_templates:
          - template: Commence par
            article: 1
            text: texte
        link_templates:
          - template: Lien
            url: 1
            text: 2
          - template: Lien web
            url: url
            text: titre
        """);

    // WHEN
    final List<CheckError> errors = analyze(createTextWithTemplates());

    // THEN
    assertTemplateReplacements(errors);
  }

  @DisplayName("should use only the YAML configuration when it exists")
  @Test
  void ignoreLegacyWithYaml() {
    // GIVEN
    checkWikiRepository.setConfiguration(
        PageAnalysisTestFactory.WIKI,
        CheckWikiConfiguration.of(
                Map.of(
                    "error_090_prio_frwiki",
                    "3",
                    "error_090_history_templates_frwiki",
                    "Historique"),
                Map.of(),
                "frwiki")
            .withParameters(
                Map.of(ALGORITHM, CheckAlgorithmParameters.parse("link_templates: []"))));

    // WHEN
    final List<CheckError> errors = analyze("[" + INDEX_URL + "Paris&action=history A]");

    // THEN
    Assertions.assertThat(errors)
        .extracting(error -> error.replacements().getFirst().text())
        .containsExactly("[[Paris|A]]");
  }

  private static String createTextWithTemplates() {
    return "["
        + INDEX_URL
        + "Paris&action=history historique] ["
        + INDEX_URL
        + "Paris&oldid=12 version] ["
        + WIKI_URL
        + "Spécial:Index?prefix=Paris/+&namespace=0 pages] {{Lien web|url="
        + WIKI_URL
        + "Paris|titre=Paris}} ["
        + INDEX_URL
        + "Paris&oldid=12&diff=prev x] Paris ["
        + INDEX_URL
        + "Paris&action=history] ["
        + INDEX_URL
        + "A%3Db&action=history c] ["
        + WIKI_URL
        + "Spécial:Index?prefix=Lyon&from=x pages] {{Lien web|lien="
        + WIKI_URL
        + "Lyon|titre=T}} {{Lien|"
        + WIKI_URL
        + "Nice|Nice}} {{Autre|url="
        + WIKI_URL
        + "Lyon|titre=T}} {{Lien web|url=https://example.org/|titre=["
        + WIKI_URL
        + "Toulon x]}} {{Autre|{{Lien web|url="
        + WIKI_URL
        + "Brest|titre=Brest}}}} ["
        + INDEX_URL
        + "Spécial:Index&prefix= tout] ["
        + INDEX_URL
        + "Paris&action=history&x=1 h] ["
        + INDEX_URL
        + "Index&prefix=A i]";
  }

  private static void assertTemplateReplacements(final List<CheckError> errors) {
    Assertions.assertThat(errors)
        .extracting(CheckError::replacements)
        .containsExactly(
            List.of(
                new Replacement("{{Historique|Paris|historique}}", false),
                new Replacement("[[Paris|historique]]", false),
                new Replacement("historique", false)),
            List.of(
                new Replacement("{{Permalien|page=Paris|version=12|texte=version}}", false),
                new Replacement("[[Paris|version]]", false),
                new Replacement("version", false)),
            List.of(
                new Replacement("{{Commence par|Paris/|texte=pages}}", false),
                new Replacement("[[Spécial:Index|pages]]", false),
                new Replacement("pages", false)),
            List.of(new Replacement("[[Paris]]", true), new Replacement("Paris", false)),
            List.of(new Replacement("[[Paris|x]]", false), new Replacement("x", false)),
            List.of(
                new Replacement("{{Historique|Paris}}", false),
                new Replacement("[[Paris]]", false)),
            List.of(
                new Replacement("{{Historique|1=A=b|c}}", false),
                new Replacement("[[A=b|c]]", false),
                new Replacement("c", false)),
            List.of(
                new Replacement("[[Spécial:Index|pages]]", false), new Replacement("pages", false)),
            List.of(new Replacement("[[Lyon]]", false)),
            List.of(new Replacement("[[Nice]]", true), new Replacement("Nice", false)),
            List.of(new Replacement("[[Lyon]]", false)),
            List.of(new Replacement("[[Toulon|x]]", true), new Replacement("x", false)),
            List.of(new Replacement("[[Brest]]", true), new Replacement("Brest", false)),
            List.of(
                new Replacement("[[Spécial:Index|tout]]", false), new Replacement("tout", false)),
            List.of(new Replacement("[[Paris|h]]", false), new Replacement("h", false)),
            List.of(new Replacement("[[Index|i]]", false), new Replacement("i", false)));
  }

  private List<CheckError> analyze(final String text) {
    return analyze("Lyon", text);
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
        CheckWikiConfiguration.of(Map.of("error_090_prio_frwiki", "3"), Map.of(), "frwiki")
            .withParameters(Map.of(ALGORITHM, CheckAlgorithmParameters.parse(yaml))));
  }
}
