package org.wpcleaner.api.api.query.meta.allmessages;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import java.net.URI;
import java.util.List;
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
import org.wpcleaner.api.api.query.QueryParameters;
import org.wpcleaner.api.wiki.definition.WikiDefinition;

@Service
public class ApiAllMessages {

  private static final String SEPARATOR = "|";

  private final ApiRestClient restClient;

  public ApiAllMessages(final ApiRestClient restClient) {
    this.restClient = restClient;
  }

  public List<Message> retrieveMessages(final WikiDefinition wiki, final AllMessagesQuery query) {
    return ApiUtils.processApiResponse(
        internalRetrieveMessages(wiki, query),
        response ->
            Optional.ofNullable(response.query())
                .map(ResponseQuery::allMessages)
                .orElse(List.of()));
  }

  @Nullable
  private Response internalRetrieveMessages(
      final WikiDefinition wiki, final AllMessagesQuery query) {
    return restClient
        .getRestClient(wiki)
        .get()
        .uri(uriBuilder -> computeUri(uriBuilder, query))
        .retrieve()
        .body(Response.class);
  }

  private static URI computeUri(final UriBuilder uriBuilder, final AllMessagesQuery query) {
    final ApiUriBuilder builder = ApiUriBuilder.of(uriBuilder, ApiParameters.Action.QUERY);
    builder.queryParam(QueryParameters.META.value, QueryParameters.Meta.ALL_MESSAGES.value);
    builder.queryParam(
        AllMessagesParameters.ARGS.value, query.args(), args -> String.join(SEPARATOR, args));
    builder.queryParam(
        AllMessagesParameters.CUSTOMISED.value, query.customised(), customised -> customised.value);
    builder.queryParam(AllMessagesParameters.ENABLE_PARSER.value, query.enableParser());
    builder.queryParam(AllMessagesParameters.FILTER.value, query.filter());
    builder.queryParam(AllMessagesParameters.FROM.value, query.from());
    builder.queryParam(AllMessagesParameters.INCLUDE_LOCAL.value, query.includeLocal());
    builder.queryParam(AllMessagesParameters.LANGUAGE.value, query.language());
    builder.queryParamCollection(AllMessagesParameters.MESSAGES.value, query.messages());
    builder.queryParam(AllMessagesParameters.NO_CONTENT.value, query.noContent());
    builder.queryParam(AllMessagesParameters.PREFIX.value, query.prefix());
    builder.queryParamCollection(
        AllMessagesParameters.PROPERTIES.value, query.properties(), property -> property.value);
    builder.queryParam(AllMessagesParameters.TITLE.value, query.title());
    builder.queryParam(AllMessagesParameters.TO.value, query.to());
    return builder.build();
  }

  private record Response(
      @JsonProperty("batchcomplete") @Nullable Boolean batchComplete,
      @JsonProperty("docref") @Nullable String docRef,
      @JsonProperty("errors") @JsonSetter(nulls = Nulls.AS_EMPTY) List<ApiError> errors,
      @JsonProperty("query") @Nullable ResponseQuery query,
      @JsonProperty("warnings") @JsonSetter(nulls = Nulls.AS_EMPTY) List<ApiError> warnings)
      implements ApiResponse {}

  private record ResponseQuery(
      @JsonProperty("allmessages") @JsonSetter(nulls = Nulls.AS_EMPTY) List<Message> allMessages) {}
}
