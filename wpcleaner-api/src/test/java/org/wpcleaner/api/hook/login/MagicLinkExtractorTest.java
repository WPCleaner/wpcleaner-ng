package org.wpcleaner.api.hook.login;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.wpcleaner.api.api.query.meta.siteinfo.General;
import org.wpcleaner.api.api.query.meta.siteinfo.SiteInfoTestFactory;
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
        SiteInfoTestFactory.createGeneral(
            Map.of(), Map.of("ISBN", true, "PMID", false, "RFC", true, "OTHER", true));

    // WHEN
    extractor.extract(WIKI, SiteInfoTestFactory.createSiteInfo(Map.of(), general));

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
    extractor.extract(WIKI, SiteInfoTestFactory.createSiteInfo(Map.of(), null));

    // THEN
    Assertions.assertThat(repository.getMagicLinks(WIKI)).isEmpty();
  }
}
