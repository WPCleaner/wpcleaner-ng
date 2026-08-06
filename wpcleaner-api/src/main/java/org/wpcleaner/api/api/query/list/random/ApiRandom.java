package org.wpcleaner.api.api.query.list.random;

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
import org.wpcleaner.api.api.Limit;
import org.wpcleaner.api.api.query.QueryParameters;
import org.wpcleaner.api.wiki.definition.WikiDefinition;

@Service
public class ApiRandom {

  private final ApiRestClient restClient;

  public ApiRandom(final ApiRestClient restClient) {
    this.restClient = restClient;
  }

  public List<RandomPage> retrieveRandomPages(
      final WikiDefinition wiki, @Nullable final RandomQuery options) {
    final Response response = internalRetrieveRandomPages(wiki, options);
    if (response == null) {
      return List.of();
    }
    return response.query().random();
  }

  @Nullable
  private Response internalRetrieveRandomPages(
      final WikiDefinition wiki, @Nullable final RandomQuery options) {
    return restClient
        .getRestClient(wiki)
        .get()
        .uri(uriBuilder -> computeUri(uriBuilder, options))
        .retrieve()
        .body(Response.class);
  }

  private URI computeUri(final UriBuilder uriBuilder, @Nullable final RandomQuery options) {
    final ApiUriBuilder builder = ApiUriBuilder.of(uriBuilder, ApiParameters.Action.QUERY);
    builder.queryParam(QueryParameters.LIST.value, QueryParameters.List.RANDOM.value);
    if (options != null) {
      computeOptions(builder, options);
    }
    return builder.build();
  }

  private void computeOptions(final ApiUriBuilder builder, final RandomQuery options) {
    builder.queryParam(
        RandomParameters.CONTENT_MODEL.value,
        options.contentModel(),
        contentModel -> contentModel.value);
    builder.queryParam(
        RandomParameters.FILTER_REDIRECT.value,
        options.filterRedirect(),
        filterRedirect -> filterRedirect.value);
    builder.queryParam(RandomParameters.LIMIT.value, options.limit(), Limit::value);
    builder.queryParam(RandomParameters.MAX_SIZE.value, options.maxSize());
    builder.queryParam(RandomParameters.MIN_SIZE.value, options.minSize());
    builder.queryParamCollection(
        RandomParameters.NAMESPACE.value,
        options.namespace(),
        namespace -> Integer.toString(namespace.id()));
    builder.queryParam(RandomParameters.CONTINUE.value, options.rnContinue());
  }

  private record Response(
      @JsonProperty("batchcomplete") boolean batchComplete,
      @JsonProperty("continue") @Nullable ResponseContinue continueData,
      @JsonProperty("query") ResponseQuery query) {}

  private record ResponseContinue(@JsonProperty("rncontinue") String rnContinue) {}

  private record ResponseQuery(
      @JsonProperty("docref") @Nullable String docref,
      @JsonProperty("errors") @JsonSetter(nulls = Nulls.AS_EMPTY) List<ApiError> errors,
      @JsonProperty("random") @JsonSetter(nulls = Nulls.AS_EMPTY) List<RandomPage> random,
      @JsonProperty("warnings") @JsonSetter(nulls = Nulls.AS_EMPTY) List<ApiError> warnings) {}
}
