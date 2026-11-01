package org.wpcleaner.api.hook.login;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.wpcleaner.api.api.query.meta.siteinfo.SiteInfo;
import org.wpcleaner.api.repository.magicword.MagicWord;
import org.wpcleaner.api.repository.magicword.MagicWordRepository;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.api.wiki.definition.WikipediaDefinitions;

class MagicWordExtractorTest {

  private static final WikiDefinition WIKI = WikipediaDefinitions.EN;

  @DisplayName("Should extract magic words with their function hook and variable flags")
  @Test
  void extract() {
    // GIVEN
    final MagicWordRepository repository = new MagicWordRepository();
    final MagicWordExtractor extractor = new MagicWordExtractor(repository);

    final SiteInfo siteInfo =
        new SiteInfo(
            List.of(),
            Map.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of("if", "pagename"),
            null,
            List.of(),
            List.of(),
            Map.of(),
            List.of(),
            List.of(
                new org.wpcleaner.api.api.query.meta.siteinfo.MagicWord(
                    List.of("si", "if"), false, "if"),
                new org.wpcleaner.api.api.query.meta.siteinfo.MagicWord(
                    List.of("NOMPAGE", "PAGENAME"), true, "pagename"),
                new org.wpcleaner.api.api.query.meta.siteinfo.MagicWord(
                    List.of("ANNEEACTUELLE", "CURRENTYEAR"), null, "currentyear")),
            List.of(),
            Map.of(),
            List.of(),
            null,
            null,
            List.of(),
            List.of(),
            List.of(),
            null,
            null,
            List.of(),
            List.of("pagename", "currentyear"));

    // WHEN
    extractor.extract(WIKI, siteInfo);

    // THEN
    Assertions.assertThat(repository.getMagicWords(WIKI))
        .containsExactly(
            new MagicWord(
                "currentyear", List.of("ANNEEACTUELLE", "CURRENTYEAR"), false, false, true),
            new MagicWord("if", List.of("si", "if"), false, true, false),
            new MagicWord("pagename", List.of("NOMPAGE", "PAGENAME"), true, true, true));
  }
}
