package org.wpcleaner.api.analysis.wiki;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.wpcleaner.api.analysis.TextBrowser;
import org.wpcleaner.api.analysis.category.CategoryElement;
import org.wpcleaner.api.analysis.comment.CommentContainer;
import org.wpcleaner.api.analysis.externallink.ExternalLinkElement;
import org.wpcleaner.api.analysis.internallink.InternalLinkElement;
import org.wpcleaner.api.analysis.interwikilink.InterwikiLinkElement;
import org.wpcleaner.api.analysis.languagelink.LanguageLinkElement;
import org.wpcleaner.api.repository.interwiki.InterwikiRepository;
import org.wpcleaner.api.repository.namespace.NamespaceRepository;
import org.wpcleaner.api.repository.protocol.ProtocolRepository;

@SuppressWarnings("PMD.DataClass")
public class WikiContainer {

  private final String text;
  private final InterwikiRepository interwikiRepository;
  private final NamespaceRepository namespaceRepository;
  private final ProtocolRepository protocolRepository;
  private final CommentContainer comments;
  private final List<CategoryElement> categories;
  private final List<ExternalLinkElement> externalLinks;
  private final List<InternalLinkElement> internalLinks;
  private final List<InterwikiLinkElement> interwikiLinks;
  private final List<LanguageLinkElement> languageLinks;
  private final Lock lock = new ReentrantLock();
  private boolean done;

  public WikiContainer(
      final String text,
      final CommentContainer comments,
      final InterwikiRepository interwikiRepository,
      final NamespaceRepository namespaceRepository,
      final ProtocolRepository protocolRepository) {
    this.text = text;
    this.interwikiRepository = interwikiRepository;
    this.namespaceRepository = namespaceRepository;
    this.protocolRepository = protocolRepository;
    this.comments = comments;
    this.categories = new ArrayList<>();
    this.externalLinks = new ArrayList<>();
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
        textBrowser.addExclusions(comments.getComments());
        final WikiAnalyzer analyzer =
            new WikiAnalyzer(
                text,
                textBrowser,
                interwikiRepository.getInterwikis(),
                namespaceRepository.getNamespaces(),
                protocolRepository.getProtocols());
        analyzer.analyze();
        categories.addAll(analyzer.getCategories());
        externalLinks.addAll(analyzer.getExternalLinks());
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
