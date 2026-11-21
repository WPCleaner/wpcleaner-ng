package org.wpcleaner.lib.check.algorithm.a0xx.a00x.a008;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.stream.Stream;
import org.wpcleaner.api.analysis.title.TitleBuilder;
import org.wpcleaner.lib.check.Replacement;

final class TitleReplacements {

  static final char EQUAL = '=';
  private static final String SPACE = " ";
  private static final String NEW_LINE = "\n";

  private TitleReplacements() {}

  static List<Replacement> create(final String line, final int equalsCount) {
    final String afterEquals = line.substring(equalsCount);
    if (afterEquals.isEmpty()) {
      return toReplacements(Stream.of(""));
    }
    final int secondEqualsBegin = afterEquals.indexOf(EQUAL);
    if (secondEqualsBegin < 0) {
      return toReplacements(Stream.of(buildTitle(equalsCount, afterEquals)));
    }
    final String title = afterEquals.substring(0, secondEqualsBegin);
    final int secondEqualsCount = countEquals(afterEquals, secondEqualsBegin);
    final String afterTitle = afterEquals.substring(secondEqualsBegin + secondEqualsCount).strip();
    final String titleAlone = buildTitle(equalsCount, title);
    final String titleThenText = titleAlone + NEW_LINE + afterTitle;
    if (secondEqualsCount >= equalsCount) {
      return toReplacements(
          Stream.of(titleThenText, buildTitle(equalsCount, title.strip() + SPACE + afterTitle)));
    }
    return toReplacements(
        Stream.of(
            buildTitle(equalsCount, afterEquals),
            titleAlone,
            buildTitle(secondEqualsCount, title),
            titleThenText));
  }

  static int countEquals(final String line, final int from) {
    int index = from;
    while (index < line.length() && line.charAt(index) == EQUAL) {
      index++;
    }
    return index - from;
  }

  private static List<Replacement> toReplacements(final Stream<String> texts) {
    return texts.map(text -> new Replacement(text, false)).toList();
  }

  private static String buildTitle(final int level, final String title) {
    return TitleBuilder.from(level, title).build();
  }
}
