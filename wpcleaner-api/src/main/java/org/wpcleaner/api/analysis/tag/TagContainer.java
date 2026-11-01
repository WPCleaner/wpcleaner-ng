package org.wpcleaner.api.analysis.tag;

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
import org.wpcleaner.api.analysis.comment.CommentContainer;

public class TagContainer {

  private final String text;
  private final CommentContainer comments;
  private final List<TagElement> tags;
  private final List<CompleteTagElement> completeTags;
  private final Lock lock = new ReentrantLock();
  private boolean done;

  public TagContainer(final String text, final CommentContainer comments) {
    this.text = text;
    this.comments = comments;
    this.tags = new ArrayList<>();
    this.completeTags = new ArrayList<>();
    this.done = false;
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
    final List<Element> exclusions = new ArrayList<>(tags);
    completeTags.stream()
        .filter(completeTag -> !completeTag.startTag().hasWikiContent())
        .forEach(exclusions::add);
    return exclusions;
  }

  private void ensureAnalyzed() {
    lock.lock();
    try {
      if (!done) {
        final TextBrowser textBrowser = new TextBrowser(text);
        textBrowser.addExclusions(comments.getComments());
        final TagAnalyzer analyzer = new TagAnalyzer(text, textBrowser);
        analyzer.analyze();
        tags.addAll(analyzer.getTags());
        completeTags.addAll(analyzer.getCompleteTags());
        done = true;
      }
    } finally {
      lock.unlock();
    }
  }
}
