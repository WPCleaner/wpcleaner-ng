package org.wpcleaner.api.api.query.prop.revisions;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import java.net.URI;
import java.util.List;
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
public class ApiRevisions {

  private final ApiRestClient restClient;

  public ApiRevisions(final ApiRestClient restClient) {
    this.restClient = restClient;
  }

  public List<Page> retrieveRevisionsByPageId(
      final WikiDefinition wiki,
      final List<Integer> pageIds,
      @Nullable final RevisionsQuery options) {
    final Response response = internalRetrieveRevisionsByPageId(wiki, pageIds, options);
    if (response == null || response.query() == null) {
      return List.of();
    }
    return response.query().pages();
  }

  public List<Page> retrieveRevisionsByRevisionId(
      final WikiDefinition wiki,
      final List<Integer> revIds,
      @Nullable final RevisionsQuery options) {
    final Response response = internalRetrieveRevisionsByRevisionId(wiki, revIds, options);
    if (response == null || response.query() == null) {
      return List.of();
    }
    return response.query().pages();
  }

  public List<Page> retrieveRevisionsByTitle(
      final WikiDefinition wiki,
      final List<String> titles,
      @Nullable final RevisionsQuery options) {
    final Response response = internalRetrieveRevisionsByTitle(wiki, titles, options);
    if (response == null || response.query() == null) {
      return List.of();
    }
    return response.query().pages();
  }

  @Nullable
  private Response internalRetrieveRevisionsByPageId(
      final WikiDefinition wiki,
      final List<Integer> pageIds,
      @Nullable final RevisionsQuery options) {
    return restClient
        .getRestClient(wiki)
        .get()
        .uri(uriBuilder -> computeUri(uriBuilder, QueryParameters.PAGE_IDS.value, pageIds, options))
        .retrieve()
        .body(Response.class);
  }

  @Nullable
  private Response internalRetrieveRevisionsByRevisionId(
      final WikiDefinition wiki,
      final List<Integer> revIds,
      @Nullable final RevisionsQuery options) {
    return restClient
        .getRestClient(wiki)
        .get()
        .uri(
            uriBuilder ->
                computeUri(uriBuilder, QueryParameters.REVISION_IDS.value, revIds, options))
        .retrieve()
        .body(Response.class);
  }

  @Nullable
  private Response internalRetrieveRevisionsByTitle(
      final WikiDefinition wiki,
      final List<String> titles,
      @Nullable final RevisionsQuery options) {
    return restClient
        .getRestClient(wiki)
        .get()
        .uri(uriBuilder -> computeUri(uriBuilder, QueryParameters.TITLES.value, titles, options))
        .retrieve()
        .body(Response.class);
  }

  private <T> URI computeUri(
      final UriBuilder uriBuilder,
      final String key,
      final List<T> values,
      @Nullable final RevisionsQuery options) {
    final ApiUriBuilder builder = ApiUriBuilder.of(uriBuilder, ApiParameters.Action.QUERY);
    builder.queryParam(
        QueryParameters.PROPERTIES.value, QueryParameters.Properties.REVISIONS.value);
    builder.queryParamCollection(key, values);
    if (options != null) {
      computeOptions(builder, options);
    }
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
    builder.queryParam(RevisionsParameters.LIMIT.value, options.limit());
    builder.queryParamCollection(
        RevisionsParameters.PROPERTIES.value, options.properties(), properties -> properties.value);
    builder.queryParam(RevisionsParameters.SECTION.value, options.section());
    builder.queryParamCollection(RevisionsParameters.SLOTS.value, options.slots());
    builder.queryParam(RevisionsParameters.START.value, options.start());
    builder.queryParam(RevisionsParameters.START_ID.value, options.startId());
    builder.queryParam(RevisionsParameters.TAG.value, options.tag());
    builder.queryParam(RevisionsParameters.USER.value, options.user());
  }

  private record Response(
      @JsonProperty("batchcomplete") @Nullable Boolean batchComplete,
      @JsonProperty("query") @Nullable ResponseQuery query) {}

  private record ResponseQuery(
      @JsonProperty("docref") @Nullable String docref,
      @JsonProperty("errors") @JsonSetter(nulls = Nulls.AS_EMPTY) List<ApiError> errors,
      @JsonProperty("pages") @JsonSetter(nulls = Nulls.AS_EMPTY) List<Page> pages,
      @JsonProperty("warnings") @JsonSetter(nulls = Nulls.AS_EMPTY) List<ApiError> warnings) {}
}
