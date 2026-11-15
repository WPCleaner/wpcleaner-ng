package org.wpcleaner.api.hook.login;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.wpcleaner.api.api.ConnectedUser;
import org.wpcleaner.api.api.query.meta.siteinfo.SiteInfoTestFactory;
import org.wpcleaner.api.repository.linter.LinterCategory;
import org.wpcleaner.api.repository.linter.LinterCategoryRepository;
import org.wpcleaner.api.repository.linter.LinterPriority;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.api.wiki.definition.WikipediaDefinitions;

class LinterCategoryExtractorTest {

  private static final WikiDefinition WIKI = WikipediaDefinitions.FR;

  @DisplayName("Should extract the Linter categories with their priority from SiteInfo")
  @Test
  void extract() {
    // GIVEN
    final LinterCategoryRepository repository = new LinterCategoryRepository();
    final LinterCategoryExtractor extractor = new LinterCategoryExtractor(repository);
    final Map<String, List<String>> linter =
        Map.of(
            "high", List.of("duplicate-ids"),
            "low", List.of("missing-end-tag", "obsolete-tag"),
            "unknown", List.of("other"));

    // WHEN
    extractor.extract(
        WIKI,
        SiteInfoTestFactory.createSiteInfo(
            Map.of(), SiteInfoTestFactory.createGeneral(linter, Map.of())));

    // THEN
    Assertions.assertThat(
            Stream.of("duplicate-ids", "missing-end-tag", "obsolete-tag", "other")
                .map(name -> repository.findCategory(WIKI, name))
                .toList())
        .containsExactly(
            Optional.of(new LinterCategory("duplicate-ids", LinterPriority.HIGH)),
            Optional.of(new LinterCategory("missing-end-tag", LinterPriority.LOW)),
            Optional.of(new LinterCategory("obsolete-tag", LinterPriority.LOW)),
            Optional.empty());
    Assertions.assertThat(repository.findCategory(WikipediaDefinitions.EN, "duplicate-ids"))
        .isEmpty();
  }

  @DisplayName(
      "Should extract no Linter category without general information, and forget them on logout")
  @Test
  void extractWithoutGeneral() {
    // GIVEN
    final LinterCategoryRepository repository = new LinterCategoryRepository();
    final LinterCategoryExtractor extractor = new LinterCategoryExtractor(repository);
    extractor.extract(
        WIKI,
        SiteInfoTestFactory.createSiteInfo(
            Map.of(), SiteInfoTestFactory.createGeneral(Map.of("high", List.of("a")), Map.of())));
    repository.logout(new ConnectedUser(false, WIKI, "", false, List.of(), List.of()));
    extractor.extract(
        WikipediaDefinitions.EN,
        SiteInfoTestFactory.createSiteInfo(
            Map.of(), SiteInfoTestFactory.createGeneral(Map.of("high", List.of("b")), Map.of())));

    // WHEN
    extractor.extract(WikipediaDefinitions.EN, SiteInfoTestFactory.createSiteInfo(Map.of(), null));

    // THEN
    Assertions.assertThat(repository.findCategory(WIKI, "a")).isEmpty();
    Assertions.assertThat(repository.findCategory(WikipediaDefinitions.EN, "b")).isEmpty();
  }
}
