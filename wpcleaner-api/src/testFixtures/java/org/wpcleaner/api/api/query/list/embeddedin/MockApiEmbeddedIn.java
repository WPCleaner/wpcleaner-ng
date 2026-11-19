package org.wpcleaner.api.api.query.list.embeddedin;

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

public final class MockApiEmbeddedIn {

  private final MockRestServiceServer mediaWikiServer;

  public MockApiEmbeddedIn(final MockRestServiceServer mediaWikiServer) {
    this.mediaWikiServer = mediaWikiServer;
  }

  public EmbeddedInAnswer givenRetrieveEmbeddedInByPageId(
      final WikiDefinition wiki, final int pageId, @Nullable final EmbeddedInQuery options) {
    return givenRetrieveEmbeddedIn(
        wiki, EmbeddedInParameters.PAGE_ID.value, Integer.toString(pageId), options);
  }

  public EmbeddedInAnswer givenRetrieveEmbeddedInByTitle(
      final WikiDefinition wiki, final String title, @Nullable final EmbeddedInQuery options) {
    return givenRetrieveEmbeddedIn(wiki, EmbeddedInParameters.TITLE.value, title, options);
  }

  private EmbeddedInAnswer givenRetrieveEmbeddedIn(
      final WikiDefinition wiki,
      final String key,
      final String value,
      @Nullable final EmbeddedInQuery options) {
    final ApiRequestExpectation request =
        ApiRequestExpectation.of(mediaWikiServer, wiki, ApiParameters.Action.QUERY);
    request.queryParam(QueryParameters.LIST.value, QueryParameters.List.EMBEDDED_IN.value);
    request.queryParam(key, value);
    if (options != null) {
      expectOptions(request, options);
    }
    return new EmbeddedInAnswer(request.answer());
  }

  private static void expectOptions(
      final ApiRequestExpectation request, final EmbeddedInQuery options) {
    request.queryParam(EmbeddedInParameters.CONTINUE.value, options.eiContinue());
    request.queryParam(
        EmbeddedInParameters.DIRECTION.value, options.direction(), direction -> direction.value);
    request.queryParam(
        EmbeddedInParameters.FILTER_REDIRECT.value,
        options.filterRedirect(),
        filterRedirect -> filterRedirect.value);
    request.queryParam(EmbeddedInParameters.LIMIT.value, options.limit(), Limit::value);
    request.queryParamCollection(
        EmbeddedInParameters.NAMESPACE.value,
        options.namespace(),
        namespace -> Integer.toString(namespace.id()));
  }

  public record EmbeddedInAnswer(ApiAnswer answer) {

    public void willReturn(final List<EmbeddingPage> pages) {
      answer.willReturnQuery(
          "embeddedin", pages.stream().map(EmbeddedInAnswer::createPage).toList());
    }

    public void willFail() {
      answer.willFail();
    }

    private static Map<String, @Nullable Object> createPage(final EmbeddingPage page) {
      final Map<String, @Nullable Object> result = new LinkedHashMap<>();
      result.put("ns", page.ns());
      result.put("pageid", page.pageId());
      result.put("title", page.title());
      return result;
    }
  }
}
