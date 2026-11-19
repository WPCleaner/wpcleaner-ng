package org.wpcleaner.api.api.query.prop.linkshere;

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

public final class MockApiLinksHere {

  private final MockRestServiceServer mediaWikiServer;

  public MockApiLinksHere(final MockRestServiceServer mediaWikiServer) {
    this.mediaWikiServer = mediaWikiServer;
  }

  public LinksHereAnswer givenRetrieveLinksHereByTitle(
      final WikiDefinition wiki,
      final List<String> titles,
      @Nullable final LinksHereQuery options) {
    return givenRetrieveLinksHereByTitle(wiki, titles, options, Map.of());
  }

  public LinksHereAnswer givenRetrieveLinksHereByTitle(
      final WikiDefinition wiki,
      final List<String> titles,
      @Nullable final LinksHereQuery options,
      final Map<String, String> continuation) {
    return givenRetrieveLinksHere(
        wiki, QueryParameters.TITLES.value, titles, options, continuation);
  }

  public LinksHereAnswer givenRetrieveLinksHereByPageId(
      final WikiDefinition wiki,
      final List<Integer> pageIds,
      @Nullable final LinksHereQuery options) {
    return givenRetrieveLinksHere(wiki, QueryParameters.PAGE_IDS.value, pageIds, options, Map.of());
  }

  private <T> LinksHereAnswer givenRetrieveLinksHere(
      final WikiDefinition wiki,
      final String key,
      final List<T> values,
      @Nullable final LinksHereQuery options,
      final Map<String, String> continuation) {
    final ApiRequestExpectation request =
        ApiRequestExpectation.of(mediaWikiServer, wiki, ApiParameters.Action.QUERY);
    request.queryParam(
        QueryParameters.PROPERTIES.value, QueryParameters.Properties.LINKS_HERE.value);
    request.queryParamCollection(key, values);
    if (options != null) {
      expectOptions(request, options);
    }
    continuation.forEach(request::queryParam);
    return new LinksHereAnswer(request.answer());
  }

  private static void expectOptions(
      final ApiRequestExpectation request, final LinksHereQuery options) {
    request.queryParam(LinksHereParameters.CONTINUE.value, options.lhContinue());
    request.queryParam(LinksHereParameters.LIMIT.value, options.limit(), Limit::value);
    request.queryParamCollection(
        LinksHereParameters.NAMESPACE.value,
        options.namespace(),
        namespace -> Integer.toString(namespace.id()));
    request.queryParamCollection(
        LinksHereParameters.PROPERTIES.value, options.properties(), property -> property.value);
    request.queryParamCollection(
        LinksHereParameters.SHOW.value, options.show(), show -> show.value);
  }

  public record LinksHereAnswer(ApiAnswer answer) {

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
      return pages.stream().map(LinksHereAnswer::createPage).toList();
    }

    private static Map<String, @Nullable Object> createPage(final Page page) {
      final Map<String, @Nullable Object> result = new LinkedHashMap<>();
      result.put("pageid", page.pageId());
      result.put("ns", page.ns());
      result.put("title", page.title());
      result.put("missing", page.missing() ? Boolean.TRUE : null);
      result.put("linkshere", page.linksHere().isEmpty() ? null : page.linksHere());
      return result;
    }
  }
}
