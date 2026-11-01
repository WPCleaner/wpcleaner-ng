package org.wpcleaner.api.analysis.preprocessor;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import org.wpcleaner.api.analysis.Element;
import org.wpcleaner.api.analysis.comment.CommentElement;
import org.wpcleaner.api.analysis.function.FunctionElement;
import org.wpcleaner.api.analysis.include.IncludeAreaElement;
import org.wpcleaner.api.analysis.parameter.ParameterElement;
import org.wpcleaner.api.analysis.tag.CompleteTagElement;
import org.wpcleaner.api.analysis.tag.TagElement;
import org.wpcleaner.api.analysis.template.TemplateElement;
import org.wpcleaner.api.repository.extensiontag.ExtensionTag;
import org.wpcleaner.api.repository.extensiontag.ExtensionTagRepository;
import org.wpcleaner.api.repository.magicword.MagicWordRepository;
import org.wpcleaner.api.wiki.definition.WikiDefinition;

@SuppressWarnings("PMD.DataClass")
public class PreprocessorContainer {

  private final WikiDefinition wiki;
  private final String text;
  private final ExtensionTagRepository extensionTagRepository;
  private final MagicWordRepository magicWordRepository;
  private final List<CommentElement> comments;
  private final List<TagElement> tags;
  private final List<CompleteTagElement> completeTags;
  private final List<IncludeAreaElement> includeAreas;
  private final List<TemplateElement> templates;
  private final List<FunctionElement> functions;
  private final List<ParameterElement> parameters;
  private final List<Element> exclusions;
  private final Lock lock = new ReentrantLock();
  private boolean done;

  public PreprocessorContainer(
      final WikiDefinition wiki,
      final String text,
      final ExtensionTagRepository extensionTagRepository,
      final MagicWordRepository magicWordRepository) {
    this.wiki = wiki;
    this.text = text;
    this.extensionTagRepository = extensionTagRepository;
    this.magicWordRepository = magicWordRepository;
    this.comments = new ArrayList<>();
    this.tags = new ArrayList<>();
    this.completeTags = new ArrayList<>();
    this.includeAreas = new ArrayList<>();
    this.templates = new ArrayList<>();
    this.functions = new ArrayList<>();
    this.parameters = new ArrayList<>();
    this.exclusions = new ArrayList<>();
    this.done = false;
  }

  public List<CommentElement> getComments() {
    ensureAnalyzed();
    return comments;
  }

  public List<TagElement> getTags() {
    ensureAnalyzed();
    return tags;
  }

  public List<CompleteTagElement> getCompleteTags() {
    ensureAnalyzed();
    return completeTags;
  }

  public List<TemplateElement> getTemplates() {
    ensureAnalyzed();
    return templates;
  }

  public List<FunctionElement> getFunctions() {
    ensureAnalyzed();
    return functions;
  }

  public List<ParameterElement> getParameters() {
    ensureAnalyzed();
    return parameters;
  }

  public List<Element> getCurlyBracketElements() {
    ensureAnalyzed();
    final List<Element> curlyBracketElements = new ArrayList<>(templates);
    curlyBracketElements.addAll(functions);
    curlyBracketElements.addAll(parameters);
    return curlyBracketElements;
  }

  public List<IncludeAreaElement> getIncludeAreas() {
    ensureAnalyzed();
    return includeAreas;
  }

  public List<Element> getExclusions() {
    ensureAnalyzed();
    return exclusions;
  }

  private void ensureAnalyzed() {
    lock.lock();
    try {
      if (!done) {
        final Set<String> extensionTagNames =
            extensionTagRepository.getExtensionTags(wiki).stream()
                .map(ExtensionTag::name)
                .collect(Collectors.toUnmodifiableSet());
        final CurlyBracketElementFactory curlyBracketElementFactory =
            new CurlyBracketElementFactory(text, magicWordRepository.getMagicWords(wiki));
        final PreprocessorAnalyzer analyzer =
            new PreprocessorAnalyzer(text, extensionTagNames, curlyBracketElementFactory);
        analyzer.analyze();
        comments.addAll(analyzer.getComments());
        tags.addAll(analyzer.getTags());
        completeTags.addAll(analyzer.getCompleteTags());
        includeAreas.addAll(analyzer.getIncludeAreas());
        templates.addAll(curlyBracketElementFactory.getTemplates());
        functions.addAll(curlyBracketElementFactory.getFunctions());
        parameters.addAll(curlyBracketElementFactory.getParameters());
        exclusions.addAll(analyzer.getExclusions());
        done = true;
      }
    } finally {
      lock.unlock();
    }
  }
}
