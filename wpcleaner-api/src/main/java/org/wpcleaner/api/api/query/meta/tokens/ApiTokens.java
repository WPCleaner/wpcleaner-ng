package org.wpcleaner.api.api.query.meta.tokens;

/*
 * SPDX-FileCopyrightText: © 2025 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriBuilder;
import org.wpcleaner.api.api.ApiError;
import org.wpcleaner.api.api.ApiParameters;
import org.wpcleaner.api.api.ApiRestClient;
import org.wpcleaner.api.api.ApiUriBuilder;
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
        .uri(uriBuilder -> computeUri(uriBuilder, tokens))
        .retrieve()
        .body(Response.class);
  }

  private URI computeUri(
      final UriBuilder uriBuilder, final Collection<TokensParameters.Type> tokens) {
    final ApiUriBuilder builder = ApiUriBuilder.of(uriBuilder, ApiParameters.Action.QUERY);
    builder.queryParam(QueryParameters.META.value, QueryParameters.Meta.TOKENS.value);
    builder.queryParamCollection(TokensParameters.TYPE.value, tokens, token -> token.value);
    return builder.build();
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
