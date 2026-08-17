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

@Service
public class InterwikiExtractor {

  private final InterwikiRepository repository;

  public InterwikiExtractor(final InterwikiRepository repository) {
    this.repository = repository;
  }

  public void extract(final SiteInfo siteInfo) {
    siteInfo.interwikiMap().forEach(this::extractInterwiki);
  }

  private void extractInterwiki(
      final org.wpcleaner.api.api.query.meta.siteinfo.Interwiki interwiki) {
    repository.addInterwiki(
        new Interwiki(
            interwiki.prefix(),
            Objects.requireNonNullElse(interwiki.local(), Boolean.FALSE),
            interwiki.url(),
            interwiki.language(),
            interwiki.deprecated()));
  }
}
