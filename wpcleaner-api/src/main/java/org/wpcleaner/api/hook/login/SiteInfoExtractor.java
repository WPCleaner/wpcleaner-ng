package org.wpcleaner.api.hook.login;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.springframework.stereotype.Service;
import org.wpcleaner.api.api.query.meta.siteinfo.SiteInfo;
import org.wpcleaner.api.wiki.definition.WikiDefinition;

@Service
public class SiteInfoExtractor {

  private final ExtensionTagExtractor extensionTagExtractor;
  private final InterwikiExtractor interwikiExtractor;
  private final MagicWordExtractor magicWordExtractor;
  private final NamespaceExtractor namespaceExtractor;
  private final ProtocolExtractor protocolExtractor;

  public SiteInfoExtractor(
      final ExtensionTagExtractor extensionTagExtractor,
      final InterwikiExtractor interwikiExtractor,
      final MagicWordExtractor magicWordExtractor,
      final NamespaceExtractor namespaceExtractor,
      final ProtocolExtractor protocolExtractor) {
    this.extensionTagExtractor = extensionTagExtractor;
    this.interwikiExtractor = interwikiExtractor;
    this.magicWordExtractor = magicWordExtractor;
    this.namespaceExtractor = namespaceExtractor;
    this.protocolExtractor = protocolExtractor;
  }

  public void extract(final WikiDefinition wiki, final SiteInfo siteInfo) {
    namespaceExtractor.extract(wiki, siteInfo);
    interwikiExtractor.extract(wiki, siteInfo);
    protocolExtractor.extract(wiki, siteInfo);
    extensionTagExtractor.extract(wiki, siteInfo);
    magicWordExtractor.extract(wiki, siteInfo);
  }
}
