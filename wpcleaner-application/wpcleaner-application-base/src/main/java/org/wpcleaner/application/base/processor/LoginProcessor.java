package org.wpcleaner.application.base.processor;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.wpcleaner.api.api.CurrentUserService;
import org.wpcleaner.api.api.login.ApiLogin;
import org.wpcleaner.api.api.query.meta.tokens.ApiTokens;
import org.wpcleaner.api.api.query.meta.tokens.Tokens;
import org.wpcleaner.api.api.query.meta.tokens.TokensParameters;
import org.wpcleaner.api.wiki.definition.WikiDefinition;

@Service
public class LoginProcessor implements Processor<LoginProcessor.Input, LoginResult> {

  private final ApiLogin apiLogin;
  private final ApiTokens apiTokens;
  private final CurrentUserService currentUserService;

  public LoginProcessor(
      final ApiLogin apiLogin,
      final ApiTokens apiTokens,
      final CurrentUserService currentUserService) {
    this.apiLogin = apiLogin;
    this.apiTokens = apiTokens;
    this.currentUserService = currentUserService;
  }

  @Override
  @SuppressWarnings("try")
  public LoginResult execute(final Input input, final ProgressTracker tracker) {
    currentUserService.logout();
    if (!input.demo()) {
      final Tokens tokens;
      try (ProgressTracker.ProgressStep ignored = tracker.start("Retrieving login token")) {
        tokens = apiTokens.requestTokens(input.wiki, List.of(TokensParameters.Type.LOGIN));
      }
      try (ProgressTracker.ProgressStep ignored =
          tracker.start("Login for user %s".formatted(input.username))) {
        apiLogin.login(
            input.wiki,
            input.username,
            new String(input.password),
            Objects.requireNonNull(tokens.login(), "Login token is null"));
      }
    }
    final String compactUsername = compactUsername(input.username);
    currentUserService.login(input.wiki(), compactUsername, input.demo());
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
      @SuppressWarnings("ArrayRecordComponent") char[] password,
      boolean demo) {
    @SuppressWarnings("PMD.UseVarargs")
    public static Input forLogin(
        final WikiDefinition wiki, final String username, final char[] password) {
      return new Input(wiki, username, password, false);
    }

    public static Input forDemo(final WikiDefinition wiki, final String username) {
      return new Input(wiki, username, new char[] {}, true);
    }
  }
}
