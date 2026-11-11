package org.wpcleaner.lib.check.comparison;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.wpcleaner.api.wiki.definition.WikiDefinition;

final class CheckWikiReport {

  private static final int INITIAL_CAPACITY = 100_000;

  private final Map<String, AlgorithmResult> algorithms = new TreeMap<>();

  void addInactiveAlgorithm(final WikiDefinition wiki, final int algorithm) {
    getAlgorithm(wiki, algorithm).active = false;
  }

  void addListedPages(final WikiDefinition wiki, final int algorithm, final int count) {
    getAlgorithm(wiki, algorithm).listedPages = count;
  }

  void addPage(final WikiDefinition wiki, final int algorithm, final PageResult page) {
    getAlgorithm(wiki, algorithm).pages.add(page);
  }

  int getPageCount() {
    return algorithms.values().stream().mapToInt(algorithm -> algorithm.pages.size()).sum();
  }

  private AlgorithmResult getAlgorithm(final WikiDefinition wiki, final int algorithm) {
    return algorithms.computeIfAbsent(
        "%s:%03d".formatted(wiki.code(), algorithm), _ -> new AlgorithmResult(wiki, algorithm));
  }

  String toMarkdown() {
    final StringBuilder markdown = new StringBuilder(INITIAL_CAPACITY);
    markdown.append(
        """
        # Detected errors compared with Check Wiki

        ## Summary

        | Wiki | Error | Listed | Detected | Not detected | Whitelisted | Missing |
        |---|---:|---:|---:|---:|---:|---:|
        """);
    algorithms.values().forEach(algorithm -> appendSummary(markdown, algorithm));
    algorithms.values().stream()
        .filter(algorithm -> algorithm.count(Status.NOT_DETECTED) > 0)
        .forEach(algorithm -> appendNotDetected(markdown, algorithm));
    return markdown.toString();
  }

  private static void appendSummary(final StringBuilder markdown, final AlgorithmResult algorithm) {
    if (!algorithm.active) {
      markdown.append(
          "| %s | %03d | inactive | | | | |%n".formatted(algorithm.wiki.code(), algorithm.number));
      return;
    }
    markdown.append(
        "| %s | %03d | %d | %d | %d | %d | %d |%n"
            .formatted(
                algorithm.wiki.code(),
                algorithm.number,
                algorithm.listedPages,
                algorithm.count(Status.DETECTED),
                algorithm.count(Status.NOT_DETECTED),
                algorithm.count(Status.WHITELISTED),
                algorithm.count(Status.MISSING)));
  }

  private static void appendNotDetected(
      final StringBuilder markdown, final AlgorithmResult algorithm) {
    markdown.append(
        "%n## %s: error %03d not detected%n%n".formatted(algorithm.wiki.code(), algorithm.number));
    algorithm.pages.stream()
        .filter(page -> page.status() == Status.NOT_DETECTED)
        .forEach(
            page ->
                markdown.append("- %s (revision %d)%n".formatted(page.title(), page.revisionId())));
  }

  enum Status {
    DETECTED,
    NOT_DETECTED,
    WHITELISTED,
    MISSING
  }

  record PageResult(String title, int revisionId, Status status, int errorCount) {}

  private static final class AlgorithmResult {

    private final WikiDefinition wiki;
    private final int number;
    private final List<PageResult> pages = new ArrayList<>();
    private boolean active = true;
    private int listedPages;

    private AlgorithmResult(final WikiDefinition wiki, final int number) {
      this.wiki = wiki;
      this.number = number;
    }

    private long count(final Status status) {
      final Map<Status, Long> counts = new EnumMap<>(Status.class);
      pages.forEach(page -> counts.merge(page.status(), 1L, Long::sum));
      return counts.getOrDefault(status, 0L);
    }
  }
}
