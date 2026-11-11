package org.wpcleaner.api.analysis;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.wpcleaner.api.analysis.category.CategoryElement;
import org.wpcleaner.api.analysis.externallink.ExternalLinkElement;
import org.wpcleaner.api.analysis.internallink.InternalLinkElement;
import org.wpcleaner.api.analysis.interwikilink.InterwikiLinkElement;
import org.wpcleaner.api.analysis.languagelink.LanguageLinkElement;
import org.wpcleaner.api.analysis.pagetitle.PageTitle;
import org.wpcleaner.api.repository.extensiontag.ExtensionTagRepository;
import org.wpcleaner.api.repository.interwiki.Interwiki;
import org.wpcleaner.api.repository.interwiki.InterwikiRepository;
import org.wpcleaner.api.repository.magiclink.MagicLinkRepository;
import org.wpcleaner.api.repository.magicword.MagicWordRepository;
import org.wpcleaner.api.repository.namespace.NamespaceRepository;
import org.wpcleaner.api.repository.protocol.ProtocolRepository;
import org.wpcleaner.api.wiki.definition.WikipediaDefinitions;

class PageAnalysisTest {

  @DisplayName("should analyze internal link")
  @Test
  void analyzeInternalLink() {
    // GIVEN
    final String text = "[[Link]]";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getInternalLinks()).hasSize(1);
    Assertions.assertThat(analysis.getInternalLinks().getFirst().begin()).isZero();
    Assertions.assertThat(analysis.getInternalLinks().getFirst().end()).isEqualTo(text.length());
  }

  @DisplayName("should not analyze category with namespaces of another wiki")
  @Test
  void analyzeCategoryWithNamespacesOfAnotherWiki() {
    // GIVEN
    final String text = "[[Catégorie:My category]]";

    // WHEN
    final PageAnalysis analysis =
        PageAnalysisTestFactory.createPageAnalysis(WikipediaDefinitions.EN, text);

    // THEN
    Assertions.assertThat(analysis.getCategories()).isEmpty();
    Assertions.assertThat(analysis.getInternalLinks()).hasSize(1);
  }

  @DisplayName("should analyze category")
  @Test
  void analyzeCategory() {
    // GIVEN
    final String text = "[[Catégorie:My category]]";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

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
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

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
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getExternalLinks()).isEmpty();
  }

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

  @DisplayName("should analyze internal link parts")
  @Test
  void analyzeInternalLinkParts() {
    // GIVEN
    final String text = "[[ Link # Anchor |Some text]]";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getInternalLinks())
        .containsExactly(
            new InternalLinkElement(
                0, text.length(), "Link", new PageTitle(0, "Link"), "Anchor", "Some text", false));
  }

  @DisplayName("should analyze internal link without text nor anchor")
  @Test
  void analyzeInternalLinkWithoutTextNorAnchor() {
    // GIVEN
    final String text = "[[Link]]";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getInternalLinks())
        .containsExactly(
            new InternalLinkElement(
                0, text.length(), "Link", new PageTitle(0, "Link"), null, null, false));
  }

  @DisplayName("should analyze internal link with empty text")
  @Test
  void analyzeInternalLinkWithEmptyText() {
    // GIVEN
    final String text = "[[Link|]]";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getInternalLinks())
        .containsExactly(
            new InternalLinkElement(
                0, text.length(), "Link", new PageTitle(0, "Link"), null, "", false));
  }

  @DisplayName("should analyze internal link to an anchor of the same page")
  @Test
  void analyzeInternalLinkToAnchor() {
    // GIVEN
    final String text = "[[#Section]]";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getInternalLinks())
        .containsExactly(
            new InternalLinkElement(0, text.length(), "", null, "Section", null, false));
  }

  @DisplayName("should analyze category parts")
  @Test
  void analyzeCategoryParts() {
    // GIVEN
    final String text = "[[Catégorie: My category |Sort key]]";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getCategories())
        .containsExactly(
            new CategoryElement(
                0,
                text.length(),
                "Catégorie",
                "My category",
                new PageTitle(14, "My category"),
                "Sort key"));
  }

  @DisplayName("should analyze external link parts")
  @Test
  void analyzeExternalLinkParts() {
    // GIVEN
    final String text = "[https://example.com/ Hello world]";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getExternalLinks())
        .containsExactly(
            new ExternalLinkElement(0, text.length(), "https://example.com/", "Hello world", true));
  }

  @DisplayName("should analyze external link without text")
  @Test
  void analyzeExternalLinkWithoutText() {
    // GIVEN
    final String text = "[https://example.com/]";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getExternalLinks())
        .containsExactly(
            new ExternalLinkElement(0, text.length(), "https://example.com/", null, true));
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

  @DisplayName("should analyze category when namespace is surrounded by whitespace")
  @Test
  void analyzeCategoryWithWhitespaceAroundNamespace() {
    // GIVEN
    final String text = "[[ Catégorie :My category]]";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getInternalLinks()).isEmpty();
    Assertions.assertThat(analysis.getCategories())
        .containsExactly(
            new CategoryElement(
                0,
                text.length(),
                "Catégorie",
                "My category",
                new PageTitle(14, "My category"),
                null));
  }

  @DisplayName("should analyze internal link followed by a closing bracket")
  @Test
  void analyzeInternalLinkFollowedByClosingBracket() {
    // GIVEN
    final String text = "[[a|b]]]";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getInternalLinks())
        .containsExactly(
            new InternalLinkElement(0, 7, "a", new PageTitle(0, "A"), null, "b", false));
  }

  @DisplayName("should analyze external link followed by a closing bracket")
  @Test
  void analyzeExternalLinkFollowedByClosingBracket() {
    // GIVEN
    final String text = "[https://example.com/ b]]";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getExternalLinks())
        .containsExactly(new ExternalLinkElement(0, 24, "https://example.com/", "b", true));
  }

  @DisplayName("should analyze external link inside double brackets")
  @Test
  void analyzeExternalLinkInsideDoubleBrackets() {
    // GIVEN
    final String text = "[[https://example.com/ b]] [[https://example.com/]]";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getInternalLinks()).isEmpty();
    Assertions.assertThat(analysis.getExternalLinks())
        .containsExactly(
            new ExternalLinkElement(1, 25, "https://example.com/", "b", true),
            new ExternalLinkElement(28, 50, "https://example.com/", null, true));
  }

  @DisplayName("should analyze external link with a pipe in its URL")
  @Test
  void analyzeExternalLinkWithPipe() {
    // GIVEN
    final String text =
        "[https://example.com/|b] [[https://example.com/|b]] {{a|[https://example.com/|b]}}";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getInternalLinks()).isEmpty();
    Assertions.assertThat(analysis.getExternalLinks())
        .extracting(
            ExternalLinkElement::begin, ExternalLinkElement::target, ExternalLinkElement::bracketed)
        .containsExactly(
            Assertions.tuple(0, "https://example.com/|b", true),
            Assertions.tuple(26, "https://example.com/|b", true),
            Assertions.tuple(57, "https://example.com/", false));
  }

  @DisplayName("should not analyze internal link with spaces before a URL")
  @Test
  void analyzeSpacesBeforeUrlInsideDoubleBrackets() {
    // GIVEN
    final String text = "[[ https://example.com/ b]]";

    // WHEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getInternalLinks()).isEmpty();
    Assertions.assertThat(analysis.getExternalLinks())
        .containsExactly(new ExternalLinkElement(3, 23, "https://example.com/", null, false));
  }
}
