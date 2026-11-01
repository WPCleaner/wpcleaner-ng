package org.wpcleaner.api.hook.login;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.springframework.stereotype.Service;
import org.wpcleaner.api.api.query.meta.siteinfo.SiteInfo;
import org.wpcleaner.api.repository.protocol.Protocol;
import org.wpcleaner.api.repository.protocol.ProtocolRepository;
import org.wpcleaner.api.wiki.definition.WikiDefinition;

@Service
public class ProtocolExtractor {

  private final ProtocolRepository repository;

  public ProtocolExtractor(final ProtocolRepository repository) {
    this.repository = repository;
  }

  public void extract(final WikiDefinition wiki, final SiteInfo siteInfo) {
    siteInfo.protocols().forEach(protocolValue -> extractProtocol(wiki, protocolValue));
  }

  private void extractProtocol(final WikiDefinition wiki, final String protocolValue) {
    repository.addProtocol(wiki, new Protocol(protocolValue));
  }
}
