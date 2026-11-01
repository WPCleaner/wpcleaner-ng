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

  private final InterwikiExtractor interwikiExtractor;
  private final NamespaceExtractor namespaceExtractor;
  private final ProtocolExtractor protocolExtractor;

  public SiteInfoExtractor(
      final InterwikiExtractor interwikiExtractor,
      final NamespaceExtractor namespaceExtractor,
      final ProtocolExtractor protocolExtractor) {
    this.interwikiExtractor = interwikiExtractor;
    this.namespaceExtractor = namespaceExtractor;
    this.protocolExtractor = protocolExtractor;
  }

  public void extract(final WikiDefinition wiki, final SiteInfo siteInfo) {
    namespaceExtractor.extract(wiki, siteInfo);
    interwikiExtractor.extract(wiki, siteInfo);
    protocolExtractor.extract(wiki, siteInfo);
  }
}
