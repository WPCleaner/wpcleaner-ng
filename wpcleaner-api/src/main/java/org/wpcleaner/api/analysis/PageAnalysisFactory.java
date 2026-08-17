package org.wpcleaner.api.analysis;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.springframework.stereotype.Service;
import org.wpcleaner.api.repository.interwiki.InterwikiRepository;
import org.wpcleaner.api.repository.namespace.NamespaceRepository;
import org.wpcleaner.api.repository.protocol.ProtocolRepository;

@Service
public class PageAnalysisFactory {

  private final InterwikiRepository interwikiRepository;
  private final NamespaceRepository namespaceRepository;
  private final ProtocolRepository protocolRepository;

  public PageAnalysisFactory(
      final InterwikiRepository interwikiRepository,
      final NamespaceRepository namespaceRepository,
      final ProtocolRepository protocolRepository) {
    this.interwikiRepository = interwikiRepository;
    this.namespaceRepository = namespaceRepository;
    this.protocolRepository = protocolRepository;
  }

  public PageAnalysis analysis(final String title, final String text) {
    return new PageAnalysis(
        title, text, interwikiRepository, namespaceRepository, protocolRepository);
  }
}
