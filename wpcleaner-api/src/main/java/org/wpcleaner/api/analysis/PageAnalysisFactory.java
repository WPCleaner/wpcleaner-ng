package org.wpcleaner.api.analysis;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.springframework.stereotype.Service;
import org.wpcleaner.api.repository.extensiontag.ExtensionTagRepository;
import org.wpcleaner.api.repository.interwiki.InterwikiRepository;
import org.wpcleaner.api.repository.magiclink.MagicLinkRepository;
import org.wpcleaner.api.repository.magicword.MagicWordRepository;
import org.wpcleaner.api.repository.namespace.NamespaceRepository;
import org.wpcleaner.api.repository.protocol.ProtocolRepository;
import org.wpcleaner.api.wiki.definition.WikiDefinition;

@Service
public class PageAnalysisFactory {

  private final ExtensionTagRepository extensionTagRepository;
  private final InterwikiRepository interwikiRepository;
  private final MagicLinkRepository magicLinkRepository;
  private final MagicWordRepository magicWordRepository;
  private final NamespaceRepository namespaceRepository;
  private final ProtocolRepository protocolRepository;

  public PageAnalysisFactory(
      final ExtensionTagRepository extensionTagRepository,
      final InterwikiRepository interwikiRepository,
      final MagicLinkRepository magicLinkRepository,
      final MagicWordRepository magicWordRepository,
      final NamespaceRepository namespaceRepository,
      final ProtocolRepository protocolRepository) {
    this.extensionTagRepository = extensionTagRepository;
    this.interwikiRepository = interwikiRepository;
    this.magicLinkRepository = magicLinkRepository;
    this.magicWordRepository = magicWordRepository;
    this.namespaceRepository = namespaceRepository;
    this.protocolRepository = protocolRepository;
  }

  public PageAnalysis analysis(final WikiDefinition wiki, final String title, final String text) {
    return new PageAnalysis(
        wiki,
        title,
        text,
        extensionTagRepository,
        interwikiRepository,
        magicLinkRepository,
        magicWordRepository,
        namespaceRepository,
        protocolRepository);
  }
}
