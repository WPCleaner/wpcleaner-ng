package org.wpcleaner.api.analysis;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.springframework.stereotype.Service;
import org.wpcleaner.api.repository.interwiki.InterwikiRepository;
import org.wpcleaner.api.repository.namespace.NamespaceRepository;

@Service
public class PageAnalysisFactory {

  private final InterwikiRepository interwikiRepository;
  private final NamespaceRepository namespaceRepository;

  public PageAnalysisFactory(
      final InterwikiRepository interwikiRepository,
      final NamespaceRepository namespaceRepository) {
    this.interwikiRepository = interwikiRepository;
    this.namespaceRepository = namespaceRepository;
  }

  public PageAnalysis analysis(final String title, final String text) {
    return new PageAnalysis(title, text, namespaceRepository, interwikiRepository);
  }
}
