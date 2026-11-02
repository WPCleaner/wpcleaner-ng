package org.wpcleaner.api.analysis.performance;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Map;

record PageMeasure(
    String label, int length, Map<String, Integer> elementCounts, List<Timing> timings) {

  PageMeasure {
    elementCounts = Map.copyOf(elementCounts);
    timings = List.copyOf(timings);
  }

  record Timing(long preprocessorNanos, long tagsNanos, long wikiNanos) {
    long totalNanos() {
      return preprocessorNanos + tagsNanos + wikiNanos;
    }
  }
}
