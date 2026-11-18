package org.wpcleaner.api.api.query.prop.links;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriBuilder;
import org.wpcleaner.api.api.ApiError;
import org.wpcleaner.api.api.ApiParameters;
import org.wpcleaner.api.api.ApiRestClient;
import org.wpcleaner.api.api.ApiUriBuilder;
import org.wpcleaner.api.api.Limit;
import org.wpcleaner.api.api.query.QueryParameters;
import org.wpcleaner.api.wiki.definition.WikiDefinition;

@Service
public class ApiLinks {

  private final ApiRestClient restClient;

  public ApiLinks(final ApiRestClient restClient) {
    this.restClient = restClient;
  }

  public List<Page> retrieveLinksByPageId(
      final WikiDefinition wiki, final List<Integer> pageIds, @Nullable final LinksQuery options) {
    return retrieveLinks(wiki, QueryParameters.PAGE_IDS.value, pageIds, options);
  }

  public List<Page> retrieveLinksByTitle(
      final WikiDefinition wiki, final List<String> titles, @Nullable final LinksQuery options) {
    return retrieveLinks(wiki, QueryParameters.TITLES.value, titles, options);
  }

  private <T> List<Page> retrieveLinks(
      final WikiDefinition wiki,
      final String key,
      final List<T> values,
      @Nullable final LinksQuery options) {
    final boolean followContinuation = options == null || options.plContinue() == null;
    final Map<String, Page> pages = new LinkedHashMap<>();
    Map<String, String> continuation = Map.of();
    do {
      final Map<String, String> requestContinuation = continuation;
      final Response response =
          restClient
              .getRestClient(wiki)
              .get()
              .uri(uriBuilder -> computeUri(uriBuilder, key, values, options, requestContinuation))
              .retrieve()
              .body(Response.class);
      Optional.ofNullable(response)
          .map(Response::query)
          .map(ResponseQuery::pages)
          .orElseGet(List::of)
          .forEach(page -> pages.merge(getPageKey(page), page, ApiLinks::mergePages));
      continuation =
          Optional.ofNullable(response)
              .map(Response::continuation)
              .filter(_ -> followContinuation)
              .orElseGet(Map::of);
    } while (!continuation.isEmpty());
    return List.copyOf(pages.values());
  }

  private static String getPageKey(final Page page) {
    return Objects.requireNonNullElseGet(page.title(), () -> String.valueOf(page.pageId()));
  }

  private static Page mergePages(final Page page, final Page continuedPage) {
    return new Page(
        Stream.concat(page.links().stream(), continuedPage.links().stream()).toList(),
        page.missing(),
        page.ns(),
        page.pageId(),
        page.title());
  }

  private <T> URI computeUri(
      final UriBuilder uriBuilder,
      final String key,
      final List<T> values,
      @Nullable final LinksQuery options,
      final Map<String, String> continuation) {
    final ApiUriBuilder builder = ApiUriBuilder.of(uriBuilder, ApiParameters.Action.QUERY);
    builder.queryParam(QueryParameters.PROPERTIES.value, QueryParameters.Properties.LINKS.value);
    builder.queryParamCollection(key, values);
    if (options != null) {
      computeOptions(builder, options);
    }
    continuation.forEach(builder::queryParam);
    return builder.build();
  }

  private void computeOptions(final ApiUriBuilder builder, final LinksQuery options) {
    builder.queryParam(LinksParameters.CONTINUE.value, options.plContinue());
    builder.queryParam(
        LinksParameters.DIRECTION.value, options.direction(), direction -> direction.value);
    builder.queryParam(LinksParameters.LIMIT.value, options.limit(), Limit::value);
    builder.queryParamCollection(
        LinksParameters.NAMESPACE.value,
        options.namespace(),
        namespace -> Integer.toString(namespace.id()));
    builder.queryParamCollection(LinksParameters.TITLES.value, options.titles());
  }

  private record Response(
      @JsonProperty("batchcomplete") @Nullable Boolean batchComplete,
      @JsonProperty("continue") @Nullable Map<String, String> continuation,
      @JsonProperty("query") @Nullable ResponseQuery query) {}

  private record ResponseQuery(
      @JsonProperty("docref") @Nullable String docref,
      @JsonProperty("errors") @JsonSetter(nulls = Nulls.AS_EMPTY) List<ApiError> errors,
      @JsonProperty("pages") @JsonSetter(nulls = Nulls.AS_EMPTY) List<Page> pages,
      @JsonProperty("warnings") @JsonSetter(nulls = Nulls.AS_EMPTY) List<ApiError> warnings) {}
}
