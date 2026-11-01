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
import org.wpcleaner.api.analysis.tag.CompleteTagElement;
import org.wpcleaner.api.analysis.tag.TagElement;
import org.wpcleaner.api.repository.extensiontag.ExtensionTag;
import org.wpcleaner.api.repository.extensiontag.ExtensionTagRepository;
import org.wpcleaner.api.wiki.definition.WikiDefinition;

@SuppressWarnings("PMD.DataClass")
public class PreprocessorContainer {

  private final WikiDefinition wiki;
  private final String text;
  private final ExtensionTagRepository extensionTagRepository;
  private final List<CommentElement> comments;
  private final List<TagElement> tags;
  private final List<CompleteTagElement> completeTags;
  private final List<Element> exclusions;
  private final Lock lock = new ReentrantLock();
  private boolean done;

  public PreprocessorContainer(
      final WikiDefinition wiki,
      final String text,
      final ExtensionTagRepository extensionTagRepository) {
    this.wiki = wiki;
    this.text = text;
    this.extensionTagRepository = extensionTagRepository;
    this.comments = new ArrayList<>();
    this.tags = new ArrayList<>();
    this.completeTags = new ArrayList<>();
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
        final PreprocessorAnalyzer analyzer = new PreprocessorAnalyzer(text, extensionTagNames);
        analyzer.analyze();
        comments.addAll(analyzer.getComments());
        tags.addAll(analyzer.getTags());
        completeTags.addAll(analyzer.getCompleteTags());
        exclusions.addAll(analyzer.getExclusions());
        done = true;
      }
    } finally {
      lock.unlock();
    }
  }
}
