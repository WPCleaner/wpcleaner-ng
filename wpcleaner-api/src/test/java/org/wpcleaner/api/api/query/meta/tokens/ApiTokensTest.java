package org.wpcleaner.api.api.query.meta.tokens;

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
import org.wpcleaner.api.wiki.definition.WikimediaDefinitions;

@SpringBootTest(classes = ApiTokensTest.SpringBootTestConfig.class)
@TestCallingMWApi
class ApiTokensTest {

  @Autowired private ApiTokens apiTokens;

  @ComponentScan(basePackages = "org.wpcleaner")
  @Configuration
  static class SpringBootTestConfig {}

  @DisplayName("Ask for tokens")
  @Test
  void requestTokens() {
    // WHEN
    final Tokens tokens =
        apiTokens.requestTokens(
            WikimediaDefinitions.META,
            List.of(TokensParameters.Type.LOGIN, TokensParameters.Type.PATROL));

    // THEN
    Assertions.assertThat(tokens).as("tokens").isNotNull();
    Assertions.assertThat(tokens.createAccount()).as("createaccount").isNull();
    Assertions.assertThat(tokens.csrf()).as("csrf").isNull();
    Assertions.assertThat(tokens.deleteGlobalAccount()).as("deleteglobalaccount").isNull();
    Assertions.assertThat(tokens.login()).as("login").isNotNull();
    Assertions.assertThat(tokens.patrol()).as("patrol").isNotNull();
    Assertions.assertThat(tokens.rollback()).as("rollback").isNull();
    Assertions.assertThat(tokens.setGlobalAccountStatus()).as("setglobalaccountstatus").isNull();
    Assertions.assertThat(tokens.userRights()).as("userrights").isNull();
    Assertions.assertThat(tokens.watch()).as("watch").isNull();
  }

  @DisplayName("Ask for zero tokens")
  @Test
  void requestZeroTokens() {
    // WHEN
    final Tokens tokens = apiTokens.requestTokens(WikimediaDefinitions.META, List.of());

    // THEN
    Assertions.assertThat(tokens).as("tokens").isNotNull();
    Assertions.assertThat(tokens.createAccount()).as("createaccount").isNull();
    Assertions.assertThat(tokens.csrf()).as("csrf").isNull();
    Assertions.assertThat(tokens.deleteGlobalAccount()).as("deleteglobalaccount").isNull();
    Assertions.assertThat(tokens.login()).as("login").isNull();
    Assertions.assertThat(tokens.patrol()).as("patrol").isNull();
    Assertions.assertThat(tokens.rollback()).as("rollback").isNull();
    Assertions.assertThat(tokens.setGlobalAccountStatus()).as("setglobalaccountstatus").isNull();
    Assertions.assertThat(tokens.userRights()).as("userrights").isNull();
    Assertions.assertThat(tokens.watch()).as("watch").isNull();
  }
}
