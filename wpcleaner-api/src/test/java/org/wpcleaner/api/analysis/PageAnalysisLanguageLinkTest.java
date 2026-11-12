package org.wpcleaner.api.analysis;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.wpcleaner.api.analysis.languagelink.LanguageLinkElement;
import org.wpcleaner.api.repository.extensiontag.ExtensionTagRepository;
import org.wpcleaner.api.repository.interwiki.Interwiki;
import org.wpcleaner.api.repository.interwiki.InterwikiRepository;
import org.wpcleaner.api.repository.magiclink.MagicLinkRepository;
import org.wpcleaner.api.repository.magicword.MagicWordRepository;
import org.wpcleaner.api.repository.namespace.NamespaceRepository;
import org.wpcleaner.api.repository.protocol.ProtocolRepository;

class PageAnalysisLanguageLinkTest {

  @DisplayName("should analyze language link when interwiki has language attribute")
  @Test
  void analyzeLanguageLink() {
    // GIVEN
    final String text = "[[en:Hello]]";
    final NamespaceRepository namespaceRepository = new NamespaceRepository();
    final InterwikiRepository interwikiRepository = new InterwikiRepository();
    interwikiRepository.addInterwiki(
        PageAnalysisTestFactory.WIKI,
        new Interwiki("en", true, "https://en.wikipedia.org/wiki/", "English", null));
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
    Assertions.assertThat(analysis.getLanguageLinks()).hasSize(1);
    Assertions.assertThat(analysis.getLanguageLinks().getFirst().begin()).isZero();
    Assertions.assertThat(analysis.getLanguageLinks().getFirst().end()).isEqualTo(text.length());
    Assertions.assertThat(analysis.getInternalLinks()).isEmpty();
  }

  @DisplayName("should analyze language link parts")
  @Test
  void analyzeLanguageLinkParts() {
    // GIVEN
    final String text = "[[en:Hello world]]";
    final InterwikiRepository interwikiRepository = new InterwikiRepository();
    interwikiRepository.addInterwiki(
        PageAnalysisTestFactory.WIKI,
        new Interwiki("en", true, "https://en.wikipedia.org/wiki/", "English", null));

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
    Assertions.assertThat(analysis.getLanguageLinks())
        .containsExactly(new LanguageLinkElement(0, text.length(), "en", "Hello world", null));
  }

  @DisplayName("should analyze language link when prefix is surrounded by whitespace")
  @Test
  void analyzeLanguageLinkWithWhitespaceAroundPrefix() {
    // GIVEN
    final String text = "[[ en :X]]";
    final InterwikiRepository interwikiRepository = new InterwikiRepository();
    interwikiRepository.addInterwiki(
        PageAnalysisTestFactory.WIKI,
        new Interwiki("en", true, "https://en.wikipedia.org/wiki/", "English", null));

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
    Assertions.assertThat(analysis.getInternalLinks()).isEmpty();
    Assertions.assertThat(analysis.getLanguageLinks())
        .containsExactly(new LanguageLinkElement(0, text.length(), "en", "X", null));
  }
}
