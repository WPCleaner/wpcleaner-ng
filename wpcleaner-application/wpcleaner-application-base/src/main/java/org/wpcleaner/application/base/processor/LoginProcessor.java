package org.wpcleaner.application.base.processor;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.wpcleaner.api.api.login.ApiLogin;
import org.wpcleaner.api.api.query.meta.tokens.ApiTokens;
import org.wpcleaner.api.api.query.meta.tokens.Tokens;
import org.wpcleaner.api.api.query.meta.tokens.TokensParameters;
import org.wpcleaner.api.wiki.definition.WikiDefinition;

@Service
public class LoginProcessor {

  private final ApiLogin apiLogin;
  private final ApiTokens apiTokens;

  public LoginProcessor(final ApiLogin apiLogin, final ApiTokens apiTokens) {
    this.apiLogin = apiLogin;
    this.apiTokens = apiTokens;
  }

  public LoginResult execute(final Input input) {
    final Tokens tokens = apiTokens.requestTokens(input.wiki, List.of(TokensParameters.Type.LOGIN));
    apiLogin.login(
        input.wiki,
        input.username,
        new String(input.password),
        Objects.requireNonNull(tokens.login(), "Login token is null"));
    final String compactUsername = compactUsername(input.username);
    return new LoginResult(input.wiki, compactUsername);
  }

  private String compactUsername(final String username) {
    final int atSignIndex = username.indexOf('@');
    if (atSignIndex == -1) {
      return username;
    }
    return username.substring(0, atSignIndex);
  }

  public record Input(
      WikiDefinition wiki,
      String username,
      @SuppressWarnings("ArrayRecordComponent") char[] password) {}
}
