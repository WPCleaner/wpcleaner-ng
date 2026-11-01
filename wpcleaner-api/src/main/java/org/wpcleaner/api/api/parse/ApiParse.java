package org.wpcleaner.api.api.parse;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import java.util.List;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.wpcleaner.api.api.ApiError;
import org.wpcleaner.api.api.ApiParameters;
import org.wpcleaner.api.api.ApiResponse;
import org.wpcleaner.api.api.ApiRestClient;
import org.wpcleaner.api.api.ApiUriBuilder;
import org.wpcleaner.api.api.ApiUtils;
import org.wpcleaner.api.wiki.definition.WikiDefinition;

@Service
public class ApiParse {

  private final ApiRestClient restClient;

  public ApiParse(final ApiRestClient restClient) {
    this.restClient = restClient;
  }

  public Parse parse(final WikiDefinition wiki, final ParseQuery query) {
    return ApiUtils.processApiResponse(internalParse(wiki, query), Response::parse);
  }

  @Nullable
  private Response internalParse(final WikiDefinition wiki, final ParseQuery query) {
    final MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    body.add(ParseParameters.TITLE.value, query.title());
    body.add(ParseParameters.TEXT.value, query.text());
    body.add(ParseParameters.CONTENT_MODEL.value, ParseParameters.WIKITEXT_CONTENT_MODEL);
    body.add(
        ParseParameters.PROPERTIES.value,
        query.properties().stream()
            .map(property -> property.value)
            .sorted()
            .collect(Collectors.joining("|")));
    return restClient
        .getRestClient(wiki)
        .post()
        .uri(uriBuilder -> ApiUriBuilder.of(uriBuilder, ApiParameters.Action.PARSE).build())
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .body(body)
        .retrieve()
        .body(Response.class);
  }

  private record Response(
      @JsonProperty("docref") @Nullable String docRef,
      @JsonProperty("errors") @JsonSetter(nulls = Nulls.AS_EMPTY) List<ApiError> errors,
      @JsonProperty("parse") Parse parse,
      @JsonProperty("warnings") @JsonSetter(nulls = Nulls.AS_EMPTY) List<ApiError> warnings)
      implements ApiResponse {}
}
