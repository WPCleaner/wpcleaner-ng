package org.wpcleaner.application.base.processor;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.wpcleaner.api.api.CurrentUserService;
import org.wpcleaner.api.api.login.ApiLogin;
import org.wpcleaner.api.api.query.list.users.ApiUsers;
import org.wpcleaner.api.api.query.list.users.User;
import org.wpcleaner.api.api.query.meta.tokens.ApiTokens;
import org.wpcleaner.api.api.query.meta.tokens.Tokens;
import org.wpcleaner.api.api.query.meta.tokens.TokensParameters;
import org.wpcleaner.api.wiki.definition.WikiDefinition;

@Service
public class LoginProcessor implements Processor<LoginProcessor.Input, LoginResult> {

  private final ApiLogin apiLogin;
  private final ApiTokens apiTokens;
  private final ApiUsers apiUsers;
  private final CurrentUserService currentUserService;

  public LoginProcessor(
      final ApiLogin apiLogin,
      final ApiTokens apiTokens,
      final ApiUsers apiUsers,
      final CurrentUserService currentUserService) {
    this.apiLogin = apiLogin;
    this.apiTokens = apiTokens;
    this.apiUsers = apiUsers;
    this.currentUserService = currentUserService;
  }

  @Override
  @SuppressWarnings("try")
  public LoginResult execute(final Input input, final ProgressTracker tracker) {
    currentUserService.logout();
    if (!input.demo()) {
      final Tokens tokens;
      try (ProgressTracker.ProgressStep _ = tracker.start("Retrieving login token")) {
        tokens = apiTokens.requestTokens(input.wiki, List.of(TokensParameters.Type.LOGIN));
      }
      try (ProgressTracker.ProgressStep _ =
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
    try (ProgressTracker.ProgressStep _ =
        tracker.start("Retrieving information about user %s".formatted(input.username))) {
      final User user = apiUsers.retrieveUser(input.wiki, compactUsername);
      currentUserService.withGroups(user.groups());
      currentUserService.withRights(user.rights());
    }
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

    @Override
    public boolean equals(final Object obj) {
      if (this == obj) {
        return true;
      }
      if (!(obj
          instanceof
          Input(
              WikiDefinition otherWiki,
              String otherUsername,
              char[] otherPassword,
              boolean otherDemo))) {
        return false;
      }
      return Objects.equals(wiki, otherWiki)
          && Objects.equals(username, otherUsername)
          && demo == otherDemo
          && Arrays.equals(password, otherPassword);
    }

    @Override
    public int hashCode() {
      int result = Objects.hash(wiki, username, demo);
      result = 31 * result + Arrays.hashCode(password);
      return result;
    }

    @Override
    public String toString() {
      return "LoginProcessor.Input{wiki=%s, username=%s, demo=%b}".formatted(wiki, username, demo);
    }
  }
}
