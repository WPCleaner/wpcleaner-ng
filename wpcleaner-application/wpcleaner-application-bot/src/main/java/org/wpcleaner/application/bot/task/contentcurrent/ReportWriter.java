package org.wpcleaner.application.bot.task.contentcurrent;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.wpcleaner.api.analysis.internallink.InternalLinkBuilder;
import org.wpcleaner.application.bot.content.ContentFiles;

@Service
public class ReportWriter {

  public static final String REPORT_FILE_FORMAT = "%s-error-%03d.txt";

  private static final Logger LOGGER =
      LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private static final ObjectReader READER = new ObjectMapper().readerFor(PageErrors.class);
  private static final Comparator<PageErrors> PAGE_ORDER =
      Comparator.comparingInt(PageErrors::namespace).thenComparing(PageErrors::title);

  public void write(
      final Path errorsFile,
      final ContentFiles files,
      final List<SelectedAlgorithm> algorithms,
      final Path outputFolder)
      throws IOException {
    for (final SelectedAlgorithm algorithm : algorithms) {
      final int number = algorithm.algorithm().number();
      final Path reportFile =
          outputFolder.resolve(REPORT_FILE_FORMAT.formatted(files.name(), number));
      write(errorsFile, number, files.name(), reportFile);
      LOGGER.info("Report for algorithm {} written in {}", number, reportFile);
    }
  }

  private static void write(
      final Path errorsFile, final int algorithm, final String source, final Path reportFile)
      throws IOException {
    final List<PageErrors> pages = readPages(errorsFile, algorithm);
    pages.sort(PAGE_ORDER);
    try (BufferedWriter output = Files.newBufferedWriter(reportFile, StandardCharsets.UTF_8)) {
      output.write("<!-- Generated using %s -->\n".formatted(source));
      for (final PageErrors page : pages) {
        output.write(formatPage(page));
        output.write('\n');
      }
    }
  }

  private static List<PageErrors> readPages(final Path errorsFile, final int algorithm)
      throws IOException {
    final List<PageErrors> pages = new ArrayList<>();
    try (MappingIterator<PageErrors> iterator = READER.readValues(errorsFile.toFile())) {
      while (iterator.hasNext()) {
        final PageErrors page = iterator.next();
        final List<ReportedError> errors =
            page.errors().stream().filter(error -> error.algorithm() == algorithm).toList();
        if (!errors.isEmpty()) {
          pages.add(new PageErrors(page.pageId(), page.namespace(), page.title(), errors));
        }
      }
    }
    return pages;
  }

  private static String formatPage(final PageErrors page) {
    return "* %s: %s"
        .formatted(
            InternalLinkBuilder.to(page.title()).withColon(page.namespace() != 0).build(),
            page.errors().stream()
                .map(ReportedError::text)
                .map(ReportedTextFormatter::format)
                .collect(Collectors.joining(", ")));
  }
}
