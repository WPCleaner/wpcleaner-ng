package org.wpcleaner.api.analysis.wiki;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Comparator;
import java.util.List;

final class SortedAreas {

  private final List<Area> areas;
  private final int[] maxEnds;

  SortedAreas(final List<Area> unsortedAreas) {
    this.areas = unsortedAreas.stream().sorted(Comparator.comparingInt(Area::begin)).toList();
    this.maxEnds = new int[areas.size()];
    int maxEnd = Integer.MIN_VALUE;
    for (int area = 0; area < areas.size(); area++) {
      maxEnd = Math.max(maxEnd, areas.get(area).end());
      maxEnds[area] = maxEnd;
    }
  }

  boolean overlaps(final int begin, final int end) {
    int low = 0;
    int high = areas.size();
    while (low < high) {
      final int middle = (low + high) >>> 1;
      if (areas.get(middle).begin() < end) {
        low = middle + 1;
      } else {
        high = middle;
      }
    }
    return low > 0 && maxEnds[low - 1] > begin;
  }
}
