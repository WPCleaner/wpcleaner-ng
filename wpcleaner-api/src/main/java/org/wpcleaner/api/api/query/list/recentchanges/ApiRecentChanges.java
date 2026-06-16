package org.wpcleaner.api.api.query.list.recentchanges;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import jakarta.annotation.Nullable;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriBuilder;
import org.wpcleaner.api.api.ApiError;
import org.wpcleaner.api.api.ApiParameters;
import org.wpcleaner.api.api.ApiRestClient;
import org.wpcleaner.api.api.ApiUtils;
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
    if (response == null || response.query() == null) {
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

  @SuppressWarnings({"PMD.CognitiveComplexity", "PMD.CyclomaticComplexity", "PMD.NPathComplexity"})
  private URI computeUri(final UriBuilder uriBuilder, @Nullable final RecentChangesQuery options) {
    final UriBuilder builder =
        ApiUtils.configure(uriBuilder, ApiParameters.Action.QUERY)
            .queryParam(QueryParameters.LIST.value, QueryParameters.List.RECENT_CHANGES.value);

    if (options == null) {
      return builder.build();
    }

    if (options.start() != null) {
      builder.queryParam(RecentChangesParameters.START.value, options.start().toString());
    }
    if (options.end() != null) {
      builder.queryParam(RecentChangesParameters.END.value, options.end().toString());
    }
    if (options.direction() != null) {
      builder.queryParam(RecentChangesParameters.DIRECTION.value, options.direction().value);
    }
    if (options.namespace() != null && !options.namespace().isEmpty()) {
      builder.queryParam(
          RecentChangesParameters.NAMESPACE.value,
          options.namespace().stream().map(Object::toString).collect(Collectors.joining("|")));
    }
    if (options.user() != null) {
      builder.queryParam(RecentChangesParameters.USER.value, options.user());
    }
    if (options.excludeUser() != null) {
      builder.queryParam(RecentChangesParameters.EXCLUDE_USER.value, options.excludeUser());
    }
    if (options.tag() != null) {
      builder.queryParam(RecentChangesParameters.TAG.value, options.tag());
    }
    if (options.properties() != null && !options.properties().isEmpty()) {
      builder.queryParam(
          RecentChangesParameters.PROPERTIES.value,
          options.properties().stream().map(p -> p.value).collect(Collectors.joining("|")));
    }
    if (options.show() != null && !options.show().isEmpty()) {
      builder.queryParam(
          RecentChangesParameters.SHOW.value,
          options.show().stream().map(s -> s.value).collect(Collectors.joining("|")));
    }
    if (options.limit() != null) {
      builder.queryParam(RecentChangesParameters.LIMIT.value, options.limit().toString());
    }
    if (options.type() != null && !options.type().isEmpty()) {
      builder.queryParam(
          RecentChangesParameters.TYPE.value,
          options.type().stream().map(t -> t.value).collect(Collectors.joining("|")));
    }
    if (options.topOnly() != null) {
      builder.queryParam(RecentChangesParameters.TOP_ONLY.value, options.topOnly().toString());
    }
    if (options.title() != null) {
      builder.queryParam(RecentChangesParameters.TITLE.value, options.title());
    }
    if (options.rccontinue() != null) {
      builder.queryParam(RecentChangesParameters.CONTINUE.value, options.rccontinue());
    }
    if (options.generateRevisions() != null) {
      builder.queryParam(
          RecentChangesParameters.GENERATE_REVISIONS.value, options.generateRevisions().toString());
    }
    if (options.slot() != null) {
      builder.queryParam(RecentChangesParameters.SLOT.value, options.slot());
    }
    return builder.build();
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
