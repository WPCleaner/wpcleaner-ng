package org.wpcleaner.api.api.query.prop.langlinks;

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
import org.wpcleaner.api.api.ApiRestClient;
import org.wpcleaner.api.api.ApiUriBuilder;
import org.wpcleaner.api.api.Limit;
import org.wpcleaner.api.api.query.QueryParameters;
import org.wpcleaner.api.wiki.definition.WikiDefinition;

@Service
public class ApiLanglinks {

  private final ApiRestClient restClient;

  public ApiLanglinks(final ApiRestClient restClient) {
    this.restClient = restClient;
  }

  public List<Page> retrieveLanglinksByPageId(
      final WikiDefinition wiki,
      final List<Integer> pageIds,
      @Nullable final LanglinksQuery options) {
    return Optional.ofNullable(internalRetrieveLanglinksByPageId(wiki, pageIds, options))
        .map(Response::query)
        .map(ResponseQuery::pages)
        .orElseGet(List::of);
  }

  public List<Page> retrieveLanglinksByTitle(
      final WikiDefinition wiki,
      final List<String> titles,
      @Nullable final LanglinksQuery options) {
    return Optional.ofNullable(internalRetrieveLanglinksByTitle(wiki, titles, options))
        .map(Response::query)
        .map(ResponseQuery::pages)
        .orElseGet(List::of);
  }

  @Nullable
  private Response internalRetrieveLanglinksByPageId(
      final WikiDefinition wiki,
      final List<Integer> pageIds,
      @Nullable final LanglinksQuery options) {
    return restClient
        .getRestClient(wiki)
        .get()
        .uri(uriBuilder -> computeUri(uriBuilder, QueryParameters.PAGE_IDS.value, pageIds, options))
        .retrieve()
        .body(Response.class);
  }

  @Nullable
  private Response internalRetrieveLanglinksByTitle(
      final WikiDefinition wiki,
      final List<String> titles,
      @Nullable final LanglinksQuery options) {
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
      @Nullable final LanglinksQuery options) {
    final ApiUriBuilder builder = ApiUriBuilder.of(uriBuilder, ApiParameters.Action.QUERY);
    builder.queryParam(
        QueryParameters.PROPERTIES.value, QueryParameters.Properties.LANGLINKS.value);
    builder.queryParamCollection(key, values);
    if (options != null) {
      computeOptions(builder, options);
    }
    return builder.build();
  }

  private void computeOptions(final ApiUriBuilder builder, final LanglinksQuery options) {
    builder.queryParam(LanglinksParameters.CONTINUE.value, options.llContinue());
    builder.queryParam(
        LanglinksParameters.DIRECTION.value, options.direction(), direction -> direction.value);
    builder.queryParam(LanglinksParameters.IN_LANGUAGE_CODE.value, options.inLanguageCode());
    builder.queryParam(LanglinksParameters.LANGUAGE.value, options.language());
    builder.queryParam(LanglinksParameters.LIMIT.value, options.limit(), Limit::value);
    builder.queryParamCollection(
        LanglinksParameters.PROPERTIES.value, options.properties(), properties -> properties.value);
    builder.queryParam(LanglinksParameters.TITLE.value, options.title());
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
