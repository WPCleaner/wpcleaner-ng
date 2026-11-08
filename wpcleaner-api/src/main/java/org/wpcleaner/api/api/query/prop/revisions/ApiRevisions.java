package org.wpcleaner.api.api.query.prop.revisions;

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
public class ApiRevisions {

  private final ApiRestClient restClient;

  public ApiRevisions(final ApiRestClient restClient) {
    this.restClient = restClient;
  }

  public List<Page> retrieveRevisionsByPageId(
      final WikiDefinition wiki,
      final List<Integer> pageIds,
      @Nullable final RevisionsQuery options) {
    return retrieveRevisions(wiki, QueryParameters.PAGE_IDS.value, pageIds, options);
  }

  public List<Page> retrieveRevisionsByRevisionId(
      final WikiDefinition wiki,
      final List<Integer> revIds,
      @Nullable final RevisionsQuery options) {
    return retrieveRevisions(wiki, QueryParameters.REVISION_IDS.value, revIds, options);
  }

  public List<Page> retrieveRevisionsByTitle(
      final WikiDefinition wiki,
      final List<String> titles,
      @Nullable final RevisionsQuery options) {
    return retrieveRevisions(wiki, QueryParameters.TITLES.value, titles, options);
  }

  private <T> List<Page> retrieveRevisions(
      final WikiDefinition wiki,
      final String key,
      final List<T> values,
      @Nullable final RevisionsQuery options) {
    final boolean followContinuation = canFollowContinuation(options);
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
          .forEach(page -> pages.merge(getPageKey(page), page, ApiRevisions::mergePages));
      continuation =
          Optional.ofNullable(response)
              .map(Response::continuation)
              .filter(_ -> followContinuation)
              .orElseGet(Map::of);
    } while (!continuation.isEmpty());
    return List.copyOf(pages.values());
  }

  private static boolean canFollowContinuation(@Nullable final RevisionsQuery options) {
    return options == null
        || Stream.of(
                options.direction(),
                options.end(),
                options.endId(),
                options.excludeUser(),
                options.limit(),
                options.rvContinue(),
                options.start(),
                options.startId(),
                options.tag(),
                options.user())
            .allMatch(Objects::isNull);
  }

  private static String getPageKey(final Page page) {
    return Objects.requireNonNullElseGet(page.title(), () -> String.valueOf(page.pageId()));
  }

  private static Page mergePages(final Page page, final Page continuedPage) {
    return new Page(
        page.missing(),
        page.ns(),
        page.pageId(),
        Stream.concat(page.revisions().stream(), continuedPage.revisions().stream()).toList(),
        page.title());
  }

  private <T> URI computeUri(
      final UriBuilder uriBuilder,
      final String key,
      final List<T> values,
      @Nullable final RevisionsQuery options,
      final Map<String, String> continuation) {
    final ApiUriBuilder builder = ApiUriBuilder.of(uriBuilder, ApiParameters.Action.QUERY);
    builder.queryParam(
        QueryParameters.PROPERTIES.value, QueryParameters.Properties.REVISIONS.value);
    builder.queryParamCollection(key, values);
    if (options != null) {
      computeOptions(builder, options);
    }
    continuation.forEach(builder::queryParam);
    return builder.build();
  }

  private void computeOptions(final ApiUriBuilder builder, final RevisionsQuery options) {
    builder.queryParam(RevisionsParameters.CONTENT_FORMAT_MAIN.value, options.contentFormatMain());
    builder.queryParam(RevisionsParameters.CONTINUE.value, options.rvContinue());
    builder.queryParam(
        RevisionsParameters.DIRECTION.value, options.direction(), direction -> direction.value);
    builder.queryParam(RevisionsParameters.END.value, options.end());
    builder.queryParam(RevisionsParameters.END_ID.value, options.endId());
    builder.queryParam(RevisionsParameters.EXCLUDE_USER.value, options.excludeUser());
    builder.queryParam(RevisionsParameters.LIMIT.value, options.limit(), Limit::value);
    builder.queryParamCollection(
        RevisionsParameters.PROPERTIES.value, options.properties(), properties -> properties.value);
    builder.queryParam(RevisionsParameters.SECTION.value, options.section());
    builder.queryParamCollection(
        RevisionsParameters.SLOTS.value, options.slots(), slot -> slot.value);
    builder.queryParam(RevisionsParameters.START.value, options.start());
    builder.queryParam(RevisionsParameters.START_ID.value, options.startId());
    builder.queryParam(RevisionsParameters.TAG.value, options.tag());
    builder.queryParam(RevisionsParameters.USER.value, options.user());
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
