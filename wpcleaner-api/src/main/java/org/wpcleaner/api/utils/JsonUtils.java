package org.wpcleaner.api.utils;

/*
 * SPDX-FileCopyrightText: © 2025 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.File;
import java.nio.charset.StandardCharsets;
import org.springframework.core.io.Resource;

public final class JsonUtils {

  private static final ObjectMapper MAPPER =
      new ObjectMapper()
          .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
          .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
          .registerModule(new JavaTimeModule());
  private static final ObjectWriter PRETTY_WRITER = MAPPER.writerWithDefaultPrettyPrinter();

  private JsonUtils() {
    // Utility class
  }

  public static <T> T readValue(final Resource resource, final Class<T> valueType) {
    return AutoCatch.run(
        () -> readValue(resource.getContentAsString(StandardCharsets.UTF_8), valueType));
  }

  public static <T> T readValue(final String json, final Class<T> valueType) {
    return AutoCatch.run(() -> MAPPER.readValue(json, valueType));
  }

  public static <T> void writeValue(final File file, final T value) {
    AutoCatch.run(() -> PRETTY_WRITER.writeValue(file, value));
  }
}
