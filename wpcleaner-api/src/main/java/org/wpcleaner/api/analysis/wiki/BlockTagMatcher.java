package org.wpcleaner.api.analysis.wiki;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import org.wpcleaner.api.analysis.tag.TagElement;
import org.wpcleaner.api.analysis.tag.TagKind;

final class BlockTagMatcher {

  private static final Set<String> HEADINGS = Set.of("h1", "h2", "h3", "h4", "h5", "h6");
  private static final Set<String> OPENING_START_TAGS =
      union(HEADINGS, Set.of("table", "tr", "p", "ul", "ol", "dl", "li"));
  private static final Set<String> OPENING_END_TAGS = Set.of("tr", "td", "th");
  private static final Set<String> CLOSING_START_TAGS =
      Set.of("td", "th", "blockquote", "div", "hr", "center");
  private static final Set<String> CLOSING_END_TAGS =
      union(
          HEADINGS,
          Set.of("table", "blockquote", "div", "pre", "p", "li", "ul", "ol", "dl", "center"));
  private static final String PREFORMATTED_TAG = "pre";

  private final NavigableMap<Integer, TagElement> tagsByBegin;

  BlockTagMatcher(final List<TagElement> tags) {
    this.tagsByBegin = new TreeMap<>();
    tags.forEach(tag -> tagsByBegin.put(tag.begin(), tag));
  }

  Match match(final int begin, final int end) {
    boolean opening = false;
    boolean closing = false;
    for (final Map.Entry<Integer, TagElement> entry : tagsByBegin.subMap(begin, end).entrySet()) {
      final TagElement tag = entry.getValue();
      final String name = tag.normalizedName();
      final boolean endTag = tag.kind() == TagKind.END;
      opening |= endTag ? OPENING_END_TAGS.contains(name) : OPENING_START_TAGS.contains(name);
      closing |=
          PREFORMATTED_TAG.equals(name)
              || (endTag ? CLOSING_END_TAGS.contains(name) : CLOSING_START_TAGS.contains(name));
    }
    if (closing) {
      return Match.CLOSING;
    }
    return opening ? Match.OPENING : Match.NONE;
  }

  private static Set<String> union(final Set<String> first, final Set<String> second) {
    final List<String> values = new ArrayList<>(first);
    values.addAll(second);
    return Set.copyOf(values);
  }

  enum Match {
    NONE,
    OPENING,
    CLOSING
  }
}
