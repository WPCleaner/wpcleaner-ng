package org.wpcleaner.api.analysis.title;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

public record TitleBuilder(int level, String title) {

  private static final String EQUAL = "=";
  private static final String SPACE = " ";

  public static TitleBuilder from(final int level, final String title) {
    return new TitleBuilder(level, title);
  }

  public String build() {
    final String equals = EQUAL.repeat(level);
    final String strippedTitle = title.strip();
    return equals + SPACE + strippedTitle + (strippedTitle.isEmpty() ? "" : SPACE) + equals;
  }
}
