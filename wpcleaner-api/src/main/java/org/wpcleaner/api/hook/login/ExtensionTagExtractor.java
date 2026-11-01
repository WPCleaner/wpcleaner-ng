package org.wpcleaner.api.hook.login;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Locale;
import org.springframework.stereotype.Service;
import org.wpcleaner.api.api.query.meta.siteinfo.SiteInfo;
import org.wpcleaner.api.repository.extensiontag.ExtensionTag;
import org.wpcleaner.api.repository.extensiontag.ExtensionTagRepository;
import org.wpcleaner.api.wiki.definition.WikiDefinition;

@Service
public class ExtensionTagExtractor {

  private final ExtensionTagRepository repository;

  public ExtensionTagExtractor(final ExtensionTagRepository repository) {
    this.repository = repository;
  }

  public void extract(final WikiDefinition wiki, final SiteInfo siteInfo) {
    siteInfo
        .extensionTags()
        .forEach(extensionTagValue -> extractExtensionTag(wiki, extensionTagValue));
  }

  private void extractExtensionTag(final WikiDefinition wiki, final String extensionTagValue) {
    final String name = extensionTagValue.trim().replaceFirst("^<", "").replaceFirst(">$", "");
    repository.addExtensionTag(wiki, new ExtensionTag(name.toLowerCase(Locale.ROOT)));
  }
}
