package org.wpcleaner.api.api.query.prop.links;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

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

public final class MockApiLinks {

  private final MockRestServiceServer mediaWikiServer;

  public MockApiLinks(final MockRestServiceServer mediaWikiServer) {
    this.mediaWikiServer = mediaWikiServer;
  }

  public LinksAnswer givenRetrieveLinksByTitle(
      final WikiDefinition wiki, final List<String> titles, @Nullable final LinksQuery options) {
    return givenRetrieveLinksByTitle(wiki, titles, options, Map.of());
  }

  public LinksAnswer givenRetrieveLinksByTitle(
      final WikiDefinition wiki,
      final List<String> titles,
      @Nullable final LinksQuery options,
      final Map<String, String> continuation) {
    return givenRetrieveLinks(wiki, QueryParameters.TITLES.value, titles, options, continuation);
  }

  public LinksAnswer givenRetrieveLinksByPageId(
      final WikiDefinition wiki, final List<Integer> pageIds, @Nullable final LinksQuery options) {
    return givenRetrieveLinks(wiki, QueryParameters.PAGE_IDS.value, pageIds, options, Map.of());
  }

  private <T> LinksAnswer givenRetrieveLinks(
      final WikiDefinition wiki,
      final String key,
      final List<T> values,
      @Nullable final LinksQuery options,
      final Map<String, String> continuation) {
    final ApiRequestExpectation request =
        ApiRequestExpectation.of(mediaWikiServer, wiki, ApiParameters.Action.QUERY);
    request.queryParam(QueryParameters.PROPERTIES.value, QueryParameters.Properties.LINKS.value);
    request.queryParamCollection(key, values);
    if (options != null) {
      expectOptions(request, options);
    }
    continuation.forEach(request::queryParam);
    return new LinksAnswer(request.answer());
  }

  private static void expectOptions(final ApiRequestExpectation request, final LinksQuery options) {
    request.queryParam(LinksParameters.CONTINUE.value, options.plContinue());
    request.queryParam(
        LinksParameters.DIRECTION.value, options.direction(), direction -> direction.value);
    request.queryParam(LinksParameters.LIMIT.value, options.limit(), Limit::value);
    request.queryParamCollection(
        LinksParameters.NAMESPACE.value,
        options.namespace(),
        namespace -> Integer.toString(namespace.id()));
    request.queryParamCollection(LinksParameters.TITLES.value, options.titles());
  }

  public record LinksAnswer(ApiAnswer answer) {

    public void willReturn(final List<Page> pages) {
      answer.willReturnQuery("pages", createPages(pages));
    }

    public void willReturn(final List<Page> pages, final Map<String, String> continuation) {
      answer.willReturn(
          Map.of("continue", continuation, "query", Map.of("pages", createPages(pages))));
    }

    public void willFail() {
      answer.willFail();
    }

    private static List<Map<String, @Nullable Object>> createPages(final List<Page> pages) {
      return pages.stream().map(LinksAnswer::createPage).toList();
    }

    private static Map<String, @Nullable Object> createPage(final Page page) {
      final Map<String, @Nullable Object> result = new LinkedHashMap<>();
      result.put("pageid", page.pageId());
      result.put("ns", page.ns());
      result.put("title", page.title());
      result.put("missing", page.missing() ? Boolean.TRUE : null);
      result.put("links", page.links().isEmpty() ? null : page.links());
      return result;
    }
  }
}
