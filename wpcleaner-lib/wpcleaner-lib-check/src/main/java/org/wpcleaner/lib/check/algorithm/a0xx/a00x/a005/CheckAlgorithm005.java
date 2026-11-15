package org.wpcleaner.lib.check.algorithm.a0xx.a00x.a005;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import org.springframework.stereotype.Component;
import org.wpcleaner.api.analysis.PageAnalysis;
import org.wpcleaner.api.analysis.comment.CommentElement;
import org.wpcleaner.lib.check.CheckAlgorithm;
import org.wpcleaner.lib.check.CheckError;
import org.wpcleaner.lib.check.Replacement;

@Component
public class CheckAlgorithm005 implements CheckAlgorithm {

  private static final int ALGORITHM_NUMBER = 5;
  private static final String MALFORMED_END = "->";
  private static final int START_LENGTH = CommentElement.TOKEN_START.length();

  @Override
  public int number() {
    return ALGORITHM_NUMBER;
  }

  @Override
  public List<CheckError> analyze(final PageAnalysis analysis) {
    return analysis.getComments().stream()
        .flatMap(comment -> analyze(analysis.getText(), comment).stream())
        .toList();
  }

  private static List<CheckError> analyze(final String text, final CommentElement comment) {
    final List<Integer> starts = findStarts(text, comment);
    final boolean closed = isClosed(text, comment);
    final int unclosedCount = closed ? starts.size() - 1 : starts.size();
    return IntStream.range(0, unclosedCount)
        .mapToObj(
            index ->
                createError(
                    text,
                    starts.get(index),
                    index + 1 < starts.size() ? starts.get(index + 1) : -1,
                    comment.end()))
        .toList();
  }

  private static List<Integer> findStarts(final String text, final CommentElement comment) {
    final List<Integer> starts = new ArrayList<>();
    int index = comment.begin();
    while (index >= 0 && index < comment.end()) {
      starts.add(index);
      index = text.indexOf(CommentElement.TOKEN_START, index + START_LENGTH);
    }
    return starts;
  }

  private static boolean isClosed(final String text, final CommentElement comment) {
    final int endTokenBegin = comment.end() - CommentElement.TOKEN_END.length();
    return endTokenBegin >= comment.begin() + START_LENGTH
        && text.startsWith(CommentElement.TOKEN_END, endTokenBegin);
  }

  private static CheckError createError(
      final String text, final int begin, final int nextBegin, final int commentEnd) {
    final int searchEnd = nextBegin >= 0 ? nextBegin : commentEnd;
    final int malformedEnd = text.indexOf(MALFORMED_END, begin + START_LENGTH);
    if (malformedEnd >= 0 && malformedEnd + MALFORMED_END.length() <= searchEnd) {
      return new CheckError(
          ALGORITHM_NUMBER,
          begin,
          malformedEnd + MALFORMED_END.length(),
          List.of(
              new Replacement(
                  text.substring(begin, malformedEnd) + CommentElement.TOKEN_END, false)));
    }
    final int contentBegin = skipSpaces(text, begin + START_LENGTH);
    if (nextBegin < 0) {
      return new CheckError(
          ALGORITHM_NUMBER, begin, contentBegin, List.of(new Replacement("", false)));
    }
    final int end = nextBegin + START_LENGTH;
    return new CheckError(
        ALGORITHM_NUMBER, begin, end, createReplacements(text, begin, nextBegin, contentBegin));
  }

  private static List<Replacement> createReplacements(
      final String text, final int begin, final int nextBegin, final int contentBegin) {
    final int end = nextBegin + START_LENGTH;
    final int endBeforeNext = skipBackSeparators(text, nextBegin);
    final int lineEnd = findLineEnd(text, begin);
    final List<Replacement> replacements = new ArrayList<>();
    if (lineEnd < endBeforeNext && lineEnd > begin + START_LENGTH) {
      replacements.add(
          new Replacement(
              text.substring(begin, lineEnd)
                  + CommentElement.TOKEN_END
                  + text.substring(lineEnd, end),
              false));
    }
    replacements.add(
        new Replacement(
            text.substring(begin, endBeforeNext)
                + CommentElement.TOKEN_END
                + text.substring(endBeforeNext, end),
            false));
    replacements.add(new Replacement(text.substring(begin, nextBegin), false));
    replacements.add(new Replacement(text.substring(contentBegin, end), false));
    return replacements;
  }

  private static int skipSpaces(final String text, final int from) {
    int index = from;
    while (index < text.length() && text.charAt(index) == ' ') {
      index++;
    }
    return index;
  }

  private static int skipBackSeparators(final String text, final int from) {
    int index = from;
    while (index > 0 && (text.charAt(index - 1) == '\n' || text.charAt(index - 1) == ' ')) {
      index--;
    }
    return index;
  }

  private static int findLineEnd(final String text, final int from) {
    final int newLine = text.indexOf('\n', from);
    return newLine < 0 ? text.length() : newLine;
  }
}
