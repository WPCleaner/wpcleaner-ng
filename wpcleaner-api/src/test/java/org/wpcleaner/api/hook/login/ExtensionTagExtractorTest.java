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
import org.wpcleaner.api.repository.extensiontag.ExtensionTag;
import org.wpcleaner.api.repository.extensiontag.ExtensionTagRepository;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.api.wiki.definition.WikipediaDefinitions;

class ExtensionTagExtractorTest {

  private static final WikiDefinition WIKI = WikipediaDefinitions.EN;

  @DisplayName("Should extract extension tag names from SiteInfo into ExtensionTagRepository")
  @Test
  void extract() {
    // GIVEN
    final ExtensionTagRepository repository = new ExtensionTagRepository();
    final ExtensionTagExtractor extractor = new ExtensionTagExtractor(repository);

    final SiteInfo siteInfo =
        new SiteInfo(
            List.of(),
            Map.of(),
            List.of(),
            List.of(),
            List.of("<ref>", "<NoWiki>", "<pre>"),
            List.of(),
            List.of(),
            null,
            List.of(),
            List.of(),
            Map.of(),
            List.of(),
            List.of(),
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
            List.of());

    // WHEN
    extractor.extract(WIKI, siteInfo);

    // THEN
    final List<ExtensionTag> extensionTags = repository.getExtensionTags(WIKI);
    Assertions.assertThat(extensionTags)
        .containsExactly(
            new ExtensionTag("nowiki"), new ExtensionTag("pre"), new ExtensionTag("ref"));
  }
}
