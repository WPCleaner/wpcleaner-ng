package org.wpcleaner.api.api.query.list.categorymembers;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.Nullable;
import org.springframework.test.web.client.MockRestServiceServer;
import org.wpcleaner.api.api.ApiAnswer;
import org.wpcleaner.api.api.ApiParameters;
import org.wpcleaner.api.api.ApiRequestExpectation;
import org.wpcleaner.api.api.Limit;
import org.wpcleaner.api.api.query.QueryParameters;
import org.wpcleaner.api.wiki.definition.WikiDefinition;

public final class MockApiCategoryMembers {

  private final MockRestServiceServer mediaWikiServer;

  public MockApiCategoryMembers(final MockRestServiceServer mediaWikiServer) {
    this.mediaWikiServer = mediaWikiServer;
  }

  public CategoryMembersAnswer givenRetrieveCategoryMembersByPageId(
      final WikiDefinition wiki, final int pageId, @Nullable final CategoryMembersQuery options) {
    return givenRetrieveCategoryMembers(
        wiki, CategoryMembersParameters.PAGE_ID.value, Integer.toString(pageId), options);
  }

  public CategoryMembersAnswer givenRetrieveCategoryMembersByTitle(
      final WikiDefinition wiki, final String title, @Nullable final CategoryMembersQuery options) {
    return givenRetrieveCategoryMembers(
        wiki, CategoryMembersParameters.TITLE.value, title, options);
  }

  private CategoryMembersAnswer givenRetrieveCategoryMembers(
      final WikiDefinition wiki,
      final String key,
      final String value,
      @Nullable final CategoryMembersQuery options) {
    final ApiRequestExpectation request =
        ApiRequestExpectation.of(mediaWikiServer, wiki, ApiParameters.Action.QUERY);
    request.queryParam(QueryParameters.LIST.value, QueryParameters.List.CATEGORY_MEMBERS.value);
    request.queryParam(key, value);
    if (options != null) {
      expectOptions(request, options);
    }
    return new CategoryMembersAnswer(request.answer());
  }

  private static void expectOptions(
      final ApiRequestExpectation request, final CategoryMembersQuery options) {
    request.queryParam(CategoryMembersParameters.CONTINUE.value, options.cmContinue());
    request.queryParam(
        CategoryMembersParameters.DIRECTION.value,
        options.direction(),
        direction -> direction.value);
    request.queryParam(CategoryMembersParameters.END.value, options.end());
    request.queryParam(CategoryMembersParameters.END_HEX_SORT_KEY.value, options.endHexSortKey());
    request.queryParam(
        CategoryMembersParameters.END_SORT_KEY_PREFIX.value, options.endSortKeyPrefix());
    request.queryParam(CategoryMembersParameters.LIMIT.value, options.limit(), Limit::value);
    request.queryParamCollection(
        CategoryMembersParameters.NAMESPACE.value,
        options.namespace(),
        namespace -> Integer.toString(namespace.id()));
    request.queryParamCollection(
        CategoryMembersParameters.PROPERTIES.value,
        options.properties(),
        properties -> properties.value);
    request.queryParam(CategoryMembersParameters.SORT.value, options.sort(), sort -> sort.value);
    request.queryParam(CategoryMembersParameters.START.value, options.start());
    request.queryParam(
        CategoryMembersParameters.START_HEX_SORT_KEY.value, options.startHexSortKey());
    request.queryParam(
        CategoryMembersParameters.START_SORT_KEY_PREFIX.value, options.startSortKeyPrefix());
    request.queryParamCollection(
        CategoryMembersParameters.TYPE.value, options.type(), type -> type.value);
  }

  public record CategoryMembersAnswer(ApiAnswer answer) {

    public void willReturn(final List<CategoryMember> members) {
      answer.willReturnQuery(
          "categorymembers", members.stream().map(CategoryMembersAnswer::createMember).toList());
    }

    public void willFail() {
      answer.willFail();
    }

    private static Map<String, @Nullable Object> createMember(final CategoryMember member) {
      final Map<String, @Nullable Object> result = new LinkedHashMap<>();
      result.put("ns", member.ns());
      result.put("pageid", member.pageId());
      result.put("sortkey", member.sortKey());
      result.put("sortkeyprefix", member.sortKeyPrefix());
      result.put("timestamp", formatInstant(member.timestamp()));
      result.put("title", member.title());
      result.put("type", member.type());
      return result;
    }

    @Nullable
    private static String formatInstant(@Nullable final Instant instant) {
      return instant == null ? null : instant.toString();
    }
  }
}
