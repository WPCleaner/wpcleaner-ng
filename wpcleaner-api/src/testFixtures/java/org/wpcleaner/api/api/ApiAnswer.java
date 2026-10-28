package org.wpcleaner.api.api;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ResponseActions;
import org.springframework.test.web.client.response.MockRestResponseCreators;

public final class ApiAnswer {

  private static final ObjectMapper MAPPER =
      new ObjectMapper()
          .setDefaultPropertyInclusion(
              JsonInclude.Value.construct(
                  JsonInclude.Include.NON_NULL, JsonInclude.Include.NON_NULL));

  private final ResponseActions request;

  ApiAnswer(final ResponseActions request) {
    this.request = request;
  }

  public void willReturn(final Object response) {
    request.andRespond(
        MockRestResponseCreators.withSuccess(toJson(response), MediaType.APPLICATION_JSON));
  }

  public void willReturnQuery(final String key, final Object value) {
    willReturn(Map.of("batchcomplete", true, "query", Map.of(key, value)));
  }

  public void willFail() {
    request.andRespond(MockRestResponseCreators.withServerError());
  }

  private static String toJson(final Object response) {
    try {
      return MAPPER.writeValueAsString(response);
    } catch (final JsonProcessingException e) {
      throw new IllegalArgumentException("Unable to create API response", e);
    }
  }
}
