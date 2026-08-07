package org.wpcleaner.application.gui.javafx.analysis.coloration;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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

  private record StyledRange(int begin, int end, String styleName)
      implements Comparable<StyledRange> {

    @Override
    public int compareTo(final StyledRange other) {
      final int beginCompare = Integer.compare(this.begin, other.begin);
      if (beginCompare != 0) {
        return beginCompare;
      }
      final int endCompare = Integer.compare(this.end, other.end);
      if (endCompare != 0) {
        return endCompare;
      }
      return this.styleName.compareTo(other.styleName);
    }

    @Override
    public boolean equals(final Object obj) {
      if (this == obj) {
        return true;
      }
      if (!(obj instanceof StyledRange other)) {
        return false;
      }
      return this.begin == other.begin
          && this.end == other.end
          && java.util.Objects.equals(this.styleName, other.styleName);
    }

    @Override
    public int hashCode() {
      return java.util.Objects.hash(this.begin, this.end, this.styleName);
    }
  }
}
