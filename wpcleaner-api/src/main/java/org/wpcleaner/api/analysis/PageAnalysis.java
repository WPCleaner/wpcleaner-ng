package org.wpcleaner.api.analysis;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Optional;
import org.wpcleaner.api.analysis.behaviorswitch.BehaviorSwitchElement;
import org.wpcleaner.api.analysis.category.CategoryElement;
import org.wpcleaner.api.analysis.comment.CommentElement;
import org.wpcleaner.api.analysis.externallink.ExternalLinkElement;
import org.wpcleaner.api.analysis.function.FunctionElement;
import org.wpcleaner.api.analysis.gallery.GalleryElement;
import org.wpcleaner.api.analysis.image.ImageElement;
import org.wpcleaner.api.analysis.include.IncludeAreaElement;
import org.wpcleaner.api.analysis.internallink.InternalLinkElement;
import org.wpcleaner.api.analysis.interwikilink.InterwikiLinkElement;
import org.wpcleaner.api.analysis.languagelink.LanguageLinkElement;
import org.wpcleaner.api.analysis.listitem.ListItemElement;
import org.wpcleaner.api.analysis.magiclink.MagicLinkElement;
import org.wpcleaner.api.analysis.pagetitle.PageTitle;
import org.wpcleaner.api.analysis.pagetitle.PageTitleNormalizer;
import org.wpcleaner.api.analysis.paragraph.ParagraphElement;
import org.wpcleaner.api.analysis.parameter.ParameterElement;
import org.wpcleaner.api.analysis.preformatted.PreformattedElement;
import org.wpcleaner.api.analysis.preprocessor.PreprocessorContainer;
import org.wpcleaner.api.analysis.redirect.RedirectElement;
import org.wpcleaner.api.analysis.table.TableElement;
import org.wpcleaner.api.analysis.tag.CompleteTagElement;
import org.wpcleaner.api.analysis.tag.TagContainer;
import org.wpcleaner.api.analysis.tag.TagElement;
import org.wpcleaner.api.analysis.template.TemplateElement;
import org.wpcleaner.api.analysis.title.TitleElement;
import org.wpcleaner.api.analysis.wiki.WikiContainer;
import org.wpcleaner.api.repository.extensiontag.ExtensionTagRepository;
import org.wpcleaner.api.repository.interwiki.InterwikiRepository;
import org.wpcleaner.api.repository.magiclink.MagicLinkRepository;
import org.wpcleaner.api.repository.magicword.MagicWordRepository;
import org.wpcleaner.api.repository.namespace.CommonNamespaces;
import org.wpcleaner.api.repository.namespace.NamespaceRepository;
import org.wpcleaner.api.repository.protocol.ProtocolRepository;
import org.wpcleaner.api.wiki.definition.WikiDefinition;

@SuppressWarnings({"PMD.CouplingBetweenObjects", "PMD.ExcessiveImports"})
public final class PageAnalysis {

  private static final String WINDOWS_LINE_END = "\r\n";
  private static final String LINE_END = "\n";

  private final WikiDefinition wiki;
  private final String title;
  private final PageTitle pageTitle;
  private final PageTitleNormalizer titleNormalizer;
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
      final MagicLinkRepository magicLinkRepository,
      final MagicWordRepository magicWordRepository,
      final NamespaceRepository namespaceRepository,
      final ProtocolRepository protocolRepository) {
    this.wiki = wiki;
    this.title = title;
    this.titleNormalizer = new PageTitleNormalizer(namespaceRepository.getNamespaces(wiki));
    this.pageTitle =
        titleNormalizer
            .normalize(title, CommonNamespaces.MAIN.id)
            .orElseGet(() -> new PageTitle(CommonNamespaces.MAIN.id, title));
    this.text = text.replace(WINDOWS_LINE_END, LINE_END);
    this.preprocessor =
        new PreprocessorContainer(
            wiki, this.text, extensionTagRepository, magicWordRepository, namespaceRepository);
    this.tags = new TagContainer(this.text, preprocessor);
    this.wikiElements =
        new WikiContainer(
            wiki,
            this.text,
            preprocessor,
            tags,
            interwikiRepository,
            magicLinkRepository,
            magicWordRepository,
            namespaceRepository,
            protocolRepository);
  }

  public WikiDefinition getWiki() {
    return wiki;
  }

  public String getTitle() {
    return title;
  }

  public PageTitle getPageTitle() {
    return pageTitle;
  }

  public Optional<PageTitle> normalizeTitle(final String name, final int defaultNamespace) {
    return titleNormalizer.normalize(name, defaultNamespace);
  }

  public String getText() {
    return text;
  }

  public List<CommentElement> getComments() {
    return preprocessor.getComments();
  }

  public List<BehaviorSwitchElement> getBehaviorSwitches() {
    return wikiElements.getBehaviorSwitches();
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

  public List<GalleryElement> getGalleries() {
    return wikiElements.getGalleries();
  }

  public List<ImageElement> getImages() {
    return wikiElements.getImages();
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

  public List<ListItemElement> getListItems() {
    return wikiElements.getListItems();
  }

  public List<MagicLinkElement> getMagicLinks() {
    return wikiElements.getMagicLinks();
  }

  public List<ParagraphElement> getParagraphs() {
    return wikiElements.getParagraphs();
  }

  public List<PreformattedElement> getPreformattedBlocks() {
    return wikiElements.getPreformattedBlocks();
  }

  public List<TableElement> getTables() {
    return wikiElements.getTables();
  }

  public Optional<RedirectElement> getRedirect() {
    return wikiElements.getRedirect();
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

  public List<TitleElement> getTitles() {
    return preprocessor.getTitles();
  }

  public List<TagElement> getTags() {
    return tags.getTags();
  }

  public List<CompleteTagElement> getCompleteTags() {
    return tags.getCompleteTags();
  }
}
