package org.wpcleaner.api.api.query.list.recentchanges;

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
public class ApiRecentChanges {

  private final ApiRestClient restClient;

  public ApiRecentChanges(final ApiRestClient restClient) {
    this.restClient = restClient;
  }

  public List<RecentChange> retrieveRecentChanges(
      final WikiDefinition wiki, @Nullable final RecentChangesQuery options) {
    final Response response = internalRetrieveRecentChanges(wiki, options);
    if (response == null) {
      return List.of();
    }
    return response.query().recentChanges();
  }

  @Nullable
  private Response internalRetrieveRecentChanges(
      final WikiDefinition wiki, @Nullable final RecentChangesQuery options) {
    return restClient
        .getRestClient(wiki)
        .get()
        .uri(uriBuilder -> computeUri(uriBuilder, options))
        .retrieve()
        .body(Response.class);
  }

  private URI computeUri(final UriBuilder uriBuilder, @Nullable final RecentChangesQuery options) {
    final ApiUriBuilder builder = ApiUriBuilder.of(uriBuilder, ApiParameters.Action.QUERY);
    builder.queryParam(QueryParameters.LIST.value, QueryParameters.List.RECENT_CHANGES.value);
    if (options != null) {
      computeOptions(builder, options);
    }
    return builder.build();
  }

  private void computeOptions(final ApiUriBuilder builder, final RecentChangesQuery options) {
    builder.queryParam(RecentChangesParameters.START.value, options.start());
    builder.queryParam(RecentChangesParameters.END.value, options.end());
    builder.queryParam(
        RecentChangesParameters.DIRECTION.value, options.direction(), direction -> direction.value);
    builder.queryParamCollection(RecentChangesParameters.NAMESPACE.value, options.namespace());
    builder.queryParam(RecentChangesParameters.USER.value, options.user());
    builder.queryParam(RecentChangesParameters.EXCLUDE_USER.value, options.excludeUser());
    builder.queryParam(RecentChangesParameters.TAG.value, options.tag());
    builder.queryParamCollection(
        RecentChangesParameters.PROPERTIES.value,
        options.properties(),
        properties -> properties.value);
    builder.queryParamCollection(
        RecentChangesParameters.SHOW.value, options.show(), show -> show.value);
    builder.queryParam(RecentChangesParameters.LIMIT.value, options.limit());
    builder.queryParamCollection(
        RecentChangesParameters.TYPE.value, options.type(), type -> type.value);
    builder.queryParam(RecentChangesParameters.TOP_ONLY.value, options.topOnly());
    builder.queryParam(RecentChangesParameters.TITLE.value, options.title());
    builder.queryParam(RecentChangesParameters.CONTINUE.value, options.rccontinue());
    builder.queryParam(
        RecentChangesParameters.GENERATE_REVISIONS.value, options.generateRevisions());
    builder.queryParam(RecentChangesParameters.SLOT.value, options.slot());
  }

  private record Response(
      @JsonProperty("batchcomplete") boolean batchComplete,
      @JsonProperty("query") ResponseQuery query) {}

  private record ResponseQuery(
      @JsonProperty("errors") @JsonSetter(nulls = Nulls.AS_EMPTY) List<ApiError> errors,
      @JsonProperty("warnings") @JsonSetter(nulls = Nulls.AS_EMPTY) List<ApiError> warnings,
      @JsonProperty("docref") @Nullable String docref,
      @JsonProperty("recentchanges") @JsonSetter(nulls = Nulls.AS_EMPTY)
          List<RecentChange> recentChanges) {}
}
