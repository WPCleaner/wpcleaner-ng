package org.wpcleaner.api.api.expandtemplates;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import java.util.List;
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
public class ApiExpandTemplates {

  private final ApiRestClient restClient;

  public ApiExpandTemplates(final ApiRestClient restClient) {
    this.restClient = restClient;
  }

  public ExpandTemplates expandTemplates(
      final WikiDefinition wiki, final ExpandTemplatesQuery query) {
    return ApiUtils.processApiResponse(
        internalExpandTemplates(wiki, query), Response::expandTemplates);
  }

  @Nullable
  private Response internalExpandTemplates(
      final WikiDefinition wiki, final ExpandTemplatesQuery query) {
    final MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    ApiUtils.addBodyParameter(body, ExpandTemplatesParameters.TITLE.value, query.title());
    ApiUtils.addBodyParameter(body, ExpandTemplatesParameters.TEXT.value, query.text());
    ApiUtils.addBodyParameter(
        body, ExpandTemplatesParameters.REVISION_ID.value, query.revisionId());
    ApiUtils.addBodyParameter(
        body,
        ExpandTemplatesParameters.PROPERTIES.value,
        query.properties().stream().map(property -> property.value).sorted().toList());
    ApiUtils.addBodyParameter(
        body, ExpandTemplatesParameters.INCLUDE_COMMENTS.value, query.includeComments());
    ApiUtils.addBodyParameter(
        body, ExpandTemplatesParameters.SHOW_STRATEGY_KEYS.value, query.showStrategyKeys());
    return restClient
        .getRestClient(wiki)
        .post()
        .uri(
            uriBuilder ->
                ApiUriBuilder.of(uriBuilder, ApiParameters.Action.EXPAND_TEMPLATES).build())
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .body(body)
        .retrieve()
        .body(Response.class);
  }

  private record Response(
      @JsonProperty("docref") @Nullable String docRef,
      @JsonProperty("errors") @JsonSetter(nulls = Nulls.AS_EMPTY) List<ApiError> errors,
      @JsonProperty("expandtemplates") ExpandTemplates expandTemplates,
      @JsonProperty("warnings") @JsonSetter(nulls = Nulls.AS_EMPTY) List<ApiError> warnings)
      implements ApiResponse {}
}
