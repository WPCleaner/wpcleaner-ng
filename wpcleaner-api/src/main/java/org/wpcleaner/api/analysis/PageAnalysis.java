package org.wpcleaner.api.analysis;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import org.wpcleaner.api.analysis.category.CategoryElement;
import org.wpcleaner.api.analysis.comment.CommentElement;
import org.wpcleaner.api.analysis.externallink.ExternalLinkElement;
import org.wpcleaner.api.analysis.function.FunctionElement;
import org.wpcleaner.api.analysis.include.IncludeAreaElement;
import org.wpcleaner.api.analysis.internallink.InternalLinkElement;
import org.wpcleaner.api.analysis.interwikilink.InterwikiLinkElement;
import org.wpcleaner.api.analysis.languagelink.LanguageLinkElement;
import org.wpcleaner.api.analysis.parameter.ParameterElement;
import org.wpcleaner.api.analysis.preprocessor.PreprocessorContainer;
import org.wpcleaner.api.analysis.tag.CompleteTagElement;
import org.wpcleaner.api.analysis.tag.TagContainer;
import org.wpcleaner.api.analysis.tag.TagElement;
import org.wpcleaner.api.analysis.template.TemplateElement;
import org.wpcleaner.api.analysis.wiki.WikiContainer;
import org.wpcleaner.api.repository.extensiontag.ExtensionTagRepository;
import org.wpcleaner.api.repository.interwiki.InterwikiRepository;
import org.wpcleaner.api.repository.magicword.MagicWordRepository;
import org.wpcleaner.api.repository.namespace.NamespaceRepository;
import org.wpcleaner.api.repository.protocol.ProtocolRepository;
import org.wpcleaner.api.wiki.definition.WikiDefinition;

@SuppressWarnings("PMD.CouplingBetweenObjects")
public final class PageAnalysis {

  private final String title;
  private final String text;
  private final PreprocessorContainer preprocessor;
  private final TagContainer tags;
  private final WikiContainer wikiElements;

  public PageAnalysis(
      final WikiDefinition wiki,
      final String title,
      final String text,
      final ExtensionTagRepository extensionTagRepository,
      final InterwikiRepository interwikiRepository,
      final MagicWordRepository magicWordRepository,
      final NamespaceRepository namespaceRepository,
      final ProtocolRepository protocolRepository) {
    this.title = title;
    this.text = text;
    this.preprocessor =
        new PreprocessorContainer(wiki, text, extensionTagRepository, magicWordRepository);
    this.tags = new TagContainer(text, preprocessor);
    this.wikiElements =
        new WikiContainer(
            wiki, text, tags, interwikiRepository, namespaceRepository, protocolRepository);
  }

  public String getTitle() {
    return title;
  }

  public String getText() {
    return text;
  }

  public List<CommentElement> getComments() {
    return preprocessor.getComments();
  }

  public List<CategoryElement> getCategories() {
    return wikiElements.getCategories();
  }

  public List<IncludeAreaElement> getIncludeAreas() {
    return preprocessor.getIncludeAreas();
  }

  public List<ExternalLinkElement> getExternalLinks() {
    return wikiElements.getExternalLinks();
  }

  public List<InternalLinkElement> getInternalLinks() {
    return wikiElements.getInternalLinks();
  }

  public List<InterwikiLinkElement> getInterwikiLinks() {
    return wikiElements.getInterwikiLinks();
  }

  public List<LanguageLinkElement> getLanguageLinks() {
    return wikiElements.getLanguageLinks();
  }

  public List<TemplateElement> getTemplates() {
    return preprocessor.getTemplates();
  }

  public List<FunctionElement> getFunctions() {
    return preprocessor.getFunctions();
  }

  public List<ParameterElement> getParameters() {
    return preprocessor.getParameters();
  }

  public List<TagElement> getTags() {
    return tags.getTags();
  }

  public List<CompleteTagElement> getCompleteTags() {
    return tags.getCompleteTags();
  }
}
