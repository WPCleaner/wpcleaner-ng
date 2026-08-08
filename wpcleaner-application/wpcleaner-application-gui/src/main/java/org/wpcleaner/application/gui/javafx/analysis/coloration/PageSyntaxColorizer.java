package org.wpcleaner.application.gui.javafx.analysis.coloration;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import org.springframework.stereotype.Service;
import org.wpcleaner.api.analysis.PageAnalysis;
import org.wpcleaner.application.gui.javafx.core.style.JavaFxStylePropertiesRegistry;

@Service
public class PageSyntaxColorizer {

  private final List<PageSyntaxRule> rules;
  private final JavaFxStylePropertiesRegistry styleRegistry;

  public PageSyntaxColorizer(
      final List<PageSyntaxRule> rules, final JavaFxStylePropertiesRegistry styleRegistry) {
    this.rules = List.copyOf(rules);
    this.styleRegistry = styleRegistry;
  }

  public StyleSpans<String> computeStyleSpans(final String text) {
    final StyleSpansBuilder<String> spansBuilder = new StyleSpansBuilder<>();
    if (text.isEmpty()) {
      spansBuilder.add("", 0);
      return spansBuilder.create();
    }

    final PageAnalysis pageAnalysis = new PageAnalysis("", text);

    final List<StyledRange> ranges = new ArrayList<>();
    for (final PageSyntaxRule rule : rules) {
      final String style = styleRegistry.getStyle(rule.getStyleName());
      for (final PageSyntaxRule.RuleRange range : rule.getRanges(pageAnalysis)) {
        ranges.add(new StyledRange(range.begin(), range.end(), style));
      }
    }
    Collections.sort(ranges);
    deduplicateStyledRanges(ranges, 0);

    int lastKwEnd = 0;
    for (final StyledRange range : ranges) {
      final int start = range.begin();
      final int end = range.end();

      if (start < lastKwEnd) {
        continue;
      }

      if (start > lastKwEnd) {
        spansBuilder.add("", start - lastKwEnd);
      }
      spansBuilder.add(range.styleName(), end - start);
      lastKwEnd = end;
    }

    if (lastKwEnd < text.length()) {
      spansBuilder.add("", text.length() - lastKwEnd);
    }
    return spansBuilder.create();
  }

  private void deduplicateStyledRanges(final List<StyledRange> ranges, final int startAt) {
    if (startAt > ranges.size() - 2) {
      return;
    }
    final int currentEnd = ranges.get(startAt).end();
    final int nextBegin = ranges.get(startAt + 1).begin();
    if (currentEnd > nextBegin) {
      final int nextEnd = ranges.get(startAt + 1).end();
      final String styleName = ranges.get(startAt).styleName();
      if (currentEnd > nextEnd) {
        ranges.add(startAt + 2, new StyledRange(nextEnd, currentEnd, styleName));
      }
      final int currentBegin = ranges.get(startAt).begin();
      ranges.set(startAt, new StyledRange(currentBegin, nextBegin, styleName));
    }
    deduplicateStyledRanges(ranges, startAt + 1);
  }

  private record StyledRange(int begin, int end, String styleName)
      implements Comparable<StyledRange> {

    @Override
    public int compareTo(final StyledRange other) {
      return Comparator.comparingInt(StyledRange::begin)
          .thenComparingInt(StyledRange::end)
          .thenComparing(StyledRange::styleName)
          .compare(this, other);
    }

    @Override
    public boolean equals(final Object obj) {
      if (this == obj) {
        return true;
      }
      if (!(obj instanceof StyledRange(int otherBegin, int otherEnd, String otherName))) {
        return false;
      }
      return this.begin == otherBegin
          && this.end == otherEnd
          && Objects.equals(this.styleName, otherName);
    }

    @Override
    public int hashCode() {
      return Objects.hash(this.begin, this.end, this.styleName);
    }
  }
}
