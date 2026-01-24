package org.wpcleaner.application.base.processor;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
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

  @SuppressWarnings("PMD.AvoidCatchingGenericException")
  public void process(
      final WikiDefinition wiki,
      final String username,
      final char[] password,
      final Consumer<LoginResult> onSuccess,
      final Consumer<RuntimeException> onFailure) {
    final LoginResult loginResult;
    try {
      loginResult = internalProcess(wiki, username, password);
    } catch (RuntimeException e) {
      onFailure.accept(e);
      return;
    }
    onSuccess.accept(loginResult);
  }

  @SuppressWarnings("PMD.UseVarargs")
  private LoginResult internalProcess(
      final WikiDefinition wiki, final String username, final char[] password) {
    final Tokens tokens = apiTokens.requestTokens(wiki, List.of(TokensParameters.Type.LOGIN));
    apiLogin.login(
        wiki,
        username,
        new String(password),
        Objects.requireNonNull(tokens.login(), "Login token is null"));
    final String compactUsername = compactUsername(username);
    return new LoginResult(wiki, compactUsername);
  }

  private String compactUsername(final String username) {
    final int atSignIndex = username.indexOf('@');
    if (atSignIndex == -1) {
      return username;
    }
    return username.substring(0, atSignIndex);
  }
}
