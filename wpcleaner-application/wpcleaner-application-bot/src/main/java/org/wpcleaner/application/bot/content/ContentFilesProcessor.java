package org.wpcleaner.application.bot.content;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.wpcleaner.application.bot.utils.Futures;

@Service
public class ContentFilesProcessor {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private static final int QUEUED_PAGES_PER_THREAD = 4;

  private final ContentFileReader fileReader;

  public ContentFilesProcessor(final ContentFileReader fileReader) {
    this.fileReader = fileReader;
  }

  public void process(final ContentFiles files, final Consumer<ContentPage> consumer)
      throws IOException, InterruptedException {
    final int threads = Runtime.getRuntime().availableProcessors();
    final List<Future<?>> readings = new ArrayList<>();
    try (ExecutorService processExecutor =
            new ThreadPoolExecutor(
                threads,
                threads,
                0,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(threads * QUEUED_PAGES_PER_THREAD),
                new ThreadPoolExecutor.CallerRunsPolicy());
        ExecutorService readExecutor = Executors.newFixedThreadPool(files.files().size())) {
      for (final Path file : files.files()) {
        readings.add(
            readExecutor.submit(
                () -> {
                  LOGGER.info("Reading {}", file);
                  fileReader.read(
                      file, page -> processExecutor.execute(() -> consumer.accept(page)));
                  LOGGER.info("Finished reading {}", file);
                  return null;
                }));
      }
      for (final Future<?> reading : readings) {
        Futures.waitFor(reading);
      }
    }
  }
}
