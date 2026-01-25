package org.wpcleaner.api.api.query.list.users;

/*
 * SPDX-FileCopyrightText: © 2025 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import jakarta.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.wpcleaner.api.api.ApiError;
import org.wpcleaner.api.api.ApiParameters;
import org.wpcleaner.api.api.ApiRestClient;
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
        .uri(
            uriBuilder ->
                uriBuilder
                    .queryParam(ApiParameters.ACTION.value, ApiParameters.Action.QUERY.value)
                    .queryParam(
                        ApiParameters.ERROR_FORMAT.value,
                        ApiParameters.ErrorFormat.PLAIN_TEXT.value)
                    .queryParam(ApiParameters.FORMAT.value, ApiParameters.Format.JSON.value)
                    .queryParam(
                        ApiParameters.FORMAT_VERSION.value, ApiParameters.FORMAT_VERSION_VALUE)
                    .queryParam(QueryParameters.LIST.value, QueryParameters.List.USERS.value)
                    .queryParam(
                        UsersParameters.PROPERTIES.value,
                        "%s|%s"
                            .formatted(
                                UsersParameters.Properties.GROUPS.value,
                                UsersParameters.Properties.RIGHTS.value))
                    .queryParam(UsersParameters.USERS.value, name)
                    .build())
        .retrieve()
        .body(Response.class);
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
