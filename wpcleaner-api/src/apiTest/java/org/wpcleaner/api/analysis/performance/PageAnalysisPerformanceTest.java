package org.wpcleaner.api.analysis.performance;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.wpcleaner.api.SpringBootTestConfig;
import org.wpcleaner.api.analysis.PageAnalysis;
import org.wpcleaner.api.analysis.PageAnalysisFactory;
import org.wpcleaner.api.analysis.table.TableElement;
import org.wpcleaner.api.analysis.tag.WikiTagType;
import org.wpcleaner.api.api.query.prop.revisions.ApiRevisions;
import org.wpcleaner.api.api.query.prop.revisions.Page;
import org.wpcleaner.api.api.query.prop.revisions.Revision;
import org.wpcleaner.api.api.query.prop.revisions.RevisionSlot;
import org.wpcleaner.api.api.query.prop.revisions.RevisionsParameters;
import org.wpcleaner.api.api.query.prop.revisions.RevisionsQuery;
import org.wpcleaner.api.hook.login.LoginHook;
import org.wpcleaner.api.hook.login.WPCleanerConfigurationLoader;
import org.wpcleaner.api.progress.ProgressTracker;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.api.wiki.definition.WikipediaDefinitions;

@SpringBootTest(classes = SpringBootTestConfig.class)
class PageAnalysisPerformanceTest {

  private static final String MAIN_SLOT = "main";
  private static final int WARMUP_RUNS = 2;
  private static final int MEASURED_RUNS = 5;
  private static final int MAXIMUM_PAGE_LENGTH = 2_000_000;
  private static final char NEW_LINE = '\n';
  private static final String SYNTHETIC_LABEL = "synthetic: en pages concatenated up to 2,000,000";
  private static final Path REPORT =
      Path.of("build", "reports", "page-analysis-performance", "report.md");
  private static final Map<WikiDefinition, List<String>> PAGES =
      Map.of(
          WikipediaDefinitions.FR,
          List.of(
              "Ellertshaar",
              "Victor Hugo",
              "Paris",
              "Liste et armorial des personnes admises aux Lignages de Bruxelles",
              "Guerre civile syrienne"),
          WikipediaDefinitions.EN,
          List.of(
              "Kulshan caldera",
              "Albert Einstein",
              "List of rodents",
              "Opinion polling for the next Italian general election"));

  @Autowired private ApiRevisions apiRevisions;
  @Autowired private LoginHook loginHook;
  @Autowired private PageAnalysisFactory pageAnalysisFactory;

  @DisplayName("Measure page analysis time on pages of different sizes and complexity")
  @Test
  void measurePageAnalysis() throws IOException {
    // GIVEN
    final PerformanceReport report = new PerformanceReport(WARMUP_RUNS, MEASURED_RUNS);

    // WHEN
    for (final WikiDefinition wiki : List.of(WikipediaDefinitions.FR, WikipediaDefinitions.EN)) {
      loginHook.executeHook(
          wiki, WPCleanerConfigurationLoader.GENERAL_USERNAME, new ProgressTracker(_ -> {}));
      final Map<String, String> contents = retrieveContents(wiki, PAGES.get(wiki));
      contents.forEach(
          (title, content) ->
              report.add(measure(wiki, "%s: %s".formatted(wiki.code(), title), content)));
      if (WikipediaDefinitions.EN.equals(wiki)) {
        report.add(measure(wiki, SYNTHETIC_LABEL, createSyntheticPage(contents.values())));
      }
    }
    Files.createDirectories(REPORT.getParent());
    Files.writeString(REPORT, report.toMarkdown());

    // THEN
    Assertions.assertThat(report.getMeasures()).as("measured pages").isNotEmpty();
  }

  private PageMeasure measure(final WikiDefinition wiki, final String label, final String text) {
    for (int run = 0; run < WARMUP_RUNS; run++) {
      analyze(wiki, text);
    }
    final List<PageMeasure.Timing> timings = new ArrayList<>();
    for (int run = 0; run < MEASURED_RUNS; run++) {
      timings.add(analyze(wiki, text));
    }
    return new PageMeasure(
        label,
        text.length(),
        countElements(pageAnalysisFactory.analysis(wiki, label, text)),
        timings);
  }

  private PageMeasure.Timing analyze(final WikiDefinition wiki, final String text) {
    final long start = System.nanoTime();
    final PageAnalysis analysis = pageAnalysisFactory.analysis(wiki, "Performance", text);
    analysis.getComments();
    final long afterPreprocessor = System.nanoTime();
    analysis.getTags();
    final long afterTags = System.nanoTime();
    analysis.getInternalLinks();
    final long afterWiki = System.nanoTime();
    return new PageMeasure.Timing(
        afterPreprocessor - start, afterTags - afterPreprocessor, afterWiki - afterTags);
  }

  private static Map<String, Integer> countElements(final PageAnalysis analysis) {
    final Map<String, Integer> counts = new LinkedHashMap<>();
    counts.put(
        "templates",
        analysis.getTemplates().size()
            + analysis.getFunctions().size()
            + analysis.getParameters().size());
    counts.put("comments", analysis.getComments().size());
    counts.put("tags", analysis.getTags().size());
    counts.put(
        "links",
        analysis.getInternalLinks().size()
            + analysis.getExternalLinks().size()
            + analysis.getInterwikiLinks().size()
            + analysis.getLanguageLinks().size()
            + analysis.getCategories().size());
    counts.put(
        "references",
        (int)
            analysis.getCompleteTags().stream()
                .filter(
                    tag ->
                        WikiTagType.REF.getNormalizedName().equals(tag.startTag().normalizedName()))
                .count());
    counts.put("tables", analysis.getTables().size());
    counts.put(
        "cells",
        analysis.getTables().stream()
            .map(TableElement::rows)
            .flatMap(List::stream)
            .mapToInt(row -> row.cells().size())
            .sum());
    counts.put("titles", analysis.getTitles().size());
    counts.put("list items", analysis.getListItems().size());
    counts.put(
        "images",
        analysis.getImages().size()
            + analysis.getGalleries().stream().mapToInt(gallery -> gallery.images().size()).sum());
    return counts;
  }

  private Map<String, String> retrieveContents(
      final WikiDefinition wiki, final List<String> titles) {
    final Map<String, String> contents = new LinkedHashMap<>();
    final List<Page> pages =
        apiRevisions.retrieveRevisionsByTitle(
            wiki,
            titles,
            RevisionsQuery.emptyBuilder()
                .properties(Set.of(RevisionsParameters.Properties.CONTENT))
                .slots(Set.of(MAIN_SLOT))
                .build());
    for (final String title : titles) {
      pages.stream()
          .filter(page -> title.equals(page.title()))
          .findFirst()
          .flatMap(PageAnalysisPerformanceTest::findContent)
          .ifPresent(content -> contents.put(title, content));
    }
    return contents;
  }

  private static Optional<String> findContent(final Page page) {
    return page.revisions().stream()
        .findFirst()
        .map(Revision::slots)
        .map(slots -> slots.get(MAIN_SLOT))
        .map(RevisionSlot::content);
  }

  private static String createSyntheticPage(final Collection<String> contents) {
    final List<String> sortedContents =
        contents.stream().sorted(Comparator.comparingInt(String::length).reversed()).toList();
    final StringBuilder synthetic = new StringBuilder(MAXIMUM_PAGE_LENGTH + 1_000_000);
    while (synthetic.length() < MAXIMUM_PAGE_LENGTH && !sortedContents.isEmpty()) {
      for (final String content : sortedContents) {
        synthetic.append(content).append(NEW_LINE);
      }
    }
    final int lastNewLine = synthetic.lastIndexOf(String.valueOf(NEW_LINE), MAXIMUM_PAGE_LENGTH);
    return synthetic.substring(0, lastNewLine < 0 ? MAXIMUM_PAGE_LENGTH : lastNewLine);
  }
}
