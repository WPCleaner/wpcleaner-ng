package org.wpcleaner.api.hook.login;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Objects;
import org.springframework.stereotype.Service;
import org.wpcleaner.api.api.query.meta.siteinfo.SiteInfo;
import org.wpcleaner.api.repository.interwiki.Interwiki;
import org.wpcleaner.api.repository.interwiki.InterwikiRepository;
import org.wpcleaner.api.wiki.definition.WikiDefinition;

@Service
public class InterwikiExtractor {

  private final InterwikiRepository repository;

  public InterwikiExtractor(final InterwikiRepository repository) {
    this.repository = repository;
  }

  public void extract(final WikiDefinition wiki, final SiteInfo siteInfo) {
    siteInfo.interwikiMap().forEach(interwiki -> extractInterwiki(wiki, interwiki));
  }

  private void extractInterwiki(
      final WikiDefinition wiki,
      final org.wpcleaner.api.api.query.meta.siteinfo.Interwiki interwiki) {
    repository.addInterwiki(
        wiki,
        new Interwiki(
            interwiki.prefix(),
            Objects.requireNonNullElse(interwiki.local(), Boolean.FALSE),
            interwiki.url(),
            interwiki.language(),
            interwiki.deprecated()));
  }
}
