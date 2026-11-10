package org.wpcleaner.lib.check.algorithm;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.wpcleaner.api.ApiSpringBootTest;
import org.wpcleaner.api.analysis.PageAnalysisTestFactory;
import org.wpcleaner.api.repository.configuration.WPCleanerConfiguration;
import org.wpcleaner.api.repository.configuration.WPCleanerConfigurationRepository;
import org.wpcleaner.lib.check.CheckError;
import org.wpcleaner.lib.check.Replacement;

@ApiSpringBootTest
class CheckAlgorithm064Test {

  private static final int ALGORITHM = 64;

  @Autowired private CheckAlgorithm064 algorithm;
  @Autowired private WPCleanerConfigurationRepository wpcleanerRepository;

  @BeforeEach
  void setUp() {
    givenApostropheTemplate(null);
  }

  @DisplayName("should automatically fix links whose text is the target")
  @Test
  void fixLinksEqualToText() {
    // GIVEN
    final String text =
        "[[Paris|Paris]] [[paris|Paris]] [[Paris|paris]] [[:Catégorie:Lyon|Catégorie:Lyon]]";

    // WHEN
    final List<CheckError> errors = analyze("Lyon", text);

    // THEN
    final int second = text.indexOf("[[paris");
    final int third = text.indexOf("[[Paris|paris");
    final int fourth = text.indexOf("[[:");
    Assertions.assertThat(errors)
        .containsExactly(
            error(0, "[[Paris|Paris]]", new Replacement("[[Paris]]", true)),
            error(second, "[[paris|Paris]]", new Replacement("[[Paris]]", true)),
            error(third, "[[Paris|paris]]", new Replacement("[[paris]]", true)),
            error(
                fourth,
                "[[:Catégorie:Lyon|Catégorie:Lyon]]",
                new Replacement("[[:Catégorie:Lyon]]", true)));
  }

  @DisplayName("should not report links whose text differs from the target")
  @Test
  void ignoreOtherLinks() {
    // GIVEN
    final String text =
        "[[Paris]] [[Paris|Lyon]] [[Paris#Histoire|Paris]] [[Paris|.Paris]] [[Paris|Paris']]"
            + " [[Paris|Paris|Lyon]] [[Paris|{{Paris}}]] [[Paris|]] [[Paris|:Paris]] [[/Sub|/Sub]]";

    // WHEN
    final List<CheckError> errors = analyze("Lyon", text);

    // THEN
    Assertions.assertThat(errors).isEmpty();
  }

  @DisplayName("should fix manually links whose text contains underscores")
  @Test
  void fixLinksWithUnderscores() {
    // GIVEN
    final String text = "[[Tour Eiffel|Tour_Eiffel]]";

    // WHEN
    final List<CheckError> errors = analyze("Lyon", text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(
            error(
                0,
                text,
                new Replacement("[[Tour_Eiffel]]", false),
                new Replacement("[[Tour Eiffel]]", false)));
  }

  @DisplayName("should fix manually links whose text ends with a line break")
  @Test
  void fixLinksWithLineBreak() {
    // GIVEN
    final String text = "[[Paris|Paris <br />]]";

    // WHEN
    final List<CheckError> errors = analyze("Lyon", text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(error(0, text, new Replacement("[[Paris]]<br />", false)));
  }

  @DisplayName("should move quotes and punctuation around the text out of the link")
  @Test
  void fixLinksWithPadding() {
    // GIVEN
    final String text = "[[Paris|«Paris»]] [[Paris|''Paris'']] l'[[Paris|Paris»]]";

    // WHEN
    final List<CheckError> errors = analyze("Lyon", text);

    // THEN
    final int second = text.indexOf("[[Paris|''");
    Assertions.assertThat(errors)
        .containsExactly(
            error(0, "[[Paris|«Paris»]]", new Replacement("«[[Paris]]»", true)),
            error(
                second,
                "[[Paris|''Paris'']]",
                new Replacement("''[[Paris]]''", true),
                new Replacement("''[[Paris]]", false),
                new Replacement("[[Paris]]''", false),
                new Replacement("[[Paris]]", false)),
            error(text.lastIndexOf("[["), "[[Paris|Paris»]]", new Replacement("[[Paris]]»", true)));
  }

  @DisplayName("should fix manually links with padding outside the main namespace")
  @Test
  void fixLinksWithPaddingOutsideMainNamespace() {
    // GIVEN
    final String text = "[[Paris|«Paris»]]";

    // WHEN
    final List<CheckError> errors = analyze("Utilisateur:Bob", text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(error(0, text, new Replacement("«[[Paris]]»", false)));
  }

  @DisplayName("should fix manually links whose padding would merge with apostrophes around")
  @Test
  void fixLinksWithApostrophesAround() {
    // GIVEN
    givenApostropheTemplate("'");
    final String text = "l'[[Paris|''Paris'']]";

    // WHEN
    final List<CheckError> errors = analyze("Lyon", text);

    // THEN
    Assertions.assertThat(errors)
        .containsExactly(
            new CheckError(
                ALGORITHM,
                1,
                text.length(),
                List.of(
                    new Replacement("'''[[Paris]]''", false),
                    new Replacement("{{'}}''[[Paris]]''", false),
                    new Replacement("'''[[Paris]]", false),
                    new Replacement("{{'}}''[[Paris]]", false),
                    new Replacement("'[[Paris]]''", false),
                    new Replacement("{{'}}[[Paris]]''", false),
                    new Replacement("'[[Paris]]", false),
                    new Replacement("{{'}}[[Paris]]", false))));
  }

  private List<CheckError> analyze(final String title, final String text) {
    return algorithm.analyze(PageAnalysisTestFactory.createPageAnalysis(title, text));
  }

  @DisplayName("should fix manually links whose padding would merge with apostrophes after")
  @Test
  void fixLinksWithApostropheAfter() {
    // GIVEN
    final String text = "[[Paris|''Paris'']]'s [[Paris|Paris‘]]";

    // WHEN
    final List<CheckError> errors = analyze("Lyon", text);

    // THEN
    final int quote = text.indexOf("[[Paris|Paris‘");
    Assertions.assertThat(errors)
        .extracting(CheckError::begin, CheckError::end, error -> error.replacements().getFirst())
        .containsExactly(
            Assertions.tuple(0, text.indexOf("'s") + 1, new Replacement("''[[Paris]]'''", false)),
            Assertions.tuple(quote, text.length(), new Replacement("[[Paris]]‘", false)));
  }

  private static CheckError error(
      final int begin, final String link, final Replacement... replacements) {
    return new CheckError(ALGORITHM, begin, begin + link.length(), List.of(replacements));
  }

  private void givenApostropheTemplate(@Nullable final String template) {
    wpcleanerRepository.setConfiguration(
        PageAnalysisTestFactory.WIKI,
        new WPCleanerConfiguration(
            template == null ? Map.of() : Map.of("general_apostrophe_template", template),
            Map.of()));
  }
}
