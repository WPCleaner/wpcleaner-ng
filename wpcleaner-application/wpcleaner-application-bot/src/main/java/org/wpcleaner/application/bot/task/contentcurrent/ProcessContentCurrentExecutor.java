package org.wpcleaner.application.bot.task.contentcurrent;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.wpcleaner.api.analysis.PageAnalysisFactory;
import org.wpcleaner.application.bot.content.ContentFiles;
import org.wpcleaner.application.bot.content.ContentFilesLocator;
import org.wpcleaner.application.bot.script.ProcessContentCurrentTask;
import org.wpcleaner.application.bot.task.TaskContext;
import org.wpcleaner.lib.check.CheckAlgorithmRegistry;

@Service
public class ProcessContentCurrentExecutor {

  public static final String ERRORS_FILE_SUFFIX = "-errors.jsonl";

  private static final Logger LOGGER =
      LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final CheckAlgorithmRegistry algorithmRegistry;
  private final PageAnalysisFactory analysisFactory;
  private final ContentFilesLocator filesLocator;
  private final CurrentErrorsCollector errorsCollector;
  private final ReportWriter reportWriter;

  public ProcessContentCurrentExecutor(
      final CheckAlgorithmRegistry algorithmRegistry,
      final PageAnalysisFactory analysisFactory,
      final ContentFilesLocator filesLocator,
      final CurrentErrorsCollector errorsCollector,
      final ReportWriter reportWriter) {
    this.algorithmRegistry = algorithmRegistry;
    this.analysisFactory = analysisFactory;
    this.filesLocator = filesLocator;
    this.errorsCollector = errorsCollector;
    this.reportWriter = reportWriter;
  }

  public void execute(final ProcessContentCurrentTask task, final TaskContext context)
      throws IOException, InterruptedException {
    final ContentFiles files = filesLocator.locate(context.contentFolder(), context.wiki());
    LOGGER.info("Processing {} content files of {}", files.files().size(), files.name());
    final List<SelectedAlgorithm> algorithms = selectAlgorithms(task);
    Files.createDirectories(context.outputFolder());
    final Path errorsFile = context.outputFolder().resolve(files.name() + ERRORS_FILE_SUFFIX);
    errorsCollector.collect(
        context.wiki(),
        files,
        new PageErrorsDetector(context.wiki(), analysisFactory, algorithmRegistry, algorithms),
        errorsFile);
    LOGGER.info("Errors still present written in {}", errorsFile);
    reportWriter.write(errorsFile, files, algorithms, context.outputFolder());
  }

  private List<SelectedAlgorithm> selectAlgorithms(final ProcessContentCurrentTask task) {
    return task.report().stream()
        .map(
            reported ->
                new SelectedAlgorithm(
                    algorithmRegistry
                        .findAlgorithm(reported.algorithm())
                        .orElseThrow(
                            () ->
                                new IllegalArgumentException(
                                    "Unknown algorithm %d".formatted(reported.algorithm()))),
                    task.namespaces(reported)))
        .toList();
  }
}
