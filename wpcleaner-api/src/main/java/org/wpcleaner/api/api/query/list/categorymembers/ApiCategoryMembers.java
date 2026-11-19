package org.wpcleaner.api.api.query.list.categorymembers;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
public class ApiCategoryMembers {

  private final ApiRestClient restClient;

  public ApiCategoryMembers(final ApiRestClient restClient) {
    this.restClient = restClient;
  }

  public List<CategoryMember> retrieveCategoryMembersByPageId(
      final WikiDefinition wiki, final int pageId, @Nullable final CategoryMembersQuery options) {
    return retrieveCategoryMembers(
        wiki, CategoryMembersParameters.PAGE_ID.value, Integer.toString(pageId), options);
  }

  public List<CategoryMember> retrieveCategoryMembersByTitle(
      final WikiDefinition wiki, final String title, @Nullable final CategoryMembersQuery options) {
    return retrieveCategoryMembers(wiki, CategoryMembersParameters.TITLE.value, title, options);
  }

  private List<CategoryMember> retrieveCategoryMembers(
      final WikiDefinition wiki,
      final String key,
      final String value,
      @Nullable final CategoryMembersQuery options) {
    final boolean followContinuation = options == null || options.cmContinue() == null;
    final List<CategoryMember> members = new ArrayList<>();
    Map<String, String> continuation = Map.of();
    do {
      final Map<String, String> requestContinuation = continuation;
      final Response response =
          ApiUtils.processApiResponse(
              restClient
                  .getRestClient(wiki)
                  .get()
                  .uri(
                      uriBuilder ->
                          computeUri(uriBuilder, key, value, options, requestContinuation))
                  .retrieve()
                  .body(Response.class),
              apiResponse -> apiResponse);
      members.addAll(
          Optional.ofNullable(response.query())
              .map(ResponseQuery::categoryMembers)
              .orElseGet(List::of));
      continuation =
          Optional.ofNullable(response.continuation())
              .filter(_ -> followContinuation)
              .orElseGet(Map::of);
    } while (!continuation.isEmpty());
    return List.copyOf(members);
  }

  private static URI computeUri(
      final UriBuilder uriBuilder,
      final String key,
      final String value,
      @Nullable final CategoryMembersQuery options,
      final Map<String, String> continuation) {
    final ApiUriBuilder builder = ApiUriBuilder.of(uriBuilder, ApiParameters.Action.QUERY);
    builder.queryParam(QueryParameters.LIST.value, QueryParameters.List.CATEGORY_MEMBERS.value);
    builder.queryParam(key, value);
    if (options != null) {
      computeOptions(builder, options);
    }
    continuation.forEach(builder::queryParam);
    return builder.build();
  }

  private static void computeOptions(
      final ApiUriBuilder builder, final CategoryMembersQuery options) {
    builder.queryParam(CategoryMembersParameters.CONTINUE.value, options.cmContinue());
    builder.queryParam(
        CategoryMembersParameters.DIRECTION.value,
        options.direction(),
        direction -> direction.value);
    builder.queryParam(CategoryMembersParameters.END.value, options.end());
    builder.queryParam(CategoryMembersParameters.END_HEX_SORT_KEY.value, options.endHexSortKey());
    builder.queryParam(
        CategoryMembersParameters.END_SORT_KEY_PREFIX.value, options.endSortKeyPrefix());
    builder.queryParam(CategoryMembersParameters.LIMIT.value, options.limit(), Limit::value);
    builder.queryParamCollection(
        CategoryMembersParameters.NAMESPACE.value,
        options.namespace(),
        namespace -> Integer.toString(namespace.id()));
    builder.queryParamCollection(
        CategoryMembersParameters.PROPERTIES.value,
        options.properties(),
        properties -> properties.value);
    builder.queryParam(CategoryMembersParameters.SORT.value, options.sort(), sort -> sort.value);
    builder.queryParam(CategoryMembersParameters.START.value, options.start());
    builder.queryParam(
        CategoryMembersParameters.START_HEX_SORT_KEY.value, options.startHexSortKey());
    builder.queryParam(
        CategoryMembersParameters.START_SORT_KEY_PREFIX.value, options.startSortKeyPrefix());
    builder.queryParamCollection(
        CategoryMembersParameters.TYPE.value, options.type(), type -> type.value);
  }

  private record Response(
      @JsonProperty("batchcomplete") @Nullable Boolean batchComplete,
      @JsonProperty("continue") @Nullable Map<String, String> continuation,
      @JsonProperty("docref") @Nullable String docRef,
      @JsonProperty("errors") @JsonSetter(nulls = Nulls.AS_EMPTY) List<ApiError> errors,
      @JsonProperty("query") @Nullable ResponseQuery query,
      @JsonProperty("warnings") @JsonSetter(nulls = Nulls.AS_EMPTY) List<ApiError> warnings)
      implements ApiResponse {}

  private record ResponseQuery(
      @JsonProperty("categorymembers") @JsonSetter(nulls = Nulls.AS_EMPTY)
          List<CategoryMember> categoryMembers) {}
}
