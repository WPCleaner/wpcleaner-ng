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
import org.wpcleaner.api.analysis.comment.CommentContainer;
import org.wpcleaner.api.analysis.internallink.InternalLinkElement;

public class WikiContainer {

  private final String text;
  private final CommentContainer comments;
  private final List<InternalLinkElement> internalLinks;
  private final Lock lock = new ReentrantLock();
  private boolean done;

  public WikiContainer(final String text, final CommentContainer comments) {
    this.text = text;
    this.comments = comments;
    this.internalLinks = new ArrayList<>();
    this.done = false;
  }

  public List<InternalLinkElement> getInternalLinks() {
    ensureAnalyzed();
    return internalLinks;
  }

  private void ensureAnalyzed() {
    lock.lock();
    try {
      if (!done) {
        final TextBrowser textBrowser = new TextBrowser(text);
        textBrowser.addExclusions(comments.getComments());
        final WikiAnalyzer analyzer = new WikiAnalyzer(text, textBrowser);
        analyzer.analyze();
        internalLinks.addAll(analyzer.getInternalLinks());
        done = true;
      }
    } finally {
      lock.unlock();
    }
  }
}
