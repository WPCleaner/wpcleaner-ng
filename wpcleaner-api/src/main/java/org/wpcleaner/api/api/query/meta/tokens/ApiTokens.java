package org.wpcleaner.api.api.query.meta.tokens;

/*
 * SPDX-FileCopyrightText: © 2025 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import jakarta.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.wpcleaner.api.api.ApiError;
import org.wpcleaner.api.api.ApiParameters;
import org.wpcleaner.api.api.ApiRestClient;
import org.wpcleaner.api.api.ApiUtils;
import org.wpcleaner.api.api.query.QueryParameters;
import org.wpcleaner.api.wiki.definition.WikiDefinition;

@Service
public class ApiTokens {

  private final ApiRestClient restClient;

  public ApiTokens(final ApiRestClient restClient) {
    this.restClient = restClient;
  }

  public Tokens requestTokens(
      final WikiDefinition wiki, final Collection<TokensParameters.Type> tokens) {
    return Optional.ofNullable(internalRequestTokens(wiki, tokens))
        .map(Response::query)
        .map(ResponseQuery::tokens)
        .orElseGet(Tokens::ofEmpty);
  }

  @Nullable
  private Response internalRequestTokens(
      final WikiDefinition wiki, final Collection<TokensParameters.Type> tokens) {
    return restClient
        .getRestClient(wiki)
        .get()
        .uri(
            uriBuilder ->
                ApiUtils.configure(uriBuilder, ApiParameters.Action.QUERY)
                    .queryParam(QueryParameters.META.value, QueryParameters.Meta.TOKENS.value)
                    .queryParam(
                        TokensParameters.TYPE.value,
                        tokens.stream().map(type -> type.value).collect(Collectors.joining("|")))
                    .build())
        .retrieve()
        .body(Response.class);
  }

  private record Response(
      @JsonProperty("batchcomplete") boolean batchComplete,
      @JsonProperty("query") ResponseQuery query) {}

  private record ResponseQuery(
      @JsonProperty("errors") @JsonSetter(nulls = Nulls.AS_EMPTY) List<ApiError> errors,
      @JsonProperty("warnings") @JsonSetter(nulls = Nulls.AS_EMPTY) List<ApiError> warnings,
      @JsonProperty("docref") @Nullable String docref,
      @JsonProperty("tokens") Tokens tokens) {}
}
