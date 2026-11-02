package org.wpcleaner.api.analysis.performance;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.ToLongFunction;

final class PerformanceReport {

  private static final double NANOS_PER_MILLI = 1_000_000.0;
  private static final int INITIAL_CAPACITY = 10_000;
  private static final List<String> ELEMENT_TYPES =
      List.of(
          "templates",
          "comments",
          "tags",
          "links",
          "references",
          "tables",
          "cells",
          "titles",
          "list items",
          "images");

  private final List<PageMeasure> measures = new ArrayList<>();
  private final int warmupRuns;
  private final int measuredRuns;

  PerformanceReport(final int warmupRuns, final int measuredRuns) {
    this.warmupRuns = warmupRuns;
    this.measuredRuns = measuredRuns;
  }

  void add(final PageMeasure measure) {
    measures.add(measure);
  }

  List<PageMeasure> getMeasures() {
    return measures;
  }

  String toMarkdown() {
    final StringBuilder markdown = new StringBuilder(INITIAL_CAPACITY);
    final Runtime runtime = Runtime.getRuntime();
    markdown
        .append("# Page analysis performance\n\n")
        .append(
            "Java %s, %d processors, max heap %d MB. %d warm-up runs, %d measured runs per page.%n%n"
                .formatted(
                    Runtime.version(),
                    runtime.availableProcessors(),
                    runtime.maxMemory() / 1024 / 1024,
                    warmupRuns,
                    measuredRuns))
        .append(
            """
            ## Time (median of measured runs, in ms)

            | Page | Characters | Preprocessor | Tags | Wiki | Total | Min total | Characters/ms |
            |---|---:|---:|---:|---:|---:|---:|---:|
            """);
    for (final PageMeasure measure : measures) {
      final double total = median(measure, PageMeasure.Timing::totalNanos);
      markdown.append(
          "| %s | %,d | %s | %s | %s | %s | %s | %,.0f |%n"
              .formatted(
                  measure.label(),
                  measure.length(),
                  format(median(measure, PageMeasure.Timing::preprocessorNanos)),
                  format(median(measure, PageMeasure.Timing::tagsNanos)),
                  format(median(measure, PageMeasure.Timing::wikiNanos)),
                  format(total),
                  format(minimum(measure)),
                  total > 0 ? measure.length() / (total / NANOS_PER_MILLI) : 0));
    }
    markdown
        .append("\n## Complexity (number of elements)\n\n| Page |")
        .append(String.join(" | ", ELEMENT_TYPES))
        .append(" |\n|---|")
        .append("---:|".repeat(ELEMENT_TYPES.size()))
        .append('\n');
    for (final PageMeasure measure : measures) {
      markdown.append("| ").append(measure.label()).append(" |");
      ELEMENT_TYPES.forEach(
          type ->
              markdown.append(" %,d |".formatted(measure.elementCounts().getOrDefault(type, 0))));
      markdown.append('\n');
    }
    return markdown.toString();
  }

  private static double median(
      final PageMeasure measure, final ToLongFunction<PageMeasure.Timing> value) {
    final long[] values = measure.timings().stream().mapToLong(value).sorted().toArray();
    if (values.length == 0) {
      return 0;
    }
    final int middle = values.length / 2;
    return values.length % 2 == 1 ? values[middle] : (values[middle - 1] + values[middle]) / 2.0;
  }

  private static double minimum(final PageMeasure measure) {
    return measure.timings().stream().mapToLong(PageMeasure.Timing::totalNanos).min().orElse(0);
  }

  private static String format(final double nanos) {
    return String.format(Locale.ROOT, "%.1f", nanos / NANOS_PER_MILLI);
  }
}
