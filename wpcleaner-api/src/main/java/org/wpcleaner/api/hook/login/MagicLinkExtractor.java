package org.wpcleaner.api.hook.login;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.wpcleaner.api.api.query.meta.siteinfo.General;
import org.wpcleaner.api.api.query.meta.siteinfo.SiteInfo;
import org.wpcleaner.api.repository.magiclink.MagicLinkRepository;
import org.wpcleaner.api.repository.magiclink.MagicLinkType;
import org.wpcleaner.api.wiki.definition.WikiDefinition;

@Service
public class MagicLinkExtractor {

  private final MagicLinkRepository repository;

  public MagicLinkExtractor(final MagicLinkRepository repository) {
    this.repository = repository;
  }

  public void extract(final WikiDefinition wiki, final SiteInfo siteInfo) {
    Optional.ofNullable(siteInfo.general())
        .map(General::magicLinks)
        .orElse(Map.of())
        .entrySet()
        .stream()
        .filter(entry -> Boolean.TRUE.equals(entry.getValue()))
        .flatMap(entry -> MagicLinkType.findByName(entry.getKey()).stream())
        .forEach(magicLink -> repository.enableMagicLink(wiki, magicLink));
  }
}
