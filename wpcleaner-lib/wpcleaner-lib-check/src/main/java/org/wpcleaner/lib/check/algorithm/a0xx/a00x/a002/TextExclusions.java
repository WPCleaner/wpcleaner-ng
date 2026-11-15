package org.wpcleaner.lib.check.algorithm.a0xx.a00x.a002;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.stream.Stream;
import org.wpcleaner.api.analysis.PageAnalysis;
import org.wpcleaner.api.analysis.tag.CompleteTagElement;

record TextExclusions(List<Area> areas) {

  TextExclusions {
    areas = List.copyOf(areas);
  }

  static TextExclusions of(final PageAnalysis analysis) {
    return new TextExclusions(
        Stream.concat(
                analysis.getComments().stream()
                    .map(comment -> new Area(comment.begin(), comment.end())),
                analysis.getCompleteTags().stream()
                    .filter(TextExclusions::hasNonWikiContent)
                    .map(Area::inside))
            .toList());
  }

  boolean contains(final int index) {
    return areas.stream().anyMatch(area -> area.begin() <= index && index < area.end());
  }

  private static boolean hasNonWikiContent(final CompleteTagElement tag) {
    return tag.startTag().type().filter(type -> !type.hasWikiContent()).isPresent();
  }

  record Area(int begin, int end) {

    static Area inside(final CompleteTagElement tag) {
      final int begin = tag.startTag().end();
      final int end = tag.endTag().begin();
      return new Area(begin, end);
    }
  }
}
