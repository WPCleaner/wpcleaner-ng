package org.wpcleaner.api.analysis.comparison;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.wpcleaner.api.wiki.definition.WikiDefinition;

final class ComparisonReport {

  private static final int MAX_DETAILS_PER_TYPE = 20;
  private static final int INITIAL_CAPACITY = 100_000;

  private final List<PageResult> pages = new ArrayList<>();
  private final List<String> reconstructionFailures = new ArrayList<>();

  void addPage(
      final WikiDefinition wiki,
      final String title,
      final int revisionId,
      final List<Difference> differences) {
    pages.add(new PageResult(wiki, title, revisionId, List.copyOf(differences)));
  }

  void addReconstructionFailure(final WikiDefinition wiki, final String title) {
    reconstructionFailures.add(wiki.code() + ":" + title);
  }

  int getPageCount() {
    return pages.size();
  }

  List<String> getReconstructionFailures() {
    return reconstructionFailures;
  }

  String toMarkdown() {
    final StringBuilder markdown = new StringBuilder(INITIAL_CAPACITY);
    markdown.append(
        """
        # Page analysis compared with MediaWiki

        ## Summary

        | Element | Only MediaWiki | Only WPCleaner | Pages |
        |---|---:|---:|---:|
        """);
    final Map<String, TypeCount> counts = new TreeMap<>();
    for (final PageResult page : pages) {
      page.differences().stream()
          .map(Difference::type)
          .distinct()
          .forEach(type -> counts.computeIfAbsent(type, _ -> new TypeCount()).addPage());
      page.differences()
          .forEach(
              difference ->
                  counts
                      .computeIfAbsent(difference.type(), _ -> new TypeCount())
                      .addDifference(difference.side()));
    }
    counts.forEach(
        (type, count) ->
            markdown.append(
                "| %s | %d | %d | %d |%n"
                    .formatted(type, count.onlyMediaWiki, count.onlyWpCleaner, count.pages)));
    markdown.append("%n%d pages compared.%n".formatted(pages.size()));
    if (!reconstructionFailures.isEmpty()) {
      markdown.append("\nParse tree not matching the text: ").append(reconstructionFailures);
    }
    for (final PageResult page : pages) {
      appendPage(markdown, page);
    }
    return markdown.toString();
  }

  private static void appendPage(final StringBuilder markdown, final PageResult page) {
    markdown.append(
        "\n## %s: %s (revision %d)\n\n"
            .formatted(page.wiki().code(), page.title(), page.revisionId()));
    if (page.differences().isEmpty()) {
      markdown.append("No difference.\n");
      return;
    }
    final Map<String, List<Difference>> byType = new TreeMap<>();
    page.differences()
        .forEach(
            difference ->
                byType.computeIfAbsent(difference.type(), _ -> new ArrayList<>()).add(difference));
    byType.forEach(
        (type, differences) -> {
          markdown.append("- **%s** (%d)\n".formatted(type, differences.size()));
          differences.stream()
              .limit(MAX_DETAILS_PER_TYPE)
              .forEach(
                  difference ->
                      markdown.append(
                          "  - %s: %s\n"
                              .formatted(
                                  difference.side() == Difference.Side.ONLY_MEDIAWIKI
                                      ? "only MediaWiki"
                                      : "only WPCleaner",
                                  difference.detail())));
        });
  }

  private record PageResult(
      WikiDefinition wiki, String title, int revisionId, List<Difference> differences) {}

  private static final class TypeCount {

    private int onlyMediaWiki;
    private int onlyWpCleaner;
    private int pages;

    private void addPage() {
      pages++;
    }

    private void addDifference(final Difference.Side side) {
      if (side == Difference.Side.ONLY_MEDIAWIKI) {
        onlyMediaWiki++;
      } else {
        onlyWpCleaner++;
      }
    }
  }
}
