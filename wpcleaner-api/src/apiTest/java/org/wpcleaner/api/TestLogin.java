package org.wpcleaner.api;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Component;
import org.wpcleaner.api.api.Credential;
import org.wpcleaner.api.api.CredentialsProvider;
import org.wpcleaner.api.api.login.ApiLogin;
import org.wpcleaner.api.api.login.Login;
import org.wpcleaner.api.api.query.meta.tokens.ApiTokens;
import org.wpcleaner.api.api.query.meta.tokens.Tokens;
import org.wpcleaner.api.api.query.meta.tokens.TokensParameters;
import org.wpcleaner.api.wiki.definition.WikiDefinition;

@Component
public class TestLogin {

  private final ApiLogin apiLogin;
  private final ApiTokens apiTokens;
  private final CredentialsProvider credentialsProvider;

  public TestLogin(
      final ApiLogin apiLogin,
      final ApiTokens apiTokens,
      final CredentialsProvider credentialsProvider) {
    this.apiLogin = apiLogin;
    this.apiTokens = apiTokens;
    this.credentialsProvider = credentialsProvider;
  }

  public Credential credential(final WikiDefinition wiki) {
    return credentialsProvider
        .getCredential(wiki)
        .orElseThrow(
            () ->
                new IllegalStateException(
                    """
                    No credential provided for wiki %s.
                    Please provide a credentials.yaml or credentials.json file with usable credential.
                    """
                        .formatted(wiki.code())));
  }

  public Login login(final WikiDefinition wiki) {
    final Tokens tokens = apiTokens.requestTokens(wiki, List.of(TokensParameters.Type.LOGIN));
    final Credential credential = credential(wiki);
    return apiLogin.login(
        wiki, credential.username(), credential.password(), Objects.requireNonNull(tokens.login()));
  }
}
