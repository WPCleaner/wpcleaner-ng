package org.wpcleaner.lib.check.algorithm.a0xx.a00x.a008;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;
import org.wpcleaner.api.analysis.Element;
import org.wpcleaner.api.analysis.PageAnalysis;
import org.wpcleaner.api.analysis.comment.CommentElement;
import org.wpcleaner.api.analysis.tag.CompleteTagElement;
import org.wpcleaner.api.analysis.tag.HtmlTagType;
import org.wpcleaner.api.analysis.tag.WikiTagType;
import org.wpcleaner.api.analysis.title.TitleElement;
import org.wpcleaner.lib.check.CheckAlgorithm;
import org.wpcleaner.lib.check.CheckError;

@Component
public class CheckAlgorithm008 implements CheckAlgorithm {

  private static final int ALGORITHM_NUMBER = 8;
  private static final char NEW_LINE = '\n';
  private static final String TRAILING_WHITESPACE = " \t";
  private static final Set<String> IGNORED_TAGS =
      Set.of(
          HtmlTagType.CODE.getNormalizedName(),
          WikiTagType.CE.getNormalizedName(),
          WikiTagType.CHEM.getNormalizedName(),
          WikiTagType.MATH.getNormalizedName(),
          WikiTagType.NOWIKI.getNormalizedName(),
          WikiTagType.PRE.getNormalizedName(),
          WikiTagType.SCORE.getNormalizedName(),
          WikiTagType.SOURCE.getNormalizedName(),
          WikiTagType.SYNTAXHIGHLIGHT.getNormalizedName());

  @Override
  public int number() {
    return ALGORITHM_NUMBER;
  }

  @Override
  public List<CheckError> analyze(final PageAnalysis analysis) {
    final String text = analysis.getText();
    final List<Element> ignoredAreas = findIgnoredAreas(analysis);
    final List<Element> argumentContainers = findArgumentContainers(analysis);
    final Set<Integer> titleBegins =
        analysis.getTitles().stream()
            .filter(title -> !hasShorterClosing(text, title))
            .map(TitleElement::begin)
            .collect(Collectors.toSet());
    final List<CheckError> errors = new ArrayList<>();
    int lineBegin = 0;
    while (lineBegin < text.length()) {
      final int lineEnd = findLineEnd(text, lineBegin);
      if (text.charAt(lineBegin) == TitleReplacements.EQUAL
          && !titleBegins.contains(lineBegin)
          && !isInsideOne(ignoredAreas, lineBegin)) {
        final boolean insideArgument = isInsideOne(argumentContainers, lineBegin);
        createError(text, analysis.getComments(), lineBegin, lineEnd)
            .map(error -> insideArgument ? withoutReplacements(error) : error)
            .ifPresent(errors::add);
      }
      lineBegin = lineEnd + 1;
    }
    return errors;
  }

  private static boolean hasShorterClosing(final String text, final TitleElement title) {
    return TitleReplacements.countEquals(text, title.begin()) > title.level()
        && !title.title().chars().allMatch(character -> character == TitleReplacements.EQUAL);
  }

  private static List<Element> findArgumentContainers(final PageAnalysis analysis) {
    return Stream.<List<? extends Element>>of(
            analysis.getTemplates(), analysis.getFunctions(), analysis.getParameters())
        .<Element>flatMap(List::stream)
        .toList();
  }

  private static CheckError withoutReplacements(final CheckError error) {
    return new CheckError(error.algorithm(), error.begin(), error.end(), List.of());
  }

  private static List<Element> findIgnoredAreas(final PageAnalysis analysis) {
    return Stream.<Element>concat(
            analysis.getComments().stream(),
            analysis.getCompleteTags().stream().filter(CheckAlgorithm008::isIgnoredTag))
        .toList();
  }

  private static boolean isIgnoredTag(final CompleteTagElement tag) {
    return IGNORED_TAGS.contains(tag.startTag().normalizedName());
  }

  private static boolean isInsideOne(final List<Element> areas, final int index) {
    return areas.stream().anyMatch(area -> area.begin() <= index && index < area.end());
  }

  private static Optional<CheckError> createError(
      final String text,
      final List<CommentElement> comments,
      final int lineBegin,
      final int lineEnd) {
    final int titleEnd = findTitleEnd(text, comments, lineBegin, lineEnd);
    final String line = text.substring(lineBegin, titleEnd);
    final int equalsCount = TitleReplacements.countEquals(line, 0);
    if (equalsCount < line.length() && countEqualsBefore(line, line.length()) >= equalsCount) {
      return Optional.empty();
    }
    return Optional.of(
        new CheckError(
            ALGORITHM_NUMBER, lineBegin, titleEnd, TitleReplacements.create(line, equalsCount)));
  }

  private static int findTitleEnd(
      final String text,
      final List<CommentElement> comments,
      final int lineBegin,
      final int lineEnd) {
    int titleEnd = skipWhitespaceBefore(text, lineBegin, lineEnd);
    boolean commentFound = true;
    while (commentFound) {
      commentFound = false;
      for (final CommentElement comment : comments) {
        if (comment.begin() < titleEnd && comment.end() >= titleEnd) {
          titleEnd = skipWhitespaceBefore(text, lineBegin, comment.begin());
          commentFound = true;
        }
      }
    }
    return titleEnd;
  }

  private static int countEqualsBefore(final String line, final int end) {
    int index = end;
    while (index > 0 && line.charAt(index - 1) == TitleReplacements.EQUAL) {
      index--;
    }
    return end - index;
  }

  private static int skipWhitespaceBefore(final String text, final int begin, final int end) {
    int index = end;
    while (index > begin && TRAILING_WHITESPACE.indexOf(text.charAt(index - 1)) >= 0) {
      index--;
    }
    return index;
  }

  private static int findLineEnd(final String text, final int from) {
    final int newLine = text.indexOf(NEW_LINE, from);
    return newLine < 0 ? text.length() : newLine;
  }
}
