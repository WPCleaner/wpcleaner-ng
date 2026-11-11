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
import org.wpcleaner.api.api.query.meta.siteinfo.SpecialPageAlias;
import org.wpcleaner.api.repository.specialpage.SpecialPage;
import org.wpcleaner.api.repository.specialpage.SpecialPageRepository;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.api.wiki.definition.WikipediaDefinitions;

class SpecialPageExtractorTest {

  private static final WikiDefinition WIKI = WikipediaDefinitions.FR;

  @DisplayName("Should extract special page aliases from SiteInfo into SpecialPageRepository")
  @Test
  void extract() {
    // GIVEN
    final SpecialPageRepository repository = new SpecialPageRepository();
    final SpecialPageExtractor extractor = new SpecialPageExtractor(repository);
    final List<SpecialPageAlias> aliases =
        List.of(
            new SpecialPageAlias(List.of("Index", "PrefixIndex"), "Prefixindex"),
            new SpecialPageAlias(List.of("Toutes les pages"), "Allpages"));

    // WHEN
    extractor.extract(WIKI, createSiteInfo(aliases));

    // THEN
    Assertions.assertThat(repository.findSpecialPage(WIKI, "Index"))
        .contains(new SpecialPage("Prefixindex", List.of("Index", "PrefixIndex")));
    Assertions.assertThat(repository.findSpecialPage(WIKI, "Toutes les pages"))
        .map(SpecialPage::realName)
        .contains("Allpages");
  }

  private static SiteInfo createSiteInfo(final List<SpecialPageAlias> aliases) {
    return new SiteInfo(
        List.of(), Map.of(), List.of(), List.of(), List.of(), List.of(), List.of(), null, List.of(),
        List.of(), Map.of(), List.of(), List.of(), List.of(), Map.of(), List.of(), null, null,
        List.of(), List.of(), aliases, null, null, List.of(), List.of());
  }
}
