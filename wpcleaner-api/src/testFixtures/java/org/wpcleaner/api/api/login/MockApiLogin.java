package org.wpcleaner.api.api.login;

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

public final class MockApiLogin {

  private final MockRestServiceServer mediaWikiServer;

  public MockApiLogin(final MockRestServiceServer mediaWikiServer) {
    this.mediaWikiServer = mediaWikiServer;
  }

  public LoginAnswer givenLogin(
      final WikiDefinition wiki, final String username, final String password, final String token) {
    final ApiRequestExpectation request =
        ApiRequestExpectation.ofPost(mediaWikiServer, wiki, ApiParameters.Action.LOGIN);
    request.bodyParam(LoginParameters.NAME.value, username);
    request.bodyParam(LoginParameters.PASSWORD.value, password);
    request.bodyParam(LoginParameters.TOKEN.value, token);
    return new LoginAnswer(request.answer());
  }

  public record LoginAnswer(ApiAnswer answer) {

    public void willReturn(final Login login) {
      answer.willReturn(Map.of("login", login));
    }

    public void willReturnErrors(final List<ApiError> errors) {
      answer.willReturnErrors(errors);
    }

    public void willFail() {
      answer.willFail();
    }
  }
}
