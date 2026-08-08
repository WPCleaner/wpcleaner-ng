package org.wpcleaner.api.analysis;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import org.wpcleaner.api.analysis.comment.CommentContainer;
import org.wpcleaner.api.analysis.comment.CommentElement;
import org.wpcleaner.api.analysis.tag.TagContainer;
import org.wpcleaner.api.analysis.tag.TagElement;

@SuppressWarnings("PMD.DataClass")
public final class PageAnalysis {

  private final String title;
  private final String text;
  private final CommentContainer comments;
  private final TagContainer tags;

  public PageAnalysis(final String title, final String text) {
    this.title = title;
    this.text = text;
    this.comments = new CommentContainer(text);
    this.tags = new TagContainer(text, comments);
  }

  public String getTitle() {
    return title;
  }

  public String getText() {
    return text;
  }

  public List<CommentElement> getComments() {
    return comments.getComments();
  }

  public List<TagElement> getTags() {
    return tags.getTags();
  }
}
