package org.wpcleaner.api.utils;

/*
 * SPDX-FileCopyrightText: © 2025 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.File;
import java.io.IOException;
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

  @SuppressWarnings("PMD.AvoidThrowingRawExceptionTypes")
  public static <T> T readValue(final Resource resource, final Class<T> valueType) {
    try {
      return readValue(resource.getContentAsString(StandardCharsets.UTF_8), valueType);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @SuppressWarnings("PMD.AvoidThrowingRawExceptionTypes")
  public static <T> T readValue(final String json, final Class<T> valueType) {
    try {
      return MAPPER.readValue(json, valueType);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  @SuppressWarnings("PMD.AvoidThrowingRawExceptionTypes")
  public static <T> void writeValue(final File file, final T value) {
    try {
      PRETTY_WRITER.writeValue(file, value);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
