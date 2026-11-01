package org.wpcleaner.api.analysis.wiki;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.wpcleaner.api.analysis.Element;
import org.wpcleaner.api.analysis.TextBrowser;
import org.wpcleaner.api.analysis.category.CategoryElement;
import org.wpcleaner.api.analysis.externallink.ExternalLinkElement;
import org.wpcleaner.api.analysis.image.ImageElement;
import org.wpcleaner.api.analysis.internallink.InternalLinkElement;
import org.wpcleaner.api.analysis.interwikilink.InterwikiLinkElement;
import org.wpcleaner.api.analysis.languagelink.LanguageLinkElement;
import org.wpcleaner.api.analysis.preprocessor.PreprocessorContainer;
import org.wpcleaner.api.analysis.tag.TagContainer;
import org.wpcleaner.api.repository.interwiki.InterwikiRepository;
import org.wpcleaner.api.repository.magicword.MagicWordRepository;
import org.wpcleaner.api.repository.namespace.NamespaceRepository;
import org.wpcleaner.api.repository.protocol.ProtocolRepository;
import org.wpcleaner.api.wiki.definition.WikiDefinition;

@SuppressWarnings("PMD.DataClass")
public class WikiContainer {

  private final WikiDefinition wiki;
  private final String text;
  private final InterwikiRepository interwikiRepository;
  private final MagicWordRepository magicWordRepository;
  private final NamespaceRepository namespaceRepository;
  private final ProtocolRepository protocolRepository;
  private final PreprocessorContainer preprocessor;
  private final TagContainer tags;
  private final List<CategoryElement> categories;
  private final List<ExternalLinkElement> externalLinks;
  private final List<ImageElement> images;
  private final List<InternalLinkElement> internalLinks;
  private final List<InterwikiLinkElement> interwikiLinks;
  private final List<LanguageLinkElement> languageLinks;
  private final Lock lock = new ReentrantLock();
  private boolean done;

  public WikiContainer(
      final WikiDefinition wiki,
      final String text,
      final PreprocessorContainer preprocessor,
      final TagContainer tags,
      final InterwikiRepository interwikiRepository,
      final MagicWordRepository magicWordRepository,
      final NamespaceRepository namespaceRepository,
      final ProtocolRepository protocolRepository) {
    this.wiki = wiki;
    this.text = text;
    this.interwikiRepository = interwikiRepository;
    this.magicWordRepository = magicWordRepository;
    this.namespaceRepository = namespaceRepository;
    this.protocolRepository = protocolRepository;
    this.preprocessor = preprocessor;
    this.tags = tags;
    this.categories = new ArrayList<>();
    this.externalLinks = new ArrayList<>();
    this.images = new ArrayList<>();
    this.internalLinks = new ArrayList<>();
    this.interwikiLinks = new ArrayList<>();
    this.languageLinks = new ArrayList<>();
    this.done = false;
  }

  public List<CategoryElement> getCategories() {
    ensureAnalyzed();
    return categories;
  }

  public List<ExternalLinkElement> getExternalLinks() {
    ensureAnalyzed();
    return externalLinks;
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

  private void ensureAnalyzed() {
    lock.lock();
    try {
      if (!done) {
        final TextBrowser textBrowser = new TextBrowser(text);
        textBrowser.addExclusions(tags.getExclusions());
        final ImageAnalyzer imageAnalyzer =
            new ImageAnalyzer(
                text,
                textBrowser,
                namespaceRepository.getNamespaces(wiki),
                new ImageParameterFactory(magicWordRepository.getMagicWords(wiki)),
                preprocessor.getCurlyBracketElements());
        final WikiAnalyzer analyzer =
            new WikiAnalyzer(
                text,
                textBrowser,
                interwikiRepository.getInterwikis(wiki),
                namespaceRepository.getNamespaces(wiki),
                protocolRepository.getProtocols(wiki),
                imageAnalyzer);
        analyzer.analyze();
        categories.addAll(analyzer.getCategories());
        externalLinks.addAll(analyzer.getExternalLinks());
        images.addAll(analyzer.getImages());
        images.sort(Element.COMPARATOR);
        internalLinks.addAll(analyzer.getInternalLinks());
        interwikiLinks.addAll(analyzer.getInterwikiLinks());
        languageLinks.addAll(analyzer.getLanguageLinks());
        done = true;
      }
    } finally {
      lock.unlock();
    }
  }
}
