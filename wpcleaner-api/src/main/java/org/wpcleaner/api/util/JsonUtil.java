package org.wpcleaner.api.util;

/*
 * SPDX-FileCopyrightText: © 2025 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.core.io.Resource;

public final class JsonUtil {

  private static final ObjectMapper MAPPER =
      new ObjectMapper()
          .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
          .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
          .registerModule(new JavaTimeModule());

  private JsonUtil() {
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
}
