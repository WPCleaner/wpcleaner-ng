package org.wpcleaner.api.analysis.comment;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;

public record CommentedText(String text, List<CommentElement> comments) {

  public CommentedText {
    comments = List.copyOf(comments);
  }

  public String withoutComments(final int begin, final int end) {
    final StringBuilder result = new StringBuilder();
    int index = begin;
    for (final CommentElement comment : commentsBetween(begin, end)) {
      result.append(text, index, comment.begin());
      index = comment.end();
    }
    result.append(text, index, end);
    return result.toString();
  }

  public int indexOutsideComments(final char searched, final int begin, final int end) {
    int index = begin;
    for (final CommentElement comment : commentsBetween(begin, end)) {
      final int found = text.substring(index, comment.begin()).indexOf(searched);
      if (found >= 0) {
        return index + found;
      }
      index = comment.end();
    }
    final int found = text.substring(index, end).indexOf(searched);
    return found >= 0 ? index + found : -1;
  }

  public int skipWhitespaceAndComments(final int begin, final int end) {
    int index = begin;
    boolean skipped = true;
    while (skipped) {
      skipped = false;
      while (index < end && Character.isWhitespace(text.charAt(index))) {
        index++;
      }
      for (final CommentElement comment : comments) {
        if (comment.begin() == index && comment.end() <= end) {
          index = comment.end();
          skipped = true;
        }
      }
    }
    return index;
  }

  private List<CommentElement> commentsBetween(final int begin, final int end) {
    return comments.stream()
        .filter(comment -> comment.begin() >= begin && comment.end() <= end)
        .toList();
  }
}
