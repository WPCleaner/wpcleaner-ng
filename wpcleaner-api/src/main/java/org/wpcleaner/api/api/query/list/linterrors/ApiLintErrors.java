package org.wpcleaner.api.api.query.list.linterrors;

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
import org.wpcleaner.api.api.Limit;
import org.wpcleaner.api.api.query.QueryParameters;
import org.wpcleaner.api.wiki.definition.WikiDefinition;

@Service
public class ApiLintErrors {

  private final ApiRestClient restClient;

  public ApiLintErrors(final ApiRestClient restClient) {
    this.restClient = restClient;
  }

  public List<PageLintError> retrieveLintErrors(
      final WikiDefinition wiki, final LintErrorsQuery query) {
    return ApiUtils.processApiResponse(
        internalRetrieveLintErrors(wiki, query),
        response ->
            Optional.ofNullable(response.query()).map(ResponseQuery::lintErrors).orElse(List.of()));
  }

  @Nullable
  private Response internalRetrieveLintErrors(
      final WikiDefinition wiki, final LintErrorsQuery query) {
    return restClient
        .getRestClient(wiki)
        .get()
        .uri(uriBuilder -> computeUri(uriBuilder, query))
        .retrieve()
        .body(Response.class);
  }

  private static URI computeUri(final UriBuilder uriBuilder, final LintErrorsQuery query) {
    final ApiUriBuilder builder = ApiUriBuilder.of(uriBuilder, ApiParameters.Action.QUERY);
    builder.queryParam(QueryParameters.LIST.value, QueryParameters.List.LINT_ERRORS.value);
    builder.queryParamCollection(LintErrorsParameters.CATEGORIES.value, query.categories());
    builder.queryParam(LintErrorsParameters.FROM.value, query.from());
    builder.queryParam(LintErrorsParameters.LIMIT.value, query.limit(), Limit::value);
    builder.queryParamCollection(
        LintErrorsParameters.NAMESPACE.value,
        query.namespace(),
        namespace -> Integer.toString(namespace.id()));
    builder.queryParamCollection(LintErrorsParameters.PAGE_ID.value, query.pageId());
    builder.queryParam(LintErrorsParameters.TITLE.value, query.title());
    return builder.build();
  }

  private record Response(
      @JsonProperty("batchcomplete") @Nullable Boolean batchComplete,
      @JsonProperty("continue") @Nullable ResponseContinue continueData,
      @JsonProperty("docref") @Nullable String docRef,
      @JsonProperty("errors") @JsonSetter(nulls = Nulls.AS_EMPTY) List<ApiError> errors,
      @JsonProperty("query") @Nullable ResponseQuery query,
      @JsonProperty("warnings") @JsonSetter(nulls = Nulls.AS_EMPTY) List<ApiError> warnings)
      implements ApiResponse {}

  private record ResponseContinue(@JsonProperty("lntfrom") @Nullable Long lntFrom) {}

  private record ResponseQuery(
      @JsonProperty("linterrors") @JsonSetter(nulls = Nulls.AS_EMPTY)
          List<PageLintError> lintErrors) {}
}
