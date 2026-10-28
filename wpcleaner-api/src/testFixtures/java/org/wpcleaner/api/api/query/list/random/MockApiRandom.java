package org.wpcleaner.api.api.query.list.random;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import org.jspecify.annotations.Nullable;
import org.springframework.test.web.client.MockRestServiceServer;
import org.wpcleaner.api.api.ApiAnswer;
import org.wpcleaner.api.api.ApiParameters;
import org.wpcleaner.api.api.ApiRequestExpectation;
import org.wpcleaner.api.api.Limit;
import org.wpcleaner.api.api.query.QueryParameters;
import org.wpcleaner.api.wiki.definition.WikiDefinition;

public final class MockApiRandom {

  private final MockRestServiceServer mediaWikiServer;

  public MockApiRandom(final MockRestServiceServer mediaWikiServer) {
    this.mediaWikiServer = mediaWikiServer;
  }

  public RandomAnswer givenRetrieveRandomPages(
      final WikiDefinition wiki, @Nullable final RandomQuery options) {
    final ApiRequestExpectation request =
        ApiRequestExpectation.of(mediaWikiServer, wiki, ApiParameters.Action.QUERY);
    request.queryParam(QueryParameters.LIST.value, QueryParameters.List.RANDOM.value);
    if (options != null) {
      expectOptions(request, options);
    }
    return new RandomAnswer(request.answer());
  }

  private static void expectOptions(
      final ApiRequestExpectation request, final RandomQuery options) {
    request.queryParam(
        RandomParameters.CONTENT_MODEL.value,
        options.contentModel(),
        contentModel -> contentModel.value);
    request.queryParam(
        RandomParameters.FILTER_REDIRECT.value,
        options.filterRedirect(),
        filterRedirect -> filterRedirect.value);
    request.queryParam(RandomParameters.LIMIT.value, options.limit(), Limit::value);
    request.queryParam(RandomParameters.MAX_SIZE.value, options.maxSize());
    request.queryParam(RandomParameters.MIN_SIZE.value, options.minSize());
    request.queryParamCollection(
        RandomParameters.NAMESPACE.value,
        options.namespace(),
        namespace -> Integer.toString(namespace.id()));
    request.queryParam(RandomParameters.CONTINUE.value, options.rnContinue());
  }

  public record RandomAnswer(ApiAnswer answer) {

    public void willReturn(final List<RandomPage> pages) {
      answer.willReturnQuery("random", pages);
    }

    public void willFail() {
      answer.willFail();
    }
  }
}
