package org.wpcleaner.api.analysis;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.wpcleaner.api.analysis.category.CategoryElement;
import org.wpcleaner.api.analysis.externallink.ExternalLinkElement;
import org.wpcleaner.api.analysis.internallink.InternalLinkElement;
import org.wpcleaner.api.analysis.interwikilink.InterwikiLinkElement;
import org.wpcleaner.api.analysis.languagelink.LanguageLinkElement;
import org.wpcleaner.api.analysis.tag.CompleteTagElement;
import org.wpcleaner.api.analysis.tag.TagAttribute;
import org.wpcleaner.api.analysis.tag.TagElement;
import org.wpcleaner.api.analysis.tag.TagKind;
import org.wpcleaner.api.repository.CaseType;
import org.wpcleaner.api.repository.interwiki.Interwiki;
import org.wpcleaner.api.repository.interwiki.InterwikiRepository;
import org.wpcleaner.api.repository.namespace.CommonNamespaces;
import org.wpcleaner.api.repository.namespace.Namespace;
import org.wpcleaner.api.repository.namespace.NamespaceRepository;
import org.wpcleaner.api.repository.protocol.Protocol;
import org.wpcleaner.api.repository.protocol.ProtocolRepository;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.api.wiki.definition.WikipediaDefinitions;

class PageAnalysisTest {

  private static final WikiDefinition WIKI = WikipediaDefinitions.FR;

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

  @DisplayName("should not analyze category with namespaces of another wiki")
  @Test
  void analyzeCategoryWithNamespacesOfAnotherWiki() {
    // GIVEN
    final String text = "[[Catégorie:My category]]";

    // WHEN
    final PageAnalysis analysis = createPageAnalysis(WikipediaDefinitions.EN, text);

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
    return createPageAnalysis(WIKI, text);
  }

  private PageAnalysis createPageAnalysis(final WikiDefinition wiki, final String text) {
    final NamespaceRepository namespaceRepository = new NamespaceRepository();
    namespaceRepository.addNamespace(
        WIKI,
        new Namespace(
            CommonNamespaces.CATEGORY.id,
            "Category",
            "Catégorie",
            List.of("Catégories"),
            CaseType.FIRST_LETTER));
    final ProtocolRepository protocolRepository = new ProtocolRepository();
    protocolRepository.addProtocol(WIKI, new Protocol("https://"));
    protocolRepository.addProtocol(WIKI, new Protocol("http://"));
    return new PageAnalysis(
        wiki, "Title", text, new InterwikiRepository(), namespaceRepository, protocolRepository);
  }

  @DisplayName("should analyze language link when interwiki has language attribute")
  @Test
  void analyzeLanguageLink() {
    // GIVEN
    final String text = "[[en:Hello]]";
    final NamespaceRepository namespaceRepository = new NamespaceRepository();
    final InterwikiRepository interwikiRepository = new InterwikiRepository();
    interwikiRepository.addInterwiki(
        WIKI, new Interwiki("en", true, "https://en.wikipedia.org/wiki/", "English", null));
    interwikiRepository.addInterwiki(
        WIKI, new Interwiki("fr", true, "https://fr.wikipedia.org/wiki/", null, null));

    // WHEN
    final PageAnalysis analysis =
        new PageAnalysis(
            WIKI,
            "Title",
            text,
            interwikiRepository,
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
        WIKI, new Interwiki("fr", true, "https://fr.wikipedia.org/wiki/", null, null));

    // WHEN
    final PageAnalysis analysis =
        new PageAnalysis(
            WIKI,
            "Title",
            text,
            interwikiRepository,
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
    final PageAnalysis analysis = createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getInternalLinks())
        .containsExactly(new InternalLinkElement(0, text.length(), "Link", "Anchor", "Some text"));
  }

  @DisplayName("should analyze internal link without text nor anchor")
  @Test
  void analyzeInternalLinkWithoutTextNorAnchor() {
    // GIVEN
    final String text = "[[Link]]";

    // WHEN
    final PageAnalysis analysis = createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getInternalLinks())
        .containsExactly(new InternalLinkElement(0, text.length(), "Link", null, null));
  }

  @DisplayName("should analyze internal link with empty text")
  @Test
  void analyzeInternalLinkWithEmptyText() {
    // GIVEN
    final String text = "[[Link|]]";

    // WHEN
    final PageAnalysis analysis = createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getInternalLinks())
        .containsExactly(new InternalLinkElement(0, text.length(), "Link", null, ""));
  }

  @DisplayName("should analyze internal link to an anchor of the same page")
  @Test
  void analyzeInternalLinkToAnchor() {
    // GIVEN
    final String text = "[[#Section]]";

    // WHEN
    final PageAnalysis analysis = createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getInternalLinks())
        .containsExactly(new InternalLinkElement(0, text.length(), "", "Section", null));
  }

  @DisplayName("should analyze category parts")
  @Test
  void analyzeCategoryParts() {
    // GIVEN
    final String text = "[[Catégorie: My category |Sort key]]";

    // WHEN
    final PageAnalysis analysis = createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getCategories())
        .containsExactly(
            new CategoryElement(0, text.length(), "Catégorie", "My category", "Sort key"));
  }

  @DisplayName("should analyze external link parts")
  @Test
  void analyzeExternalLinkParts() {
    // GIVEN
    final String text = "[https://example.com/ Hello world]";

    // WHEN
    final PageAnalysis analysis = createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getExternalLinks())
        .containsExactly(
            new ExternalLinkElement(0, text.length(), "https://example.com/", "Hello world"));
  }

  @DisplayName("should analyze external link without text")
  @Test
  void analyzeExternalLinkWithoutText() {
    // GIVEN
    final String text = "[https://example.com/]";

    // WHEN
    final PageAnalysis analysis = createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getExternalLinks())
        .containsExactly(new ExternalLinkElement(0, text.length(), "https://example.com/", null));
  }

  @DisplayName("should analyze language link parts")
  @Test
  void analyzeLanguageLinkParts() {
    // GIVEN
    final String text = "[[en:Hello world]]";
    final InterwikiRepository interwikiRepository = new InterwikiRepository();
    interwikiRepository.addInterwiki(
        WIKI, new Interwiki("en", true, "https://en.wikipedia.org/wiki/", "English", null));

    // WHEN
    final PageAnalysis analysis =
        new PageAnalysis(
            WIKI,
            "Title",
            text,
            interwikiRepository,
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
        WIKI, new Interwiki("wikt", true, "https://en.wiktionary.org/wiki/", null, null));

    // WHEN
    final PageAnalysis analysis =
        new PageAnalysis(
            WIKI,
            "Title",
            text,
            interwikiRepository,
            new NamespaceRepository(),
            new ProtocolRepository());

    // THEN
    Assertions.assertThat(analysis.getInterwikiLinks())
        .containsExactly(
            new InterwikiLinkElement(0, text.length(), "wikt", "hello", "English", "hello"));
  }

  @DisplayName("should analyze language link when prefix is surrounded by whitespace")
  @Test
  void analyzeLanguageLinkWithWhitespaceAroundPrefix() {
    // GIVEN
    final String text = "[[ en :X]]";
    final InterwikiRepository interwikiRepository = new InterwikiRepository();
    interwikiRepository.addInterwiki(
        WIKI, new Interwiki("en", true, "https://en.wikipedia.org/wiki/", "English", null));

    // WHEN
    final PageAnalysis analysis =
        new PageAnalysis(
            WIKI,
            "Title",
            text,
            interwikiRepository,
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
    final PageAnalysis analysis = createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getInternalLinks()).isEmpty();
    Assertions.assertThat(analysis.getCategories())
        .containsExactly(new CategoryElement(0, text.length(), "Catégorie", "My category", null));
  }

  @DisplayName("should ignore internal links in every comment")
  @Test
  void analyzeInternalLinksInSeveralComments() {
    // GIVEN
    final String text = "<!-- [[A]] --><!-- [[B]] -->[[C]]";

    // WHEN
    final PageAnalysis analysis = createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getInternalLinks())
        .containsExactly(new InternalLinkElement(28, text.length(), "C", null, null));
  }

  @DisplayName("should analyze tag attributes")
  @Test
  void analyzeTagAttributes() {
    // GIVEN
    final String text = "<span class=\"a b\" title = 'c' data-x=y hidden>";

    // WHEN
    final PageAnalysis analysis = createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getTags())
        .containsExactly(
            new TagElement(
                0,
                text.length(),
                "span",
                TagKind.START,
                List.of(
                    new TagAttribute("class", "a b"),
                    new TagAttribute("title", "c"),
                    new TagAttribute("data-x", "y"),
                    new TagAttribute("hidden", null))));
  }

  @DisplayName("should analyze tag with a slash in an attribute value")
  @Test
  void analyzeTagWithSlashInAttributeValue() {
    // GIVEN
    final String text = "<a href=\"https://example.com/\" rel=nofollow/>";

    // WHEN
    final PageAnalysis analysis = createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getTags())
        .containsExactly(
            new TagElement(
                0,
                text.length(),
                "a",
                TagKind.SELF_CLOSING,
                List.of(
                    new TagAttribute("href", "https://example.com/"),
                    new TagAttribute("rel", "nofollow"))));
  }

  @DisplayName("should analyze tag kinds")
  @Test
  void analyzeTagKinds() {
    // GIVEN
    final String text = "<b><br/></B >";

    // WHEN
    final PageAnalysis analysis = createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getTags())
        .containsExactly(
            new TagElement(0, 3, "b", TagKind.START, List.of()),
            new TagElement(3, 8, "br", TagKind.SELF_CLOSING, List.of()),
            new TagElement(8, 13, "B", TagKind.END, List.of()));
  }

  @DisplayName("should not analyze tag when name does not start with a letter")
  @Test
  void analyzeTagNotStartingWithLetter() {
    // GIVEN
    final String text = "x <5 and y> 3";

    // WHEN
    final PageAnalysis analysis = createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getTags()).isEmpty();
  }

  @DisplayName("should match start and end tags")
  @Test
  void analyzeCompleteTags() {
    // GIVEN
    final String text = "<b><span><span></span></span><br></B>";

    // WHEN
    final PageAnalysis analysis = createPageAnalysis(text);

    // THEN
    final List<TagElement> tags = analysis.getTags();
    Assertions.assertThat(analysis.getCompleteTags())
        .containsExactly(
            new CompleteTagElement(tags.get(0), tags.get(6)),
            new CompleteTagElement(tags.get(1), tags.get(4)),
            new CompleteTagElement(tags.get(2), tags.get(3)));
  }

  @DisplayName("should not match unclosed tags")
  @Test
  void analyzeUnclosedTags() {
    // GIVEN
    final String text = "<b>Bold</i>";

    // WHEN
    final PageAnalysis analysis = createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getTags()).hasSize(2);
    Assertions.assertThat(analysis.getCompleteTags()).isEmpty();
  }

  @DisplayName("should not analyze tags nor links inside nowiki")
  @Test
  void analyzeNowiki() {
    // GIVEN
    final String text = "<NoWiki><b>[[Link]]</b></nowiki>[[Other]]";

    // WHEN
    final PageAnalysis analysis = createPageAnalysis(text);

    // THEN
    final TagElement startTag = new TagElement(0, 8, "NoWiki", TagKind.START, List.of());
    final TagElement endTag = new TagElement(23, 32, "nowiki", TagKind.END, List.of());
    Assertions.assertThat(analysis.getTags()).containsExactly(startTag, endTag);
    Assertions.assertThat(analysis.getCompleteTags())
        .containsExactly(new CompleteTagElement(startTag, endTag));
    Assertions.assertThat(analysis.getInternalLinks())
        .containsExactly(new InternalLinkElement(32, text.length(), "Other", null, null));
  }

  @DisplayName("should not analyze links inside pre")
  @Test
  void analyzePre() {
    // GIVEN
    final String text = "<pre>[[Link]]</pre>";

    // WHEN
    final PageAnalysis analysis = createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getCompleteTags()).hasSize(1);
    Assertions.assertThat(analysis.getInternalLinks()).isEmpty();
  }

  @DisplayName("should analyze links after unclosed nowiki")
  @Test
  void analyzeUnclosedNowiki() {
    // GIVEN
    final String text = "<nowiki>[[Link]]";

    // WHEN
    final PageAnalysis analysis = createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getCompleteTags()).isEmpty();
    Assertions.assertThat(analysis.getInternalLinks())
        .containsExactly(new InternalLinkElement(8, text.length(), "Link", null, null));
  }

  @DisplayName("should analyze links inside tags with wiki content")
  @Test
  void analyzeLinkInsideRef() {
    // GIVEN
    final String text = "<ref name=\"a\">[[Link]]</ref>";

    // WHEN
    final PageAnalysis analysis = createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getCompleteTags()).hasSize(1);
    Assertions.assertThat(analysis.getInternalLinks())
        .containsExactly(new InternalLinkElement(14, 22, "Link", null, null));
  }

  @DisplayName("should not analyze links inside tag attributes")
  @Test
  void analyzeLinkInsideTagAttribute() {
    // GIVEN
    final String text = "<span title=\"[[Link]]\">";

    // WHEN
    final PageAnalysis analysis = createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getTags()).hasSize(1);
    Assertions.assertThat(analysis.getInternalLinks()).isEmpty();
  }

  @DisplayName("should analyze internal link followed by a comment")
  @Test
  void analyzeInternalLinkFollowedByComment() {
    // GIVEN
    final String text = "[[Link]]<!-- comment -->";

    // WHEN
    final PageAnalysis analysis = createPageAnalysis(text);

    // THEN
    Assertions.assertThat(analysis.getInternalLinks())
        .containsExactly(new InternalLinkElement(0, 8, "Link", null, null));
  }
}
