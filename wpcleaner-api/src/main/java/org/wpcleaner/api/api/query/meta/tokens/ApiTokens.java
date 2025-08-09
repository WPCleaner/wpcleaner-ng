package org.wpcleaner.api.api.query.meta.tokens;

/*
 * SPDX-FileCopyrightText: © 2025 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.wpcleaner.api.api.ApiParameters;
import org.wpcleaner.api.api.query.QueryParameters;
import org.wpcleaner.api.wiki.definition.WikiDefinition;

@Service
public class ApiTokens {

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
    final RestClient restClient = RestClient.builder().build();
    return restClient
        .get()
        .uri(
            uriBuilder ->
                uriBuilder
                    .scheme("https")
                    .host(wiki.mainHost())
                    .path(wiki.apiPath())
                    .queryParam(ApiParameters.ACTION.value, ApiParameters.Action.QUERY.value)
                    .queryParam(ApiParameters.FORMAT.value, ApiParameters.Format.JSON.value)
                    .queryParam(QueryParameters.META.value, QueryParameters.Meta.TOKENS.value)
                    .queryParam(
                        TokensParameters.TYPE.value,
                        tokens.stream().map(type -> type.value).collect(Collectors.joining("|")))
                    .build())
        .header(HttpHeaders.ACCEPT_CHARSET, "utf-8")
        .header(HttpHeaders.ACCEPT_ENCODING, "gzip")
        .header(HttpHeaders.CONTENT_TYPE, "application/json")
        .header(HttpHeaders.USER_AGENT, "WPCleaner")
        .retrieve()
        .body(Response.class);
  }

  @SuppressWarnings("unused")
  private record Response(
      @JsonProperty("batchcomplete") boolean batchComplete,
      @JsonProperty("query") ResponseQuery query) {}

  private record ResponseQuery(@JsonProperty("tokens") Tokens tokens) {}
}
