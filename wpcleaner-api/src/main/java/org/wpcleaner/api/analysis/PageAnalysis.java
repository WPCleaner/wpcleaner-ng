package org.wpcleaner.api.analysis;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.wpcleaner.api.analysis.comment.CommentContainer;

public final class PageAnalysis {

  private final String title;
  private final String text;
  private final CommentContainer comments;

  public PageAnalysis(final String title, final String text) {
    this.title = title;
    this.text = text;
    this.comments = new CommentContainer(text);
  }

  public String getTitle() {
    return title;
  }

  public String getText() {
    return text;
  }

  public CommentContainer getComments() {
    return comments;
  }
}
