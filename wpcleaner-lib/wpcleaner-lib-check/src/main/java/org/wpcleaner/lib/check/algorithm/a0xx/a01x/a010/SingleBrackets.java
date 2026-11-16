package org.wpcleaner.lib.check.algorithm.a0xx.a01x.a010;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.wpcleaner.api.analysis.Element;
import org.wpcleaner.api.analysis.PageAnalysis;
import org.wpcleaner.lib.check.algorithm.TextExclusions;

record SingleBrackets(String text, TextExclusions exclusions, Map<Integer, Integer> skippedEnds) {

  static final char OPEN = '[';
  static final char CLOSE = ']';

  SingleBrackets {
    skippedEnds = Map.copyOf(skippedEnds);
  }

  static SingleBrackets of(final PageAnalysis analysis, final TextExclusions exclusions) {
    return new SingleBrackets(
        analysis.getText(),
        exclusions,
        Stream.<List<? extends Element>>of(
                analysis.getTemplates(),
                analysis.getFunctions(),
                analysis.getParameters(),
                analysis.getTags())
            .<Element>flatMap(List::stream)
            .collect(Collectors.toMap(Element::begin, Element::end, Math::max)));
  }

  static int skipForward(final String text, final int from, final char character) {
    int index = from;
    while (index < text.length() && text.charAt(index) == character) {
      index++;
    }
    return index;
  }

  static int skipBackward(final String text, final int from, final char character) {
    int index = from;
    while (index > 0 && text.charAt(index - 1) == character) {
      index--;
    }
    return index;
  }

  String removeUnpaired(final int begin, final int end) {
    final Deque<Integer> opened = new ArrayDeque<>();
    final Set<Integer> unpaired = new HashSet<>();
    int index = begin;
    while (index < end) {
      final int tokenEnd = skippedEnds.getOrDefault(index, index + 1);
      if (tokenEnd == index + 1 && isBracket(index) && !exclusions.contains(index)) {
        if (text.charAt(index) == OPEN) {
          opened.push(index);
        } else if (opened.isEmpty()) {
          unpaired.add(index);
        } else {
          opened.pop();
        }
      }
      index = tokenEnd;
    }
    unpaired.addAll(opened);
    return IntStream.range(begin, end)
        .filter(position -> !unpaired.contains(position))
        .mapToObj(position -> String.valueOf(text.charAt(position)))
        .collect(Collectors.joining());
  }

  private boolean isBracket(final int index) {
    return text.charAt(index) == OPEN || text.charAt(index) == CLOSE;
  }
}
