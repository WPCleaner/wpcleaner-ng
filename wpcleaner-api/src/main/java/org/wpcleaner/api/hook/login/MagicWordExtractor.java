package org.wpcleaner.api.hook.login;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.springframework.stereotype.Service;
import org.wpcleaner.api.api.query.meta.siteinfo.SiteInfo;
import org.wpcleaner.api.repository.magicword.MagicWord;
import org.wpcleaner.api.repository.magicword.MagicWordRepository;
import org.wpcleaner.api.wiki.definition.WikiDefinition;

@Service
public class MagicWordExtractor {

  private final MagicWordRepository repository;

  public MagicWordExtractor(final MagicWordRepository repository) {
    this.repository = repository;
  }

  public void extract(final WikiDefinition wiki, final SiteInfo siteInfo) {
    siteInfo.magicWords().forEach(magicWord -> extractMagicWord(wiki, siteInfo, magicWord));
  }

  private void extractMagicWord(
      final WikiDefinition wiki,
      final SiteInfo siteInfo,
      final org.wpcleaner.api.api.query.meta.siteinfo.MagicWord magicWord) {
    repository.addMagicWord(
        wiki,
        new MagicWord(
            magicWord.name(),
            magicWord.aliases(),
            Boolean.TRUE.equals(magicWord.caseSensitive()),
            siteInfo.doubleUnderscores().contains(magicWord.name()),
            siteInfo.functionHooks().contains(magicWord.name()),
            siteInfo.variables().contains(magicWord.name())));
  }
}
