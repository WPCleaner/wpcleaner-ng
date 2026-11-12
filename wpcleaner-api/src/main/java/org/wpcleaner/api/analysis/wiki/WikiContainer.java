package org.wpcleaner.api.analysis.wiki;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.api.analysis.Element;
import org.wpcleaner.api.analysis.TextBrowser;
import org.wpcleaner.api.analysis.behaviorswitch.BehaviorSwitchElement;
import org.wpcleaner.api.analysis.category.CategoryElement;
import org.wpcleaner.api.analysis.comment.CommentedText;
import org.wpcleaner.api.analysis.externallink.ExternalLinkElement;
import org.wpcleaner.api.analysis.gallery.GalleryElement;
import org.wpcleaner.api.analysis.image.ImageElement;
import org.wpcleaner.api.analysis.internallink.InternalLinkElement;
import org.wpcleaner.api.analysis.interwikilink.InterwikiLinkElement;
import org.wpcleaner.api.analysis.languagelink.LanguageLinkElement;
import org.wpcleaner.api.analysis.listitem.ListItemElement;
import org.wpcleaner.api.analysis.magiclink.MagicLinkElement;
import org.wpcleaner.api.analysis.pagetitle.PageTitleNormalizer;
import org.wpcleaner.api.analysis.paragraph.ParagraphElement;
import org.wpcleaner.api.analysis.preformatted.PreformattedElement;
import org.wpcleaner.api.analysis.preprocessor.PreprocessorContainer;
import org.wpcleaner.api.analysis.redirect.RedirectElement;
import org.wpcleaner.api.analysis.table.TableElement;
import org.wpcleaner.api.analysis.tag.TagContainer;
import org.wpcleaner.api.repository.interwiki.InterwikiRepository;
import org.wpcleaner.api.repository.magiclink.MagicLinkRepository;
import org.wpcleaner.api.repository.magicword.MagicWord;
import org.wpcleaner.api.repository.magicword.MagicWordRepository;
import org.wpcleaner.api.repository.namespace.NamespaceRepository;
import org.wpcleaner.api.repository.protocol.ProtocolRepository;
import org.wpcleaner.api.wiki.definition.WikiDefinition;

@SuppressWarnings({"PMD.CouplingBetweenObjects", "PMD.DataClass"})
public class WikiContainer {

  private final WikiDefinition wiki;
  private final String text;
  private final InterwikiRepository interwikiRepository;
  private final MagicLinkRepository magicLinkRepository;
  private final MagicWordRepository magicWordRepository;
  private final NamespaceRepository namespaceRepository;
  private final ProtocolRepository protocolRepository;
  private final PreprocessorContainer preprocessor;
  private final TagContainer tags;
  private final List<BehaviorSwitchElement> behaviorSwitches;
  private final List<CategoryElement> categories;
  private final List<ExternalLinkElement> externalLinks;
  private final List<GalleryElement> galleries;
  private final List<ImageElement> images;
  private final List<InternalLinkElement> internalLinks;
  private final List<InterwikiLinkElement> interwikiLinks;
  private final List<LanguageLinkElement> languageLinks;
  private final List<ListItemElement> listItems;
  private final List<MagicLinkElement> magicLinks;
  private final List<ParagraphElement> paragraphs;
  private final List<PreformattedElement> preformattedBlocks;
  private final List<TableElement> tables;
  @Nullable private RedirectElement redirect;
  private final Lock lock = new ReentrantLock();
  private boolean done;

  public WikiContainer(
      final WikiDefinition wiki,
      final String text,
      final PreprocessorContainer preprocessor,
      final TagContainer tags,
      final InterwikiRepository interwikiRepository,
      final MagicLinkRepository magicLinkRepository,
      final MagicWordRepository magicWordRepository,
      final NamespaceRepository namespaceRepository,
      final ProtocolRepository protocolRepository) {
    this.wiki = wiki;
    this.text = text;
    this.interwikiRepository = interwikiRepository;
    this.magicLinkRepository = magicLinkRepository;
    this.magicWordRepository = magicWordRepository;
    this.namespaceRepository = namespaceRepository;
    this.protocolRepository = protocolRepository;
    this.preprocessor = preprocessor;
    this.tags = tags;
    this.behaviorSwitches = new ArrayList<>();
    this.categories = new ArrayList<>();
    this.externalLinks = new ArrayList<>();
    this.galleries = new ArrayList<>();
    this.images = new ArrayList<>();
    this.internalLinks = new ArrayList<>();
    this.interwikiLinks = new ArrayList<>();
    this.languageLinks = new ArrayList<>();
    this.listItems = new ArrayList<>();
    this.magicLinks = new ArrayList<>();
    this.paragraphs = new ArrayList<>();
    this.preformattedBlocks = new ArrayList<>();
    this.tables = new ArrayList<>();
    this.done = false;
  }

  public List<BehaviorSwitchElement> getBehaviorSwitches() {
    ensureAnalyzed();
    return behaviorSwitches;
  }

  public List<CategoryElement> getCategories() {
    ensureAnalyzed();
    return categories;
  }

  public List<ExternalLinkElement> getExternalLinks() {
    ensureAnalyzed();
    return externalLinks;
  }

  public List<GalleryElement> getGalleries() {
    ensureAnalyzed();
    return galleries;
  }

  public List<ImageElement> getImages() {
    ensureAnalyzed();
    return images;
  }

  public List<InternalLinkElement> getInternalLinks() {
    ensureAnalyzed();
    return internalLinks;
  }

  public List<InterwikiLinkElement> getInterwikiLinks() {
    ensureAnalyzed();
    return interwikiLinks;
  }

  public List<LanguageLinkElement> getLanguageLinks() {
    ensureAnalyzed();
    return languageLinks;
  }

  public List<ListItemElement> getListItems() {
    ensureAnalyzed();
    return listItems;
  }

  public List<MagicLinkElement> getMagicLinks() {
    ensureAnalyzed();
    return magicLinks;
  }

  public List<ParagraphElement> getParagraphs() {
    ensureAnalyzed();
    return paragraphs;
  }

  public List<PreformattedElement> getPreformattedBlocks() {
    ensureAnalyzed();
    return preformattedBlocks;
  }

  public List<TableElement> getTables() {
    ensureAnalyzed();
    return tables;
  }

  public Optional<RedirectElement> getRedirect() {
    ensureAnalyzed();
    return Optional.ofNullable(redirect);
  }

  private void ensureAnalyzed() {
    lock.lock();
    try {
      if (!done) {
        final List<MagicWord> magicWords = magicWordRepository.getMagicWords(wiki);
        final TitleDecoder titleDecoder =
            new TitleDecoder(new PageTitleNormalizer(namespaceRepository.getNamespaces(wiki)));
        final Optional<RedirectAnalyzer.Redirect> redirectArea =
            new RedirectAnalyzer(text, magicWords, titleDecoder).analyze();
        final int contentBegin =
            redirectArea.map(RedirectAnalyzer.Redirect::contentBegin).orElse(0);
        final TextBrowser textBrowser = new TextBrowser(text);
        textBrowser.addExclusions(
            GalleryCaptions.excludeOutside(
                tags.getExclusions(), GalleryCaptions.find(preprocessor.getCompleteTags())));
        redirectArea.ifPresent(
            area -> textBrowser.addExclusions(List.of(new Area(0, area.contentBegin()))));
        final ImageAnalyzer imageAnalyzer =
            new ImageAnalyzer(
                text,
                textBrowser,
                namespaceRepository.getNamespaces(wiki),
                new ImageParameterFactory(magicWords),
                preprocessor.getCurlyBracketElements());
        final TextContainers containers = new TextContainers(text, preprocessor);
        final SquareBracketAnalyzer squareBracketAnalyzer =
            new SquareBracketAnalyzer(
                text,
                new CommentedText(text, preprocessor.getComments()),
                interwikiRepository.getInterwikis(wiki),
                namespaceRepository.getNamespaces(wiki),
                protocolRepository.getProtocols(wiki),
                imageAnalyzer,
                containers);
        redirect =
            redirectArea
                .map(
                    area ->
                        area.toElement(
                            squareBracketAnalyzer.persistRedirectLink(
                                area.linkBegin(), area.end(), area.target(), area.linkText())))
                .orElse(null);
        final WikiAnalyzer analyzer =
            new WikiAnalyzer(
                text,
                textBrowser,
                squareBracketAnalyzer,
                imageAnalyzer,
                new BehaviorSwitchAnalyzer(text, textBrowser, magicWords),
                new BlockLevelAnalyzer(
                    text, textBrowser, preprocessor, containers, tags.getTags(), contentBegin));
        analyzer.analyze();
        behaviorSwitches.addAll(analyzer.getBehaviorSwitches());
        categories.addAll(analyzer.getCategories());
        categories.sort(Element.COMPARATOR);
        externalLinks.addAll(analyzer.getExternalLinks());
        images.addAll(analyzer.getImages());
        images.sort(Element.COMPARATOR);
        internalLinks.addAll(analyzer.getInternalLinks());
        internalLinks.sort(Element.COMPARATOR);
        interwikiLinks.addAll(analyzer.getInterwikiLinks());
        interwikiLinks.sort(Element.COMPARATOR);
        languageLinks.addAll(analyzer.getLanguageLinks());
        languageLinks.sort(Element.COMPARATOR);
        final MagicLinkAnalyzer magicLinkAnalyzer =
            new MagicLinkAnalyzer(
                text,
                textBrowser,
                magicLinkRepository.getMagicLinks(wiki),
                protocolRepository.getProtocols(wiki),
                containers,
                preprocessor.getCurlyBracketElements());
        magicLinkAnalyzer.analyze(
            Stream.of(categories, externalLinks, internalLinks, interwikiLinks, languageLinks)
                .<Element>flatMap(List::stream)
                .toList(),
            images);
        magicLinks.addAll(magicLinkAnalyzer.getMagicLinks());
        galleries.addAll(
            new GalleryAnalyzer(
                    text,
                    textBrowser,
                    titleDecoder,
                    ImageParameterFactory.forGallery(magicWords),
                    preprocessor.getCurlyBracketElements(),
                    preprocessor.getComments())
                .analyze(preprocessor.getCompleteTags()));
        externalLinks.addAll(magicLinkAnalyzer.getFreeExternalLinks());
        externalLinks.sort(Element.COMPARATOR);
        analyzer.analyzeDefinitions(
            Stream.of(
                    categories,
                    externalLinks,
                    images,
                    internalLinks,
                    interwikiLinks,
                    languageLinks,
                    magicLinks,
                    preprocessor.getCurlyBracketElements(),
                    preprocessor.getComments(),
                    tags.getTags(),
                    tags.getCompleteTags())
                .<Element>flatMap(List::stream)
                .toList());
        listItems.addAll(analyzer.getListItems());
        paragraphs.addAll(analyzer.getParagraphs());
        preformattedBlocks.addAll(analyzer.getPreformattedBlocks());
        tables.addAll(analyzer.getTables());
        done = true;
      }
    } finally {
      lock.unlock();
    }
  }
}
