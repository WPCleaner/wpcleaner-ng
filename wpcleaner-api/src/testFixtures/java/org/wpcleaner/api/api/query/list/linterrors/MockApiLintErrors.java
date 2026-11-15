package org.wpcleaner.api.api.query.list.linterrors;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import org.springframework.test.web.client.MockRestServiceServer;
import org.wpcleaner.api.api.ApiAnswer;
import org.wpcleaner.api.api.ApiError;
import org.wpcleaner.api.api.ApiParameters;
import org.wpcleaner.api.api.ApiRequestExpectation;
import org.wpcleaner.api.api.Limit;
import org.wpcleaner.api.api.query.QueryParameters;
import org.wpcleaner.api.wiki.definition.WikiDefinition;

public final class MockApiLintErrors {

  private final MockRestServiceServer mediaWikiServer;

  public MockApiLintErrors(final MockRestServiceServer mediaWikiServer) {
    this.mediaWikiServer = mediaWikiServer;
  }

  public LintErrorsAnswer givenRetrieveLintErrors(
      final WikiDefinition wiki, final LintErrorsQuery query) {
    final ApiRequestExpectation request =
        ApiRequestExpectation.of(mediaWikiServer, wiki, ApiParameters.Action.QUERY);
    request.queryParam(QueryParameters.LIST.value, QueryParameters.List.LINT_ERRORS.value);
    request.queryParamCollection(LintErrorsParameters.CATEGORIES.value, query.categories());
    request.queryParam(LintErrorsParameters.FROM.value, query.from());
    request.queryParam(LintErrorsParameters.LIMIT.value, query.limit(), Limit::value);
    request.queryParamCollection(
        LintErrorsParameters.NAMESPACE.value,
        query.namespace(),
        namespace -> Integer.toString(namespace.id()));
    request.queryParamCollection(LintErrorsParameters.PAGE_ID.value, query.pageId());
    request.queryParam(LintErrorsParameters.TITLE.value, query.title());
    return new LintErrorsAnswer(request.answer());
  }

  public record LintErrorsAnswer(ApiAnswer answer) {

    public void willReturn(final List<PageLintError> errors) {
      answer.willReturnQuery("linterrors", errors);
    }

    public void willReturnErrors(final List<ApiError> errors) {
      answer.willReturnErrors(errors);
    }
  }
}
