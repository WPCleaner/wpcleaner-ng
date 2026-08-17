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
import org.wpcleaner.api.repository.interwiki.Interwiki;
import org.wpcleaner.api.repository.interwiki.InterwikiRepository;

class InterwikiExtractorTest {

  @DisplayName("Should extract interwikis from SiteInfo into InterwikiRepository")
  @Test
  void extract() {
    // GIVEN
    final InterwikiRepository repository = new InterwikiRepository();
    final InterwikiExtractor extractor = new InterwikiExtractor(repository);

    final org.wpcleaner.api.api.query.meta.siteinfo.Interwiki apiInterwiki =
        new org.wpcleaner.api.api.query.meta.siteinfo.Interwiki(
            null, "English", true, "en", "https://en.wikipedia.org/wiki/$1");
    final SiteInfo siteInfo =
        new SiteInfo(
            List.of(),
            Map.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            null,
            List.of(apiInterwiki),
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
    extractor.extract(siteInfo);

    // THEN
    final List<Interwiki> interwikis = repository.getInterwikis();
    Assertions.assertThat(interwikis).hasSize(1);
    final Interwiki interwiki = interwikis.getFirst();
    Assertions.assertThat(interwiki.prefix()).isEqualTo("en");
    Assertions.assertThat(interwiki.local()).isTrue();
    Assertions.assertThat(interwiki.url()).isEqualTo("https://en.wikipedia.org/wiki/$1");
    Assertions.assertThat(interwiki.language()).isEqualTo("English");
    Assertions.assertThat(interwiki.deprecated()).isNull();
  }
}
