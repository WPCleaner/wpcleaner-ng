package org.wpcleaner.application.gui.javafx.analysis.coloration;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import org.springframework.stereotype.Service;
import org.wpcleaner.application.gui.javafx.core.style.JavaFxStylePropertiesRegistry;

@Service
public class PageSyntaxColorizer {

  private final List<PageSyntaxRule> rules;
  private final JavaFxStylePropertiesRegistry styleRegistry;
  private final Pattern combinedPattern;

  public PageSyntaxColorizer(
      final List<PageSyntaxRule> rules, final JavaFxStylePropertiesRegistry styleRegistry) {
    this.rules = List.copyOf(rules);
    this.styleRegistry = styleRegistry;

    final StringBuilder patternBuilder = new StringBuilder();
    for (final PageSyntaxRule rule : this.rules) {
      if (!patternBuilder.isEmpty()) {
        patternBuilder.append('|');
      }
      patternBuilder
          .append("(?<")
          .append(rule.getGroupName())
          .append('>')
          .append(rule.getPattern().pattern())
          .append(')');
    }

    if (patternBuilder.isEmpty()) {
      this.combinedPattern = Pattern.compile("$^");
    } else {
      this.combinedPattern = Pattern.compile(patternBuilder.toString());
    }
  }

  public StyleSpans<String> computeStyleSpans(final String text) {
    final StyleSpansBuilder<String> spansBuilder = new StyleSpansBuilder<>();
    if (text.isEmpty()) {
      spansBuilder.add("", 0);
      return spansBuilder.create();
    }

    final Matcher matcher = combinedPattern.matcher(text);
    int lastKwEnd = 0;
    while (matcher.find()) {
      String style = "";
      for (final PageSyntaxRule rule : rules) {
        if (matcher.group(rule.getGroupName()) != null) {
          style = styleRegistry.getStyle(rule.getStyleName());
          break;
        }
      }

      final int start = matcher.start();
      if (start > lastKwEnd) {
        spansBuilder.add("", start - lastKwEnd);
      }
      spansBuilder.add(style, matcher.end() - start);
      lastKwEnd = matcher.end();
    }

    if (lastKwEnd < text.length()) {
      spansBuilder.add("", text.length() - lastKwEnd);
    }
    return spansBuilder.create();
  }
}
