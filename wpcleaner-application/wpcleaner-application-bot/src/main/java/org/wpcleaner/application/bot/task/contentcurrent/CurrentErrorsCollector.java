package org.wpcleaner.application.bot.task.contentcurrent;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.wpcleaner.api.api.query.prop.revisions.ApiRevisions;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.application.bot.content.ContentFiles;
import org.wpcleaner.application.bot.content.ContentFilesProcessor;
import org.wpcleaner.application.bot.request.RequestLimitsProvider;
import org.wpcleaner.application.bot.utils.Futures;

@Service
public class CurrentErrorsCollector {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private static final long PROGRESS_MINUTES = 1;

  private final ApiRevisions apiRevisions;
  private final ContentFilesProcessor filesProcessor;
  private final RequestLimitsProvider limitsProvider;

  public CurrentErrorsCollector(
      final ApiRevisions apiRevisions,
      final ContentFilesProcessor filesProcessor,
      final RequestLimitsProvider limitsProvider) {
    this.apiRevisions = apiRevisions;
    this.filesProcessor = filesProcessor;
    this.limitsProvider = limitsProvider;
  }

  public void collect(
      final WikiDefinition wiki,
      final ContentFiles files,
      final PageErrorsDetector detector,
      final Path errorsFile)
      throws IOException, InterruptedException {
    try (PageErrorsWriter writer = new PageErrorsWriter(errorsFile)) {
      collect(
          files,
          detector,
          new CurrentRevisionChecker(
              wiki, apiRevisions, detector, writer, limitsProvider.getLimits()));
    }
  }

  private void collect(
      final ContentFiles files,
      final PageErrorsDetector detector,
      final CurrentRevisionChecker checker)
      throws IOException, InterruptedException {
    final AtomicLong readPages = new AtomicLong();
    final AtomicLong candidatePages = new AtomicLong();
    try (ExecutorService checkerExecutor = Executors.newSingleThreadExecutor();
        ScheduledExecutorService progressExecutor = Executors.newSingleThreadScheduledExecutor()) {
      final Future<Void> checking = checkerExecutor.submit(checker);
      final ScheduledFuture<?> progress =
          progressExecutor.scheduleAtFixedRate(
              () -> logProgress(readPages, candidatePages, checker),
              PROGRESS_MINUTES,
              PROGRESS_MINUTES,
              TimeUnit.MINUTES);
      try {
        filesProcessor.process(
            files,
            page -> {
              readPages.incrementAndGet();
              if (!detector.detect(page).isEmpty()) {
                candidatePages.incrementAndGet();
                checker.add(page.id());
              }
            });
      } finally {
        checker.finish();
      }
      Futures.waitFor(checking);
      progress.cancel(false);
    }
    logProgress(readPages, candidatePages, checker);
  }

  private static void logProgress(
      final AtomicLong readPages,
      final AtomicLong candidatePages,
      final CurrentRevisionChecker checker) {
    LOGGER.info(
        "Pages read: {}, with errors in content files: {}, checked: {}, still with errors: {},"
            + " waiting for check: {}",
        readPages.get(),
        candidatePages.get(),
        checker.getCheckedPages(),
        checker.getPagesWithErrors(),
        checker.getPendingPages());
  }
}
