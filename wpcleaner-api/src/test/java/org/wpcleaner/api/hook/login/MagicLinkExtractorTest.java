package org.wpcleaner.api.hook.login;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.wpcleaner.api.api.query.meta.siteinfo.General;
import org.wpcleaner.api.api.query.meta.siteinfo.SiteInfo;
import org.wpcleaner.api.repository.magiclink.MagicLinkRepository;
import org.wpcleaner.api.repository.magiclink.MagicLinkType;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.api.wiki.definition.WikipediaDefinitions;

class MagicLinkExtractorTest {

  private static final WikiDefinition WIKI = WikipediaDefinitions.EN;

  @DisplayName("Should extract enabled magic links from SiteInfo into MagicLinkRepository")
  @Test
  void extract() {
    // GIVEN
    final MagicLinkRepository repository = new MagicLinkRepository();
    final MagicLinkExtractor extractor = new MagicLinkExtractor(repository);
    final General general =
        new General(
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            Map.of("ISBN", true, "PMID", false, "RFC", true, "OTHER", true),
            null,
            null,
            null,
            null,
            null,
            null,
            null);

    // WHEN
    extractor.extract(WIKI, createSiteInfo(general));

    // THEN
    Assertions.assertThat(repository.getMagicLinks(WIKI))
        .containsExactlyInAnyOrder(MagicLinkType.ISBN, MagicLinkType.RFC);
  }

  @DisplayName("Should extract no magic links when SiteInfo has no general information")
  @Test
  void extractWithoutGeneral() {
    // GIVEN
    final MagicLinkRepository repository = new MagicLinkRepository();
    final MagicLinkExtractor extractor = new MagicLinkExtractor(repository);

    // WHEN
    extractor.extract(WIKI, createSiteInfo(null));

    // THEN
    Assertions.assertThat(repository.getMagicLinks(WIKI)).isEmpty();
  }

  private static SiteInfo createSiteInfo(@Nullable final General general) {
    return new SiteInfo(
        List.of(), Map.of(), List.of(), List.of(), List.of(), List.of(), List.of(), general,
        List.of(), List.of(), Map.of(), List.of(), List.of(), List.of(), Map.of(), List.of(), null,
        null, List.of(), List.of(), List.of(), null, null, List.of(), List.of());
  }
}
