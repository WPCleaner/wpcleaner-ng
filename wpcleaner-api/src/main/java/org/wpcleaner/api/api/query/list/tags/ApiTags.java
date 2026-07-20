package org.wpcleaner.api.api.query.list.tags;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import java.net.URI;
import java.util.Collection;
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
public class ApiTags {

  private final ApiRestClient restClient;

  public ApiTags(final ApiRestClient restClient) {
    this.restClient = restClient;
  }

  public List<Tag> retrieveTags(
      final WikiDefinition wiki,
      @Nullable final String limit,
      @Nullable final String tgcontinue,
      @Nullable final Collection<TagsParameters.Properties> properties) {
    final Response response = internalRetrieveTags(wiki, limit, tgcontinue, properties);
    if (response == null) {
      return List.of();
    }
    return response.query().tags();
  }

  @Nullable
  private Response internalRetrieveTags(
      final WikiDefinition wiki,
      @Nullable final String limit,
      @Nullable final String tgcontinue,
      @Nullable final Collection<TagsParameters.Properties> properties) {
    return restClient
        .getRestClient(wiki)
        .get()
        .uri(uriBuilder -> computeUri(uriBuilder, limit, tgcontinue, properties))
        .retrieve()
        .body(Response.class);
  }

  private URI computeUri(
      final UriBuilder uriBuilder,
      @Nullable final String limit,
      @Nullable final String tgcontinue,
      @Nullable final Collection<TagsParameters.Properties> properties) {
    final ApiUriBuilder builder = ApiUriBuilder.of(uriBuilder, ApiParameters.Action.QUERY);
    builder.queryParam(QueryParameters.LIST.value, QueryParameters.List.TAGS.value);
    builder.queryParam(TagsParameters.LIMIT.value, limit);
    builder.queryParam(TagsParameters.CONTINUE.value, tgcontinue);
    builder.queryParamCollection(TagsParameters.PROPERTIES.value, properties, prop -> prop.value);
    return builder.build();
  }

  private record Response(
      @JsonProperty("batchcomplete") boolean batchComplete,
      @JsonProperty("query") ResponseQuery query) {}

  private record ResponseQuery(
      @JsonProperty("docref") @Nullable String docref,
      @JsonProperty("errors") @JsonSetter(nulls = Nulls.AS_EMPTY) List<ApiError> errors,
      @JsonProperty("tags") @JsonSetter(nulls = Nulls.AS_EMPTY) List<Tag> tags,
      @JsonProperty("warnings") @JsonSetter(nulls = Nulls.AS_EMPTY) List<ApiError> warnings) {}
}
