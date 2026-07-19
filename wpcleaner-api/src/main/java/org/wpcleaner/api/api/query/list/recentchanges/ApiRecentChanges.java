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
import org.wpcleaner.api.api.ApiUtils;
import org.wpcleaner.api.api.UriBuilderUtils;
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
    final UriBuilder builder =
        ApiUtils.configure(uriBuilder, ApiParameters.Action.QUERY)
            .queryParam(QueryParameters.LIST.value, QueryParameters.List.RECENT_CHANGES.value);
    if (options != null) {
      computeOptions(builder, options);
    }
    return builder.build();
  }

  private void computeOptions(final UriBuilder builder, final RecentChangesQuery options) {
    UriBuilderUtils.queryParam(builder, RecentChangesParameters.START.value, options.start());
    UriBuilderUtils.queryParam(builder, RecentChangesParameters.END.value, options.end());
    UriBuilderUtils.queryParam(
        builder,
        RecentChangesParameters.DIRECTION.value,
        options.direction(),
        direction -> direction.value);
    UriBuilderUtils.queryParamCollection(
        builder, RecentChangesParameters.NAMESPACE.value, options.namespace());
    UriBuilderUtils.queryParam(builder, RecentChangesParameters.USER.value, options.user());
    UriBuilderUtils.queryParam(
        builder, RecentChangesParameters.EXCLUDE_USER.value, options.excludeUser());
    UriBuilderUtils.queryParam(builder, RecentChangesParameters.TAG.value, options.tag());
    UriBuilderUtils.queryParamCollection(
        builder,
        RecentChangesParameters.PROPERTIES.value,
        options.properties(),
        properties -> properties.value);
    UriBuilderUtils.queryParamCollection(
        builder, RecentChangesParameters.SHOW.value, options.show(), show -> show.value);
    UriBuilderUtils.queryParam(builder, RecentChangesParameters.LIMIT.value, options.limit());
    UriBuilderUtils.queryParamCollection(
        builder, RecentChangesParameters.TYPE.value, options.type(), type -> type.value);
    UriBuilderUtils.queryParam(builder, RecentChangesParameters.TOP_ONLY.value, options.topOnly());
    UriBuilderUtils.queryParam(builder, RecentChangesParameters.TITLE.value, options.title());
    UriBuilderUtils.queryParam(
        builder, RecentChangesParameters.CONTINUE.value, options.rccontinue());
    UriBuilderUtils.queryParam(
        builder, RecentChangesParameters.GENERATE_REVISIONS.value, options.generateRevisions());
    UriBuilderUtils.queryParam(builder, RecentChangesParameters.SLOT.value, options.slot());
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
