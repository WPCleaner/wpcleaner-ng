package org.wpcleaner.api.hook.login;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.springframework.stereotype.Service;
import org.wpcleaner.api.api.query.meta.siteinfo.SiteInfo;
import org.wpcleaner.api.repository.specialpage.SpecialPage;
import org.wpcleaner.api.repository.specialpage.SpecialPageRepository;
import org.wpcleaner.api.wiki.definition.WikiDefinition;

@Service
public class SpecialPageExtractor {

  private final SpecialPageRepository repository;

  public SpecialPageExtractor(final SpecialPageRepository repository) {
    this.repository = repository;
  }

  public void extract(final WikiDefinition wiki, final SiteInfo siteInfo) {
    siteInfo
        .specialPageAliases()
        .forEach(
            alias ->
                repository.addSpecialPage(
                    wiki, new SpecialPage(alias.realName(), alias.aliases())));
  }
}
