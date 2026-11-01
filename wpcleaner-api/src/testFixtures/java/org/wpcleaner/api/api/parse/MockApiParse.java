package org.wpcleaner.api.api.parse;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.test.web.client.MockRestServiceServer;
import org.wpcleaner.api.api.ApiAnswer;
import org.wpcleaner.api.api.ApiError;
import org.wpcleaner.api.api.ApiParameters;
import org.wpcleaner.api.api.ApiRequestExpectation;
import org.wpcleaner.api.wiki.definition.WikiDefinition;

public final class MockApiParse {

  private final MockRestServiceServer mediaWikiServer;

  public MockApiParse(final MockRestServiceServer mediaWikiServer) {
    this.mediaWikiServer = mediaWikiServer;
  }

  public ParseAnswer givenParse(final WikiDefinition wiki, final ParseQuery query) {
    final ApiRequestExpectation request =
        ApiRequestExpectation.ofPost(mediaWikiServer, wiki, ApiParameters.Action.PARSE);
    request.bodyParam(ParseParameters.TITLE.value, query.title());
    request.bodyParam(ParseParameters.TEXT.value, query.text());
    request.bodyParam(ParseParameters.CONTENT_MODEL.value, ParseParameters.WIKITEXT_CONTENT_MODEL);
    request.bodyParam(
        ParseParameters.PROPERTIES.value,
        query.properties().stream()
            .map(property -> property.value)
            .sorted()
            .collect(Collectors.joining("|")));
    return new ParseAnswer(request.answer());
  }

  public record ParseAnswer(ApiAnswer answer) {

    public void willReturn(final Parse parse) {
      answer.willReturn(Map.of("parse", parse));
    }

    public void willReturnErrors(final List<ApiError> errors) {
      answer.willReturnErrors(errors);
    }
  }
}
