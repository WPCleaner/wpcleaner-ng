package org.wpcleaner.lib.check.comparison;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.wpcleaner.api.analysis.PageAnalysis;
import org.wpcleaner.api.analysis.PageAnalysisFactory;
import org.wpcleaner.api.api.query.Slot;
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
import org.wpcleaner.lib.check.CheckAlgorithm;
import org.wpcleaner.lib.check.CheckAlgorithmRegistry;
import org.wpcleaner.lib.check.SpringBootTestConfig;
import org.wpcleaner.lib.check.server.CheckWikiServer;

@SpringBootTest(classes = SpringBootTestConfig.class)
class CheckWikiComparisonTest {

  private static final int MAX_PAGES = 100;
  private static final int TITLES_PER_REQUEST = 50;
  private static final Path REPORT =
      Path.of("build", "reports", "check-wiki-comparison", "report.md");

  @Autowired private ApiRevisions apiRevisions;
  @Autowired private CheckAlgorithmRegistry algorithmRegistry;
  @Autowired private CheckWikiServer checkWikiServer;
  @Autowired private LoginHook loginHook;
  @Autowired private PageAnalysisFactory pageAnalysisFactory;

  @DisplayName("Compare detected errors with the pages listed by Check Wiki")
  @Test
  void compareWithCheckWiki() throws IOException {
    // GIVEN
    final CheckWikiReport report = new CheckWikiReport();

    // WHEN
    for (final WikiDefinition wiki : List.of(WikipediaDefinitions.FR, WikipediaDefinitions.EN)) {
      loginHook.executeHook(
          wiki, WPCleanerConfigurationLoader.GENERAL_USERNAME, ProgressTracker.NOOP);
      for (final CheckAlgorithm algorithm : algorithmRegistry.getAlgorithms()) {
        compareAlgorithm(wiki, algorithm, report);
      }
    }
    Files.createDirectories(REPORT.getParent());
    Files.writeString(REPORT, report.toMarkdown());

    // THEN
    Assertions.assertThat(report.getPageCount()).as("compared pages").isPositive();
  }

  private void compareAlgorithm(
      final WikiDefinition wiki, final CheckAlgorithm algorithm, final CheckWikiReport report) {
    if (!algorithmRegistry.getActiveAlgorithms(wiki).contains(algorithm)) {
      report.addInactiveAlgorithm(wiki, algorithm.number());
      return;
    }
    final List<String> titles = checkWikiServer.retrievePages(wiki, algorithm.number(), MAX_PAGES);
    report.addListedPages(wiki, algorithm.number(), titles.size());
    for (int index = 0; index < titles.size(); index += TITLES_PER_REQUEST) {
      retrievePages(
              wiki, titles.subList(index, Math.min(titles.size(), index + TITLES_PER_REQUEST)))
          .forEach(
              page -> report.addPage(wiki, algorithm.number(), comparePage(wiki, algorithm, page)));
    }
  }

  private CheckWikiReport.PageResult comparePage(
      final WikiDefinition wiki, final CheckAlgorithm algorithm, final Page page) {
    final String title = Optional.ofNullable(page.title()).orElse("");
    final Optional<Revision> revision = page.revisions().stream().findFirst();
    final Optional<String> content =
        revision.flatMap(Revision::mainSlot).map(RevisionSlot::content);
    if (page.missing() || content.isEmpty()) {
      return new CheckWikiReport.PageResult(title, 0, CheckWikiReport.Status.MISSING, 0);
    }
    final int revisionId = revision.map(Revision::revId).orElse(0);
    final PageAnalysis analysis = pageAnalysisFactory.analysis(wiki, title, content.get());
    if (!algorithmRegistry.getActiveAlgorithms(analysis).contains(algorithm)) {
      return new CheckWikiReport.PageResult(
          title, revisionId, CheckWikiReport.Status.WHITELISTED, 0);
    }
    final int errorCount = algorithm.analyze(analysis).size();
    return new CheckWikiReport.PageResult(
        title,
        revisionId,
        errorCount > 0 ? CheckWikiReport.Status.DETECTED : CheckWikiReport.Status.NOT_DETECTED,
        errorCount);
  }

  private List<Page> retrievePages(final WikiDefinition wiki, final List<String> titles) {
    return apiRevisions.retrieveRevisionsByTitle(
        wiki,
        titles,
        RevisionsQuery.emptyBuilder()
            .properties(
                Set.of(RevisionsParameters.Properties.CONTENT, RevisionsParameters.Properties.IDS))
            .slots(Set.of(Slot.MAIN))
            .build());
  }
}
