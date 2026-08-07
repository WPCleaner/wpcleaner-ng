package org.wpcleaner.api.analysis.comment;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

public record CommentElement(int begin, int end) {

  public static final String START = "<!--";
  public static final String END = "-->";
}
