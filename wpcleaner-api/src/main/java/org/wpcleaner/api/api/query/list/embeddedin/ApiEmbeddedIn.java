package org.wpcleaner.api.api.query.list.embeddedin;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriBuilder;
import org.wpcleaner.api.api.ApiError;
import org.wpcleaner.api.api.ApiParameters;
import org.wpcleaner.api.api.ApiResponse;
import org.wpcleaner.api.api.ApiRestClient;
import org.wpcleaner.api.api.ApiUriBuilder;
import org.wpcleaner.api.api.ApiUtils;
import org.wpcleaner.api.api.Limit;
import org.wpcleaner.api.api.query.QueryParameters;
import org.wpcleaner.api.wiki.definition.WikiDefinition;

@Service
public class ApiEmbeddedIn {

  private final ApiRestClient restClient;

  public ApiEmbeddedIn(final ApiRestClient restClient) {
    this.restClient = restClient;
  }

  public List<EmbeddingPage> retrieveEmbeddedInByPageId(
      final WikiDefinition wiki, final int pageId, @Nullable final EmbeddedInQuery options) {
    return retrieveEmbeddedIn(
        wiki, EmbeddedInParameters.PAGE_ID.value, Integer.toString(pageId), options);
  }

  public List<EmbeddingPage> retrieveEmbeddedInByTitle(
      final WikiDefinition wiki, final String title, @Nullable final EmbeddedInQuery options) {
    return retrieveEmbeddedIn(wiki, EmbeddedInParameters.TITLE.value, title, options);
  }

  private List<EmbeddingPage> retrieveEmbeddedIn(
      final WikiDefinition wiki,
      final String key,
      final String value,
      @Nullable final EmbeddedInQuery options) {
    final boolean followContinuation = options == null || options.eiContinue() == null;
    final List<EmbeddingPage> pages = new ArrayList<>();
    Map<String, String> continuation = Map.of();
    do {
      final Map<String, String> requestContinuation = continuation;
      final Response response =
          ApiUtils.processApiResponse(
              restClient
                  .getRestClient(wiki)
                  .get()
                  .uri(
                      uriBuilder ->
                          computeUri(uriBuilder, key, value, options, requestContinuation))
                  .retrieve()
                  .body(Response.class),
              apiResponse -> apiResponse);
      pages.addAll(
          Optional.ofNullable(response.query()).map(ResponseQuery::embeddedIn).orElseGet(List::of));
      continuation =
          Optional.ofNullable(response.continuation())
              .filter(_ -> followContinuation)
              .orElseGet(Map::of);
    } while (!continuation.isEmpty());
    return List.copyOf(pages);
  }

  private static URI computeUri(
      final UriBuilder uriBuilder,
      final String key,
      final String value,
      @Nullable final EmbeddedInQuery options,
      final Map<String, String> continuation) {
    final ApiUriBuilder builder = ApiUriBuilder.of(uriBuilder, ApiParameters.Action.QUERY);
    builder.queryParam(QueryParameters.LIST.value, QueryParameters.List.EMBEDDED_IN.value);
    builder.queryParam(key, value);
    if (options != null) {
      computeOptions(builder, options);
    }
    continuation.forEach(builder::queryParam);
    return builder.build();
  }

  private static void computeOptions(final ApiUriBuilder builder, final EmbeddedInQuery options) {
    builder.queryParam(EmbeddedInParameters.CONTINUE.value, options.eiContinue());
    builder.queryParam(
        EmbeddedInParameters.DIRECTION.value, options.direction(), direction -> direction.value);
    builder.queryParam(
        EmbeddedInParameters.FILTER_REDIRECT.value,
        options.filterRedirect(),
        filterRedirect -> filterRedirect.value);
    builder.queryParam(EmbeddedInParameters.LIMIT.value, options.limit(), Limit::value);
    builder.queryParamCollection(
        EmbeddedInParameters.NAMESPACE.value,
        options.namespace(),
        namespace -> Integer.toString(namespace.id()));
  }

  private record Response(
      @JsonProperty("batchcomplete") @Nullable Boolean batchComplete,
      @JsonProperty("continue") @Nullable Map<String, String> continuation,
      @JsonProperty("docref") @Nullable String docRef,
      @JsonProperty("errors") @JsonSetter(nulls = Nulls.AS_EMPTY) List<ApiError> errors,
      @JsonProperty("query") @Nullable ResponseQuery query,
      @JsonProperty("warnings") @JsonSetter(nulls = Nulls.AS_EMPTY) List<ApiError> warnings)
      implements ApiResponse {}

  private record ResponseQuery(
      @JsonProperty("embeddedin") @JsonSetter(nulls = Nulls.AS_EMPTY)
          List<EmbeddingPage> embeddedIn) {}
}
