package org.wpcleaner.api.api.query.list.users;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Set;
import org.springframework.test.web.client.MockRestServiceServer;
import org.wpcleaner.api.api.ApiAnswer;
import org.wpcleaner.api.api.ApiParameters;
import org.wpcleaner.api.api.ApiRequestExpectation;
import org.wpcleaner.api.api.query.QueryParameters;
import org.wpcleaner.api.wiki.definition.WikiDefinition;

public final class MockApiUsers {

  private final MockRestServiceServer mediaWikiServer;

  public MockApiUsers(final MockRestServiceServer mediaWikiServer) {
    this.mediaWikiServer = mediaWikiServer;
  }

  public UsersAnswer givenRetrieveUser(final WikiDefinition wiki, final String name) {
    final ApiRequestExpectation request =
        ApiRequestExpectation.of(mediaWikiServer, wiki, ApiParameters.Action.QUERY);
    request.queryParam(QueryParameters.LIST.value, QueryParameters.List.USERS.value);
    request.queryParamCollection(
        UsersParameters.PROPERTIES.value,
        Set.of(UsersParameters.Properties.GROUPS.value, UsersParameters.Properties.RIGHTS.value));
    request.queryParam(UsersParameters.USERS.value, name);
    return new UsersAnswer(request.answer());
  }

  public record UsersAnswer(ApiAnswer answer) {

    public void willReturn(final User user) {
      answer.willReturnQuery("users", List.of(user));
    }

    public void willFail() {
      answer.willFail();
    }
  }
}
