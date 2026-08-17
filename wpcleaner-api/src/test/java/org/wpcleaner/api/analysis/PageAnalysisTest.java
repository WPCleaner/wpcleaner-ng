package org.wpcleaner.api.analysis;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.wpcleaner.api.repository.CaseType;
import org.wpcleaner.api.repository.interwiki.Interwiki;
import org.wpcleaner.api.repository.interwiki.InterwikiRepository;
import org.wpcleaner.api.repository.namespace.CommonNamespaces;
import org.wpcleaner.api.repository.namespace.Namespace;
import org.wpcleaner.api.repository.namespace.NamespaceRepository;
import org.wpcleaner.api.repository.protocol.Protocol;
import org.wpcleaner.api.repository.protocol.ProtocolRepository;

class PageAnalysisTest {

  @DisplayName("should analyze comment")
  @Test
  void analyzeComment() {
    // GIVEN
    final String text = "<!-- COMMENT -->";

    // WHEN
    final PageAnalysis analysis = createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getComments()).hasSize(1);
    Assertions.assertThat(analysis.getComments().getFirst().begin()).isZero();
    Assertions.assertThat(analysis.getComments().getFirst().end()).isEqualTo(text.length());
  }

  @DisplayName("should analyze internal link")
  @Test
  void analyzeInternalLink() {
    // GIVEN
    final String text = "[[Link]]";

    // WHEN
    final PageAnalysis analysis = createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getInternalLinks()).hasSize(1);
    Assertions.assertThat(analysis.getInternalLinks().getFirst().begin()).isZero();
    Assertions.assertThat(analysis.getInternalLinks().getFirst().end()).isEqualTo(text.length());
  }

  @DisplayName("should analyze category")
  @Test
  void analyzeCategory() {
    // GIVEN
    final String text = "[[Catégorie:My category]]";

    // WHEN
    final PageAnalysis analysis = createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getInternalLinks()).isEmpty();
    Assertions.assertThat(analysis.getCategories()).hasSize(1);
    Assertions.assertThat(analysis.getCategories().getFirst().begin()).isZero();
    Assertions.assertThat(analysis.getCategories().getFirst().end()).isEqualTo(text.length());
  }

  @DisplayName("should analyze external link when protocol is known")
  @Test
  void analyzeExternalLink() {
    // GIVEN
    final String text = "[https://example.com/ Hello]";

    // WHEN
    final PageAnalysis analysis = createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getExternalLinks()).hasSize(1);
    Assertions.assertThat(analysis.getExternalLinks().getFirst().begin()).isZero();
    Assertions.assertThat(analysis.getExternalLinks().getFirst().end()).isEqualTo(text.length());
  }

  @DisplayName("should not analyze external link when protocol is unknown")
  @Test
  void analyzeExternalLinkWithUnknownProtocol() {
    // GIVEN
    final String text = "[ftp://example.com/ Hello]";

    // WHEN
    final PageAnalysis analysis = createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getExternalLinks()).isEmpty();
  }

  private PageAnalysis createPageAnalysis(final String text) {
    final NamespaceRepository namespaceRepository = new NamespaceRepository();
    namespaceRepository.addNamespace(
        new Namespace(
            CommonNamespaces.CATEGORY.id,
            "Category",
            "Catégorie",
            List.of("Catégories"),
            CaseType.FIRST_LETTER));
    final ProtocolRepository protocolRepository = new ProtocolRepository();
    protocolRepository.addProtocol(new Protocol("https://"));
    protocolRepository.addProtocol(new Protocol("http://"));
    return new PageAnalysis(
        "Title", text, new InterwikiRepository(), namespaceRepository, protocolRepository);
  }

  @DisplayName("should analyze language link when interwiki has language attribute")
  @Test
  void analyzeLanguageLink() {
    // GIVEN
    final String text = "[[en:Hello]]";
    final NamespaceRepository namespaceRepository = new NamespaceRepository();
    final InterwikiRepository interwikiRepository = new InterwikiRepository();
    interwikiRepository.addInterwiki(
        new Interwiki("en", true, "https://en.wikipedia.org/wiki/", "English", null));
    interwikiRepository.addInterwiki(
        new Interwiki("fr", true, "https://fr.wikipedia.org/wiki/", null, null));

    // WHEN
    final PageAnalysis analysis =
        new PageAnalysis(
            "Title", text, interwikiRepository, namespaceRepository, new ProtocolRepository());

    // THEN
    Assertions.assertThat(analysis.getLanguageLinks()).hasSize(1);
    Assertions.assertThat(analysis.getLanguageLinks().getFirst().begin()).isZero();
    Assertions.assertThat(analysis.getLanguageLinks().getFirst().end()).isEqualTo(text.length());
    Assertions.assertThat(analysis.getInternalLinks()).isEmpty();
  }

  @DisplayName("should analyze interwiki link when interwiki has null language attribute")
  @Test
  void analyzeInterwikiLinkWithNullLanguageInterwiki() {
    // GIVEN
    final String text = "[[fr:Bonjour]]";
    final NamespaceRepository namespaceRepository = new NamespaceRepository();
    final InterwikiRepository interwikiRepository = new InterwikiRepository();
    interwikiRepository.addInterwiki(
        new Interwiki("fr", true, "https://fr.wikipedia.org/wiki/", null, null));

    // WHEN
    final PageAnalysis analysis =
        new PageAnalysis(
            "Title", text, interwikiRepository, namespaceRepository, new ProtocolRepository());

    // THEN
    Assertions.assertThat(analysis.getLanguageLinks()).isEmpty();
    Assertions.assertThat(analysis.getInterwikiLinks()).hasSize(1);
    Assertions.assertThat(analysis.getInterwikiLinks().getFirst().begin()).isZero();
    Assertions.assertThat(analysis.getInterwikiLinks().getFirst().end()).isEqualTo(text.length());
  }
}
