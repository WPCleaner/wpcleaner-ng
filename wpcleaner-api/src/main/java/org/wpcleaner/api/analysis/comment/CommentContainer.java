package org.wpcleaner.api.analysis.comment;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CommentContainer {

  private final String text;
  private final List<CommentElement> comments;
  private final Lock lock = new ReentrantLock();
  private boolean done;

  public CommentContainer(final String text) {
    this.text = text;
    this.comments = new ArrayList<>();
    this.done = false;
  }

  public List<CommentElement> getComments() {
    lock.lock();
    try {
      if (!done) {
        comments.addAll(new CommentAnalyzer(text).analyze());
        done = true;
      }
    } finally {
      lock.unlock();
    }
    return comments;
  }
}
