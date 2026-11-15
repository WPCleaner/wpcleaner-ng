package org.wpcleaner.api.api.query.meta.allmessages;

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
import org.wpcleaner.api.api.query.QueryParameters;
import org.wpcleaner.api.wiki.definition.WikiDefinition;

public final class MockApiAllMessages {

  private final MockRestServiceServer mediaWikiServer;

  public MockApiAllMessages(final MockRestServiceServer mediaWikiServer) {
    this.mediaWikiServer = mediaWikiServer;
  }

  public AllMessagesAnswer givenRetrieveMessages(
      final WikiDefinition wiki, final AllMessagesQuery query) {
    final ApiRequestExpectation request =
        ApiRequestExpectation.of(mediaWikiServer, wiki, ApiParameters.Action.QUERY);
    request.queryParam(QueryParameters.META.value, QueryParameters.Meta.ALL_MESSAGES.value);
    request.queryParam(
        AllMessagesParameters.ARGS.value, query.args(), args -> String.join("|", args));
    request.queryParam(
        AllMessagesParameters.CUSTOMISED.value, query.customised(), customised -> customised.value);
    request.queryParam(AllMessagesParameters.ENABLE_PARSER.value, query.enableParser());
    request.queryParam(AllMessagesParameters.FILTER.value, query.filter());
    request.queryParam(AllMessagesParameters.FROM.value, query.from());
    request.queryParam(AllMessagesParameters.INCLUDE_LOCAL.value, query.includeLocal());
    request.queryParam(AllMessagesParameters.LANGUAGE.value, query.language());
    request.queryParamCollection(AllMessagesParameters.MESSAGES.value, query.messages());
    request.queryParam(AllMessagesParameters.NO_CONTENT.value, query.noContent());
    request.queryParam(AllMessagesParameters.PREFIX.value, query.prefix());
    request.queryParamCollection(
        AllMessagesParameters.PROPERTIES.value, query.properties(), property -> property.value);
    request.queryParam(AllMessagesParameters.TITLE.value, query.title());
    request.queryParam(AllMessagesParameters.TO.value, query.to());
    return new AllMessagesAnswer(request.answer());
  }

  public record AllMessagesAnswer(ApiAnswer answer) {

    public void willReturn(final List<Message> messages) {
      answer.willReturnQuery("allmessages", messages);
    }

    public void willReturnErrors(final List<ApiError> errors) {
      answer.willReturnErrors(errors);
    }
  }
}
