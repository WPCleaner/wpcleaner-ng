package org.wpcleaner.api.api.query.list.users;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.HttpServerErrorException;
import org.wpcleaner.api.ApiSpringBootTest;
import org.wpcleaner.api.wiki.definition.WikipediaDefinitions;

@ApiSpringBootTest
class MockApiUsersTest {

  @Autowired private ApiUsers apiUsers;
  @Autowired private MockApiUsers mockApiUsers;

  @DisplayName("Retrieve user returns the user given to the mock")
  @Test
  void retrieveUser() {
    final User user =
        new User(
            null,
            null,
            42,
            null,
            null,
            List.of(),
            List.of("*", "bot", "user"),
            List.of("*", "user"),
            null,
            null,
            "Jane Doe",
            null,
            List.of("edit", "read"),
            null,
            1234);
    mockApiUsers.givenRetrieveUser(WikipediaDefinitions.EN, "Jane Doe").willReturn(user);

    final User result = apiUsers.retrieveUser(WikipediaDefinitions.EN, "Jane Doe");

    Assertions.assertThat(result).isEqualTo(user);
  }

  @DisplayName("Retrieve user fails when the mock fails")
  @Test
  void retrieveUserWhenFailing() {
    mockApiUsers.givenRetrieveUser(WikipediaDefinitions.EN, "Jane Doe").willFail();

    Assertions.assertThatThrownBy(() -> apiUsers.retrieveUser(WikipediaDefinitions.EN, "Jane Doe"))
        .isInstanceOf(HttpServerErrorException.class);
  }
}
