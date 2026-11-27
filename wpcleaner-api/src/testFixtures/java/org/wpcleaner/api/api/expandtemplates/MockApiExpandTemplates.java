package org.wpcleaner.api.api.expandtemplates;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Map;
import org.springframework.test.web.client.MockRestServiceServer;
import org.wpcleaner.api.api.ApiAnswer;
import org.wpcleaner.api.api.ApiError;
import org.wpcleaner.api.api.ApiParameters;
import org.wpcleaner.api.api.ApiRequestExpectation;
import org.wpcleaner.api.wiki.definition.WikiDefinition;

public final class MockApiExpandTemplates {

  private final MockRestServiceServer mediaWikiServer;

  public MockApiExpandTemplates(final MockRestServiceServer mediaWikiServer) {
    this.mediaWikiServer = mediaWikiServer;
  }

  public ExpandTemplatesAnswer givenExpandTemplates(
      final WikiDefinition wiki, final ExpandTemplatesQuery query) {
    final ApiRequestExpectation request =
        ApiRequestExpectation.ofPost(mediaWikiServer, wiki, ApiParameters.Action.EXPAND_TEMPLATES);
    request.bodyParam(ExpandTemplatesParameters.TITLE.value, query.title());
    request.bodyParam(ExpandTemplatesParameters.TEXT.value, query.text());
    request.bodyParam(ExpandTemplatesParameters.REVISION_ID.value, query.revisionId());
    request.bodyParam(
        ExpandTemplatesParameters.PROPERTIES.value,
        query.properties().stream().map(property -> property.value).sorted().toList());
    request.bodyParam(ExpandTemplatesParameters.INCLUDE_COMMENTS.value, query.includeComments());
    request.bodyParam(ExpandTemplatesParameters.SHOW_STRATEGY_KEYS.value, query.showStrategyKeys());
    return new ExpandTemplatesAnswer(request.answer());
  }

  public record ExpandTemplatesAnswer(ApiAnswer answer) {

    public void willReturn(final ExpandTemplates expandTemplates) {
      answer.willReturn(Map.of("expandtemplates", expandTemplates));
    }

    public void willReturnErrors(final List<ApiError> errors) {
      answer.willReturnErrors(errors);
    }
  }
}
