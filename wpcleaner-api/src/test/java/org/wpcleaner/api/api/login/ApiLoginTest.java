package org.wpcleaner.api.api.login;

/*
 * SPDX-FileCopyrightText: © 2025 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.wpcleaner.api.TestCallingMWApi;
import org.wpcleaner.api.api.Credential;
import org.wpcleaner.api.api.CredentialsProvider;
import org.wpcleaner.api.api.query.meta.tokens.ApiTokens;
import org.wpcleaner.api.api.query.meta.tokens.Tokens;
import org.wpcleaner.api.api.query.meta.tokens.TokensParameters;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.api.wiki.definition.WikimediaDefinitions;

@SpringBootTest(classes = ApiLoginTest.SpringBootTestConfig.class)
@TestCallingMWApi
class ApiLoginTest {

  @Autowired private ApiLogin apiLogin;
  @Autowired private ApiTokens apiTokens;
  @Autowired private CredentialsProvider credentialsProvider;

  @Configuration
  @ComponentScan(basePackages = "org.wpcleaner")
  static class SpringBootTestConfig {}

  @Test
  @DisplayName("Login")
  void login() {
    // GIVEN
    final WikiDefinition wiki = WikimediaDefinitions.META;
    final Tokens tokens = apiTokens.requestTokens(wiki, List.of(TokensParameters.Type.LOGIN));
    final Credential credential =
        credentialsProvider
            .getCredential(wiki)
            .orElseThrow(
                () ->
                    new IllegalStateException(
                        """
                        No credential provided for wiki %s.
                        Please provide a credentials.yaml or credentials.json file with usable credential.
                        """
                            .formatted(wiki.code())));

    // WHEN
    final Login login =
        apiLogin.login(wiki, credential.username(), credential.password(), tokens.login());

    // THEN
    Assertions.assertThat(login).as("login").isNotNull();
    Assertions.assertThat(login.result()).as("result").isEqualTo("Success");
    Assertions.assertThat(login.username()).as("username").isEqualTo(credential.username());
    Assertions.assertThat(login.userId()).as("userId").isNotNull();
  }
}
