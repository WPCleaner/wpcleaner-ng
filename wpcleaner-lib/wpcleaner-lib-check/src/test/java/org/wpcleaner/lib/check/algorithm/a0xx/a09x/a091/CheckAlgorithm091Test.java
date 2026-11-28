package org.wpcleaner.lib.check.algorithm.a0xx.a09x.a091;

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
import org.wpcleaner.lib.check.ExternalView;
import org.wpcleaner.lib.check.Replacement;
import org.wpcleaner.lib.check.configuration.CheckAlgorithmParameters;
import org.wpcleaner.lib.check.configuration.CheckWikiConfiguration;
import org.wpcleaner.lib.check.configuration.CheckWikiConfigurationRepository;

@CheckSpringBootTest
class CheckAlgorithm091Test {

  private static final int ALGORITHM = 91;
  private static final String EN_URL = "https://en.wikipedia.org/wiki/";
  private static final String WIKTIONARY_URL = "https://fr.wiktionary.org/wiki/chat";
  private static final String WIKTIONARY_LINK = "[" + WIKTIONARY_URL + " chat]";
  private static final String GOOGLE_URL = "https://www.google.com/search?q=chat&hl=fr";
  private static final String GOOGLE_LINK = "[" + GOOGLE_URL + " chat]";

  @Autowired private CheckAlgorithm091 algorithm;
  @Autowired private CheckWikiConfigurationRepository checkWikiRepository;

  @BeforeEach
  void setUp() {
    givenLegacyConfiguration(Map.of());
  }

  @DisplayName("should fix manually links to articles of other languages")
  @Test
  void fixLanguageLinks() {
    // GIVEN
    final String withText = "[" + EN_URL + "Tour_Eiffel Eiffel tower]";
    final String bare = "https://de.wikipedia.org/wiki/Paris";
    final String doubled = "[[" + EN_URL + "London London]]";
    final String fragment = "[" + EN_URL + "Rome#History Rome]";
    final String attributes = "[" + EN_URL + "Oslo?oldid=1 Oslo]";
    final String mobile = "[https://en.m.wikipedia.org/wiki/Nantes Nantes]";
    final String protocolRelative = "[//en.wikipedia.org/wiki/Nice Nice]";
    final String text =
        String.join(
            " ",
            withText,
            bare,
            doubled,
            fragment,
            attributes,
            mobile,
            protocolRelative,
            "[https://en.x.wikipedia.org/wiki/Nice Nice]",
            "[https://m.en.wikipedia.org/wiki/Nice Nice]",
            "[https://en.wikipedia.org/w/index.php?title=Nice Nice]",
            "[https://fr.wikipedia.org/wiki/Lyon Lyon]",
            WIKTIONARY_LINK,
            GOOGLE_LINK);

    // WHEN
    final List<CheckError> errors = analyze("Lyon", text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(
            error(
                text, withText, "[[:en:Tour Eiffel|Eiffel tower]]", false, EN_URL + "Tour_Eiffel"),
            error(text, bare, "[[:de:Paris|Paris]]", false, bare),
            error(text, doubled, "[[:en:London|London]]", false, EN_URL + "London"),
            error(text, fragment, "[[:en:Rome#History|Rome]]", false, EN_URL + "Rome#History"),
            error(text, attributes, "[[:en:Oslo|Oslo]]", false, EN_URL + "Oslo?oldid=1"),
            error(
                text,
                mobile,
                "[[:en:Nantes|Nantes]]",
                false,
                "https://en.m.wikipedia.org/wiki/Nantes"),
            error(
                text,
                protocolRelative,
                "[[:en:Nice|Nice]]",
                false,
                "https://en.wikipedia.org/wiki/Nice"));
  }

  @DisplayName("should report only articles, not talk pages")
  @Test
  void ignoreTalkPages() {
    // GIVEN
    final String link = "[" + EN_URL + "Paris Paris]";

    // WHEN
    final List<CheckError> talkErrors = analyze("Discussion utilisateur:Lyon", link);
    final List<CheckError> userErrors = analyze("Utilisateur:Lyon", link);

    // THEN
    Assertions.assertThat(talkErrors).isEmpty();
    Assertions.assertThat(userErrors)
        .containsExactly(error(link, link, "[[:en:Paris|Paris]]", false, EN_URL + "Paris"));
  }

  @DisplayName("should report links to wikis without language when only_language is false")
  @Test
  void reportOtherWikisWithLegacyConfiguration() {
    // GIVEN
    givenLegacyConfiguration(
        Map.of(
            "error_091_only_language_frwiki",
            "false",
            "error_091_link_templates_frwiki",
            "Lien web|url|titre"));
    final String bare = "https://fr.wiktionary.org/wiki/chien";
    final String withoutText = "[https://fr.wiktionary.org/wiki/rat]";
    final String template = "{{Lien web|url=https://fr.wiktionary.org/wiki/loup|titre=Loup}}";
    final String english = "[" + EN_URL + "Paris Paris]";
    final String text =
        String.join(
            " ",
            WIKTIONARY_LINK,
            bare,
            withoutText,
            template,
            english,
            GOOGLE_LINK,
            "[https://example.org/ home]",
            "[https://example.m.org/wiki/Home home]");

    // WHEN
    final List<CheckError> errors = analyze("Lyon", text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(
            error(text, WIKTIONARY_LINK, "[[:wikt:chat|chat]]", true, WIKTIONARY_URL),
            error(text, bare, "[[:wikt:chien|chien]]", false, bare),
            error(
                text,
                withoutText,
                "[[:wikt:rat|rat]]",
                false,
                "https://fr.wiktionary.org/wiki/rat"),
            error(
                text,
                template,
                "[[:wikt:loup|Loup]]",
                false,
                "https://fr.wiktionary.org/wiki/loup"),
            error(text, english, "[[:en:Paris|Paris]]", false, EN_URL + "Paris"));
  }

  @DisplayName("should report links to non local wikis when only_local is false")
  @Test
  void reportNonLocalWikisWithYamlConfiguration() {
    // GIVEN
    givenYamlConfiguration("only_language: false\nonly_local: false");
    final String other = "[https://www.google.com/maps?q=chat chat]";
    final String text = String.join(" ", GOOGLE_LINK, other);

    // WHEN
    final List<CheckError> errors = analyze("Lyon", text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(error(text, GOOGLE_LINK, "[[:google:chat|chat]]", false, GOOGLE_URL));
  }

  @DisplayName("should use only the YAML configuration when it exists")
  @Test
  void ignoreLegacyWithYaml() {
    // GIVEN
    checkWikiRepository.setConfiguration(
        PageAnalysisTestFactory.WIKI,
        CheckWikiConfiguration.of(
                Map.of("error_091_prio_frwiki", "3", "error_091_only_language_frwiki", "false"),
                Map.of(),
                "frwiki")
            .withParameters(Map.of(ALGORITHM, CheckAlgorithmParameters.parse("only_local: true"))));

    // WHEN
    final List<CheckError> errors = analyze("Lyon", WIKTIONARY_LINK);

    // THEN
    Assertions.assertThat(errors).isEmpty();
  }

  @DisplayName("should replace the whole link template, with the legacy configuration")
  @Test
  void fixLinkTemplatesWithLegacyConfiguration() {
    // GIVEN
    givenLegacyConfiguration(Map.of("error_091_link_templates_frwiki", "Lien web|url|titre"));
    final String template = "{{Lien web|url=" + EN_URL + "Berlin|titre=Berlin city}}";
    final String text = "<ref>" + template + "</ref>";

    // WHEN
    final List<CheckError> errors = analyze("Lyon", text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(
            error(text, template, "[[:en:Berlin|Berlin city]]", false, EN_URL + "Berlin"));
  }

  @DisplayName("should replace the whole link template, with the YAML configuration")
  @Test
  void fixLinkTemplatesWithYamlConfiguration() {
    // GIVEN
    givenYamlConfiguration(
        "link_templates:\n  - template: Lien web\n    url: url\n    text: titre");
    final String template = "{{Lien web|url=" + EN_URL + "Berlin|titre=Berlin city}}";

    // WHEN
    final List<CheckError> errors = analyze("Lyon", template);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(
            error(template, template, "[[:en:Berlin|Berlin city]]", false, EN_URL + "Berlin"));
  }

  private List<CheckError> analyze(final String title, final String text) {
    return algorithm.analyze(PageAnalysisTestFactory.createPageAnalysis(title, text));
  }

  private static CheckError error(
      final String text,
      final String link,
      final String replacement,
      final boolean automatic,
      final String url) {
    final int begin = text.indexOf(link);
    return new CheckError(
        ALGORITHM,
        begin,
        begin + link.length(),
        List.of(new Replacement(replacement, automatic), new ExternalView("External Viewer", url)));
  }

  private void givenLegacyConfiguration(final Map<String, String> parameters) {
    final Map<String, String> properties = new HashMap<>(parameters);
    properties.put("error_091_prio_frwiki", "3");
    checkWikiRepository.setConfiguration(
        PageAnalysisTestFactory.WIKI, CheckWikiConfiguration.of(properties, Map.of(), "frwiki"));
  }

  private void givenYamlConfiguration(final String yaml) {
    checkWikiRepository.setConfiguration(
        PageAnalysisTestFactory.WIKI,
        CheckWikiConfiguration.of(Map.of("error_091_prio_frwiki", "3"), Map.of(), "frwiki")
            .withParameters(Map.of(ALGORITHM, CheckAlgorithmParameters.parse(yaml))));
  }
}
