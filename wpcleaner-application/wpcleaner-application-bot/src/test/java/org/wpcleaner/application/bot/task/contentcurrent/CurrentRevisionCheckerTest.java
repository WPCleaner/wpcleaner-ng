package org.wpcleaner.application.bot.task.contentcurrent;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.client.MockRestServiceServer;
import org.wpcleaner.api.analysis.PageAnalysisFactory;
import org.wpcleaner.api.api.query.prop.revisions.ApiRevisions;
import org.wpcleaner.api.api.query.prop.revisions.MockApiRevisions;
import org.wpcleaner.api.api.query.prop.revisions.Page;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.api.wiki.definition.WikipediaDefinitions;
import org.wpcleaner.application.bot.BotSpringBootTest;
import org.wpcleaner.application.bot.request.RequestLimits;
import org.wpcleaner.lib.check.CheckAlgorithmRegistry;

@BotSpringBootTest
class CurrentRevisionCheckerTest {

  private static final WikiDefinition WIKI = WikipediaDefinitions.FR;

  @Autowired private ApiRevisions apiRevisions;
  @Autowired private CheckAlgorithmRegistry algorithmRegistry;
  @Autowired private MockApiRevisions mockApiRevisions;
  @Autowired private MockRestServiceServer mediaWikiServer;
  @Autowired private PageAnalysisFactory analysisFactory;

  @TempDir private Path folder;

  @BeforeEach
  void setUp() {
    mediaWikiServer.reset();
  }

  @DisplayName("should count the pages waiting for a check until their batch is checked")
  @Test
  void countPendingPages() throws IOException, InterruptedException {
    // GIVEN
    mockApiRevisions
        .givenRetrieveRevisionsByPageId(
            WIKI, List.of(1, 2, 3), CurrentRevisionChecker.CURRENT_CONTENT_QUERY)
        .willReturn(List.of(new Page(true, 0, 1, List.of(), "Paris")));
    try (PageErrorsWriter writer = new PageErrorsWriter(folder.resolve("errors.jsonl"))) {
      final CurrentRevisionChecker checker =
          new CurrentRevisionChecker(
              WIKI,
              apiRevisions,
              new PageErrorsDetector(WIKI, analysisFactory, algorithmRegistry, List.of()),
              writer,
              new RequestLimits(50, 60_000));
      checker.add(1);
      checker.add(2);
      checker.add(3);
      final long pendingBeforeCheck = checker.getPendingPages();

      // WHEN
      checker.finish();
      checker.call();

      // THEN
      mediaWikiServer.verify();
      Assertions.assertThat(pendingBeforeCheck).isEqualTo(3);
      Assertions.assertThat(checker.getPendingPages()).isZero();
      Assertions.assertThat(checker.getCheckedPages()).isEqualTo(3);
    }
  }
}
