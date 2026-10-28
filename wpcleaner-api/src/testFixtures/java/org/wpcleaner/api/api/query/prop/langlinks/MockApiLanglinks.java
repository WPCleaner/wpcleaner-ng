package org.wpcleaner.api.api.query.prop.langlinks;

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

public final class MockApiLanglinks {

  private final MockRestServiceServer mediaWikiServer;

  public MockApiLanglinks(final MockRestServiceServer mediaWikiServer) {
    this.mediaWikiServer = mediaWikiServer;
  }

  public LanglinksAnswer givenRetrieveLanglinksByTitle(
      final WikiDefinition wiki,
      final List<String> titles,
      @Nullable final LanglinksQuery options) {
    final ApiRequestExpectation request =
        ApiRequestExpectation.of(mediaWikiServer, wiki, ApiParameters.Action.QUERY);
    request.queryParam(
        QueryParameters.PROPERTIES.value, QueryParameters.Properties.LANGLINKS.value);
    request.queryParamCollection(QueryParameters.TITLES.value, titles);
    if (options != null) {
      expectOptions(request, options);
    }
    return new LanglinksAnswer(request.answer());
  }

  private static void expectOptions(
      final ApiRequestExpectation request, final LanglinksQuery options) {
    request.queryParam(LanglinksParameters.CONTINUE.value, options.llContinue());
    request.queryParam(
        LanglinksParameters.DIRECTION.value, options.direction(), direction -> direction.value);
    request.queryParam(LanglinksParameters.IN_LANGUAGE_CODE.value, options.inLanguageCode());
    request.queryParam(LanglinksParameters.LANGUAGE.value, options.language());
    request.queryParam(LanglinksParameters.LIMIT.value, options.limit(), Limit::value);
    request.queryParamCollection(
        LanglinksParameters.PROPERTIES.value, options.properties(), properties -> properties.value);
    request.queryParam(LanglinksParameters.TITLE.value, options.title());
  }

  public record LanglinksAnswer(ApiAnswer answer) {

    public void willReturn(final List<Page> pages) {
      answer.willReturnQuery("pages", pages.stream().map(LanglinksAnswer::createPage).toList());
    }

    public void willFail() {
      answer.willFail();
    }

    private static Map<String, @Nullable Object> createPage(final Page page) {
      final Map<String, @Nullable Object> result = new LinkedHashMap<>();
      result.put("pageid", page.pageId());
      result.put("ns", page.ns());
      result.put("title", page.title());
      result.put("missing", page.missing() ? Boolean.TRUE : null);
      result.put("langlinks", page.langlinks().isEmpty() ? null : page.langlinks());
      return result;
    }
  }
}
