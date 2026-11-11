package org.wpcleaner.launcher.utils;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.IOException;
import java.nio.file.Path;

public final class JsonFiles {

  private static final ObjectMapper MAPPER =
      new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

  private JsonFiles() {}

  public static <T> T read(final String content, final Class<T> type) throws IOException {
    return MAPPER.readValue(content, type);
  }

  public static <T> T read(final Path file, final Class<T> type) throws IOException {
    return MAPPER.readValue(file.toFile(), type);
  }

  public static void write(final Path file, final Object value) throws IOException {
    MAPPER.writeValue(file.toFile(), value);
  }
}
