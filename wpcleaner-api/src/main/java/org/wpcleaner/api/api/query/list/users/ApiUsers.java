package org.wpcleaner.api.api.query.list.users;

/*
 * SPDX-FileCopyrightText: © 2025 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
public class ApiUsers {

  private final ApiRestClient restClient;

  public ApiUsers(final ApiRestClient restClient) {
    this.restClient = restClient;
  }

  public User retrieveUser(final WikiDefinition wiki, final String name) {
    return Optional.ofNullable(internalRetrieveUser(wiki, name))
        .map(Response::query)
        .map(ResponseQuery::users)
        .map(list -> list.isEmpty() ? null : list.getFirst())
        .orElseGet(() -> User.ofMissing(name));
  }

  @Nullable
  private Response internalRetrieveUser(final WikiDefinition wiki, final String name) {
    return restClient
        .getRestClient(wiki)
        .get()
        .uri(uriBuilder -> computeUri(uriBuilder, name))
        .retrieve()
        .body(Response.class);
  }

  private URI computeUri(final UriBuilder uriBuilder, final String name) {
    final ApiUriBuilder builder = ApiUriBuilder.of(uriBuilder, ApiParameters.Action.QUERY);
    builder.queryParam(QueryParameters.LIST.value, QueryParameters.List.USERS.value);
    builder.queryParamCollection(
        QueryParameters.PROPERTIES.value,
        Set.of(UsersParameters.Properties.GROUPS.value, UsersParameters.Properties.RIGHTS.value));
    builder.queryParam(UsersParameters.USERS.value, name);
    return builder.build();
  }

  private record Response(
      @JsonProperty("batchcomplete") boolean batchComplete,
      @JsonProperty("query") ResponseQuery query) {}

  private record ResponseQuery(
      @JsonProperty("errors") @JsonSetter(nulls = Nulls.AS_EMPTY) List<ApiError> errors,
      @JsonProperty("warnings") @JsonSetter(nulls = Nulls.AS_EMPTY) List<ApiError> warnings,
      @JsonProperty("docref") @Nullable String docref,
      @JsonProperty("users") List<User> users) {}
}
