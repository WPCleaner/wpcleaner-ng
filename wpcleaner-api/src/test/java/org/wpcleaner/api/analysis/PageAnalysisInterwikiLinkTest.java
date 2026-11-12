package org.wpcleaner.api.analysis;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.wpcleaner.api.analysis.interwikilink.InterwikiLinkElement;
import org.wpcleaner.api.repository.extensiontag.ExtensionTagRepository;
import org.wpcleaner.api.repository.interwiki.Interwiki;
import org.wpcleaner.api.repository.interwiki.InterwikiRepository;
import org.wpcleaner.api.repository.magiclink.MagicLinkRepository;
import org.wpcleaner.api.repository.magicword.MagicWordRepository;
import org.wpcleaner.api.repository.namespace.NamespaceRepository;
import org.wpcleaner.api.repository.protocol.ProtocolRepository;

class PageAnalysisInterwikiLinkTest {

  @DisplayName("should analyze interwiki link when interwiki has null language attribute")
  @Test
  void analyzeInterwikiLinkWithNullLanguageInterwiki() {
    // GIVEN
    final String text = "[[fr:Bonjour]]";
    final NamespaceRepository namespaceRepository = new NamespaceRepository();
    final InterwikiRepository interwikiRepository = new InterwikiRepository();
    interwikiRepository.addInterwiki(
        PageAnalysisTestFactory.WIKI,
        new Interwiki("fr", true, "https://fr.wikipedia.org/wiki/", null, null));

    // WHEN
    final PageAnalysis analysis =
        new PageAnalysis(
            PageAnalysisTestFactory.WIKI,
            "Title",
            text,
            new ExtensionTagRepository(),
            interwikiRepository,
            new MagicLinkRepository(),
            new MagicWordRepository(),
            namespaceRepository,
            new ProtocolRepository());

    // THEN
    Assertions.assertThat(analysis.getLanguageLinks()).isEmpty();
    Assertions.assertThat(analysis.getInterwikiLinks()).hasSize(1);
    Assertions.assertThat(analysis.getInterwikiLinks().getFirst().begin()).isZero();
    Assertions.assertThat(analysis.getInterwikiLinks().getFirst().end()).isEqualTo(text.length());
  }

  @DisplayName("should analyze interwiki link parts")
  @Test
  void analyzeInterwikiLinkParts() {
    // GIVEN
    final String text = "[[wikt:hello#English|hello]]";
    final InterwikiRepository interwikiRepository = new InterwikiRepository();
    interwikiRepository.addInterwiki(
        PageAnalysisTestFactory.WIKI,
        new Interwiki("wikt", true, "https://en.wiktionary.org/wiki/", null, null));

    // WHEN
    final PageAnalysis analysis =
        new PageAnalysis(
            PageAnalysisTestFactory.WIKI,
            "Title",
            text,
            new ExtensionTagRepository(),
            interwikiRepository,
            new MagicLinkRepository(),
            new MagicWordRepository(),
            new NamespaceRepository(),
            new ProtocolRepository());

    // THEN
    Assertions.assertThat(analysis.getInterwikiLinks())
        .containsExactly(
            new InterwikiLinkElement(0, text.length(), "wikt", "hello", "English", "hello", false));
  }
}
