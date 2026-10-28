package org.wpcleaner.api.api.login;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.wpcleaner.api.ApiSpringBootTest;
import org.wpcleaner.api.api.ApiError;
import org.wpcleaner.api.api.ApiException;
import org.wpcleaner.api.wiki.definition.WikipediaDefinitions;

@ApiSpringBootTest
class MockApiLoginTest {

  @Autowired private ApiLogin apiLogin;
  @Autowired private MockApiLogin mockApiLogin;

  @DisplayName("Login returns the login given to the mock")
  @Test
  void login() {
    final Login login = new Login(Login.RESULT_SUCCESS, 1234L, "Jane Doe");
    mockApiLogin
        .givenLogin(WikipediaDefinitions.EN, "Jane Doe@bot", "p&ss+word", "token+\\\\")
        .willReturn(login);

    final Login result =
        apiLogin.login(WikipediaDefinitions.EN, "Jane Doe@bot", "p&ss+word", "token+\\\\");

    Assertions.assertThat(result).isEqualTo(login);
  }

  @DisplayName("Login throws ApiException when MediaWiki returns errors")
  @Test
  void loginWithErrors() {
    mockApiLogin
        .givenLogin(WikipediaDefinitions.EN, "Jane Doe", "password", "token")
        .willReturnErrors(List.of(new ApiError("badtoken", "login", "Invalid CSRF token.")));

    Assertions.assertThatThrownBy(
            () -> apiLogin.login(WikipediaDefinitions.EN, "Jane Doe", "password", "token"))
        .isInstanceOf(ApiException.class);
  }
}
