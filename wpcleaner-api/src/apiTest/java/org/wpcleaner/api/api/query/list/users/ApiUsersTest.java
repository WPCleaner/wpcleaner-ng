package org.wpcleaner.api.api.query.list.users;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.wpcleaner.api.SpringBootTestConfig;
import org.wpcleaner.api.wiki.definition.WikimediaDefinitions;

@SpringBootTest(classes = SpringBootTestConfig.class)
class ApiUsersTest {

  @Autowired private ApiUsers apiUsers;

  @DisplayName("Retrieve existing user")
  @Test
  void retrieveExistingUser() {
    // WHEN
    final User user = apiUsers.retrieveUser(WikimediaDefinitions.META, "NicoV");

    // THEN
    Assertions.assertThat(user).as("user").isNotNull();
    Assertions.assertThat(user.name()).as("name").isEqualTo("NicoV");
    Assertions.assertThat(user.missing()).as("missing").isNull();
    Assertions.assertThat(user.userId()).as("userId").isNotNull().isPositive();
    Assertions.assertThat(user.groups()).as("groups").contains("user");
    Assertions.assertThat(user.rights()).as("rights").contains("edit");
  }

  @DisplayName("Retrieve missing user")
  @Test
  void retrieveMissingUser() {
    // GIVEN
    final String name = "WPCleaner missing user 7f3c9b1e";

    // WHEN
    final User user = apiUsers.retrieveUser(WikimediaDefinitions.META, name);

    // THEN
    Assertions.assertThat(user).as("user").isNotNull();
    Assertions.assertThat(user.name()).as("name").isEqualTo(name);
    Assertions.assertThat(user.missing()).as("missing").isTrue();
    Assertions.assertThat(user.userId()).as("userId").isNull();
  }
}
