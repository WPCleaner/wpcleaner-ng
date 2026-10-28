package org.wpcleaner.api.api.query.meta.tokens;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Collection;
import java.util.List;
import org.springframework.test.web.client.MockRestServiceServer;
import org.wpcleaner.api.api.ApiAnswer;
import org.wpcleaner.api.api.ApiParameters;
import org.wpcleaner.api.api.ApiRequestExpectation;
import org.wpcleaner.api.api.query.QueryParameters;
import org.wpcleaner.api.wiki.definition.WikiDefinition;

public final class MockApiTokens {

  private final MockRestServiceServer mediaWikiServer;

  public MockApiTokens(final MockRestServiceServer mediaWikiServer) {
    this.mediaWikiServer = mediaWikiServer;
  }

  public void givenCsrfToken(final WikiDefinition wiki, final String csrfToken) {
    givenRequestTokens(wiki, List.of(TokensParameters.Type.CSRF))
        .willReturn(new Tokens(null, csrfToken, null, null, null, null, null, null, null));
  }

  public void givenLoginToken(final WikiDefinition wiki, final String loginToken) {
    givenRequestTokens(wiki, List.of(TokensParameters.Type.LOGIN))
        .willReturn(new Tokens(null, null, null, loginToken, null, null, null, null, null));
  }

  public TokensAnswer givenRequestTokens(
      final WikiDefinition wiki, final Collection<TokensParameters.Type> tokens) {
    final ApiRequestExpectation request =
        ApiRequestExpectation.of(mediaWikiServer, wiki, ApiParameters.Action.QUERY);
    request.queryParam(QueryParameters.META.value, QueryParameters.Meta.TOKENS.value);
    request.queryParamCollection(TokensParameters.TYPE.value, tokens, token -> token.value);
    return new TokensAnswer(request.answer());
  }

  public record TokensAnswer(ApiAnswer answer) {

    public void willReturn(final Tokens tokens) {
      answer.willReturnQuery("tokens", tokens);
    }

    public void willFail() {
      answer.willFail();
    }
  }
}
