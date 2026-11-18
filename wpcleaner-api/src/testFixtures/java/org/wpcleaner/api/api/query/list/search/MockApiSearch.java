package org.wpcleaner.api.api.query.list.search;

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

public final class MockApiSearch {

  private final MockRestServiceServer mediaWikiServer;

  public MockApiSearch(final MockRestServiceServer mediaWikiServer) {
    this.mediaWikiServer = mediaWikiServer;
  }

  public SearchAnswer givenSearchPages(
      final WikiDefinition wiki, final String search, @Nullable final SearchQuery options) {
    final ApiRequestExpectation request =
        ApiRequestExpectation.of(mediaWikiServer, wiki, ApiParameters.Action.QUERY);
    request.queryParam(QueryParameters.LIST.value, QueryParameters.List.SEARCH.value);
    request.queryParam(SearchParameters.SEARCH.value, search);
    if (options != null) {
      expectOptions(request, options);
    }
    return new SearchAnswer(request.answer());
  }

  private static void expectOptions(
      final ApiRequestExpectation request, final SearchQuery options) {
    request.queryParam(SearchParameters.ENABLE_REWRITES.value, options.enableRewrites());
    request.queryParamCollection(SearchParameters.INFO.value, options.info(), info -> info.value);
    request.queryParam(SearchParameters.INTERWIKI.value, options.interwiki());
    request.queryParam(SearchParameters.LIMIT.value, options.limit(), Limit::value);
    request.queryParamCollection(
        SearchParameters.NAMESPACE.value,
        options.namespace(),
        namespace -> Integer.toString(namespace.id()));
    request.queryParam(SearchParameters.OFFSET.value, options.offset());
    request.queryParamCollection(
        SearchParameters.PROPERTIES.value, options.properties(), properties -> properties.value);
    request.queryParam(
        SearchParameters.QUERY_DEPENDENT_PROFILE.value,
        options.queryDependentProfile(),
        profile -> profile.value);
    request.queryParam(
        SearchParameters.QUERY_INDEPENDENT_PROFILE.value,
        options.queryIndependentProfile(),
        profile -> profile.value);
    request.queryParam(SearchParameters.SORT.value, options.sort(), sort -> sort.value);
    request.queryParam(SearchParameters.WHAT.value, options.what(), what -> what.value);
  }

  public record SearchAnswer(ApiAnswer answer) {

    public void willReturn(final List<SearchPage> pages) {
      answer.willReturnQuery("search", pages.stream().map(SearchAnswer::createPage).toList());
    }

    public void willFail() {
      answer.willFail();
    }

    private static Map<String, @Nullable Object> createPage(final SearchPage page) {
      final Map<String, @Nullable Object> result = new LinkedHashMap<>();
      result.put("categorysnippet", page.categorySnippet());
      result.put("extensiondata", page.extensionData());
      result.put("isfilematch", page.isFileMatch());
      result.put("ns", page.ns());
      result.put("pageid", page.pageId());
      result.put("redirectsnippet", page.redirectSnippet());
      result.put("redirecttitle", page.redirectTitle());
      result.put("sectionsnippet", page.sectionSnippet());
      result.put("sectiontitle", page.sectionTitle());
      result.put("size", page.size());
      result.put("snippet", page.snippet());
      result.put("timestamp", formatInstant(page.timestamp()));
      result.put("title", page.title());
      result.put("titlesnippet", page.titleSnippet());
      result.put("wordcount", page.wordCount());
      return result;
    }

    @Nullable
    private static String formatInstant(@Nullable final Instant instant) {
      return instant == null ? null : instant.toString();
    }
  }
}
