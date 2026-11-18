package org.wpcleaner.api.api.query.list.search;

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
public class ApiSearch {

  private final ApiRestClient restClient;

  public ApiSearch(final ApiRestClient restClient) {
    this.restClient = restClient;
  }

  public List<SearchPage> searchPages(
      final WikiDefinition wiki, final String search, @Nullable final SearchQuery options) {
    final boolean followContinuation = options == null || options.offset() == null;
    final List<SearchPage> pages = new ArrayList<>();
    Map<String, String> continuation = Map.of();
    do {
      final Map<String, String> requestContinuation = continuation;
      final Response response =
          ApiUtils.processApiResponse(
              restClient
                  .getRestClient(wiki)
                  .get()
                  .uri(uriBuilder -> computeUri(uriBuilder, search, options, requestContinuation))
                  .retrieve()
                  .body(Response.class),
              apiResponse -> apiResponse);
      pages.addAll(
          Optional.ofNullable(response.query()).map(ResponseQuery::search).orElseGet(List::of));
      continuation =
          Optional.ofNullable(response.continuation())
              .filter(_ -> followContinuation)
              .orElseGet(Map::of);
    } while (!continuation.isEmpty());
    return List.copyOf(pages);
  }

  private static URI computeUri(
      final UriBuilder uriBuilder,
      final String search,
      @Nullable final SearchQuery options,
      final Map<String, String> continuation) {
    final ApiUriBuilder builder = ApiUriBuilder.of(uriBuilder, ApiParameters.Action.QUERY);
    builder.queryParam(QueryParameters.LIST.value, QueryParameters.List.SEARCH.value);
    builder.queryParam(SearchParameters.SEARCH.value, search);
    if (options != null) {
      computeOptions(builder, options);
    }
    continuation.forEach(builder::queryParam);
    return builder.build();
  }

  private static void computeOptions(final ApiUriBuilder builder, final SearchQuery options) {
    builder.queryParam(SearchParameters.ENABLE_REWRITES.value, options.enableRewrites());
    builder.queryParamCollection(SearchParameters.INFO.value, options.info(), info -> info.value);
    builder.queryParam(SearchParameters.INTERWIKI.value, options.interwiki());
    builder.queryParam(SearchParameters.LIMIT.value, options.limit(), Limit::value);
    builder.queryParamCollection(
        SearchParameters.NAMESPACE.value,
        options.namespace(),
        namespace -> Integer.toString(namespace.id()));
    builder.queryParam(SearchParameters.OFFSET.value, options.offset());
    builder.queryParamCollection(
        SearchParameters.PROPERTIES.value, options.properties(), properties -> properties.value);
    builder.queryParam(
        SearchParameters.QUERY_DEPENDENT_PROFILE.value,
        options.queryDependentProfile(),
        profile -> profile.value);
    builder.queryParam(
        SearchParameters.QUERY_INDEPENDENT_PROFILE.value,
        options.queryIndependentProfile(),
        profile -> profile.value);
    builder.queryParam(SearchParameters.SORT.value, options.sort(), sort -> sort.value);
    builder.queryParam(SearchParameters.WHAT.value, options.what(), what -> what.value);
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
      @JsonProperty("search") @JsonSetter(nulls = Nulls.AS_EMPTY) List<SearchPage> search,
      @JsonProperty("searchinfo") @Nullable SearchInfo searchInfo) {}

  private record SearchInfo(
      @JsonProperty("rewrittenquery") @Nullable String rewrittenQuery,
      @JsonProperty("rewrittenquerysnippet") @Nullable String rewrittenQuerySnippet,
      @JsonProperty("suggestion") @Nullable String suggestion,
      @JsonProperty("suggestionsnippet") @Nullable String suggestionSnippet,
      @JsonProperty("totalhits") @Nullable Integer totalHits) {}
}
