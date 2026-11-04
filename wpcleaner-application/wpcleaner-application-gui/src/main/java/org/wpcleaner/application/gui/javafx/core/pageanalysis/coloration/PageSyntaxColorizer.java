package org.wpcleaner.application.gui.javafx.core.pageanalysis.coloration;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.SortedSet;
import java.util.TreeSet;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import org.springframework.stereotype.Service;
import org.wpcleaner.api.analysis.Element;
import org.wpcleaner.api.analysis.PageAnalysis;
import org.wpcleaner.application.gui.core.style.PageAnalysisStylePropertiesInitializer;
import org.wpcleaner.application.gui.javafx.core.style.JavaFxStylePropertiesRegistry;

@Service
public class PageSyntaxColorizer {

  private static final String NO_STYLE = "";
  private static final Comparator<StyledRange> INNERMOST_FIRST =
      Comparator.comparingInt(StyledRange::begin)
          .reversed()
          .thenComparingInt(StyledRange::end)
          .thenComparing(StyledRange::styleName);

  private final List<PageSyntaxRule> rules;
  private final JavaFxStylePropertiesRegistry styleRegistry;

  public PageSyntaxColorizer(
      final List<PageSyntaxRule> rules, final JavaFxStylePropertiesRegistry styleRegistry) {
    this.rules = List.copyOf(rules);
    this.styleRegistry = styleRegistry;
  }

  public StyleSpans<String> computeStyleSpans(final PageAnalysis pageAnalysis) {
    final String text = pageAnalysis.getText();
    final SpansBuilder spansBuilder = new SpansBuilder();
    if (text.isEmpty()) {
      return spansBuilder.create();
    }
    final List<StyledRange> ranges = collectRanges(pageAnalysis, text.length());
    final SortedSet<Integer> boundaries = new TreeSet<>(List.of(0, text.length()));
    ranges.forEach(
        range -> {
          boundaries.add(range.begin());
          boundaries.add(range.end());
        });
    final Queue<StyledRange> activeRanges = new PriorityQueue<>(INNERMOST_FIRST);
    int nextRange = 0;
    int segmentBegin = 0;
    for (final int segmentEnd : boundaries.tailSet(1)) {
      while (nextRange < ranges.size() && ranges.get(nextRange).begin() <= segmentBegin) {
        activeRanges.add(ranges.get(nextRange));
        nextRange++;
      }
      while (!activeRanges.isEmpty() && activeRanges.peek().end() <= segmentBegin) {
        activeRanges.poll();
      }
      final StyledRange innermostRange = activeRanges.peek();
      spansBuilder.add(
          innermostRange == null ? NO_STYLE : innermostRange.styleName(),
          segmentEnd - segmentBegin);
      segmentBegin = segmentEnd;
    }
    return spansBuilder.create();
  }

  public StyleSpans<String> highlight(
      final StyleSpans<String> styleSpans, final List<? extends Element> elements) {
    final String highlightStyle =
        styleRegistry.getStyle(PageAnalysisStylePropertiesInitializer.CHECK_ERROR);
    final int length = styleSpans.length();
    final SpansBuilder spansBuilder = new SpansBuilder();
    int position = 0;
    for (final Element element :
        elements.stream().sorted(Comparator.comparingInt(Element::begin)).toList()) {
      final int begin = Math.max(position, element.begin());
      final int end = Math.min(length, element.end());
      if (begin < end) {
        spansBuilder.add(NO_STYLE, begin - position);
        spansBuilder.add(highlightStyle, end - begin);
        position = end;
      }
    }
    spansBuilder.add(NO_STYLE, length - position);
    return styleSpans.overlay(spansBuilder.create(), String::concat);
  }

  private List<StyledRange> collectRanges(final PageAnalysis pageAnalysis, final int length) {
    final List<StyledRange> ranges = new ArrayList<>();
    for (final PageSyntaxRule rule : rules) {
      final String style = styleRegistry.getStyle(rule.getStyleName());
      for (final PageSyntaxRule.RuleRange range : rule.getRanges(pageAnalysis)) {
        final int begin = Math.max(0, range.begin());
        final int end = Math.min(length, range.end());
        if (begin < end) {
          ranges.add(new StyledRange(begin, end, style));
        }
      }
    }
    ranges.sort(Comparator.comparingInt(StyledRange::begin));
    return ranges;
  }

  private record StyledRange(int begin, int end, String styleName) {}

  private static final class SpansBuilder {

    private final StyleSpansBuilder<String> builder = new StyleSpansBuilder<>();
    private String pendingStyle = NO_STYLE;
    private int pendingLength;
    private boolean spanAdded;

    private void add(final String style, final int length) {
      if (!style.equals(pendingStyle)) {
        flush();
        pendingStyle = style;
      }
      pendingLength += length;
    }

    private StyleSpans<String> create() {
      flush();
      if (!spanAdded) {
        builder.add(NO_STYLE, 0);
      }
      return builder.create();
    }

    private void flush() {
      if (pendingLength > 0) {
        builder.add(pendingStyle, pendingLength);
        pendingLength = 0;
        spanAdded = true;
      }
    }
  }
}
