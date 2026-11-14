package org.wpcleaner.application.bot.task.contentcurrent;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class PageErrorsWriter implements AutoCloseable {

  private static final ObjectWriter WRITER = new ObjectMapper().writerFor(PageErrors.class);

  private final BufferedWriter output;

  public PageErrorsWriter(final Path file) throws IOException {
    this.output = Files.newBufferedWriter(file, StandardCharsets.UTF_8);
  }

  public void write(final PageErrors errors) throws IOException {
    output.write(WRITER.writeValueAsString(errors));
    output.write('\n');
    output.flush();
  }

  @Override
  public void close() throws IOException {
    output.close();
  }
}
