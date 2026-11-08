package org.wpcleaner.api.api;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.SpringVersion;
import org.wpcleaner.api.ApiSpringBootTest;
import org.wpcleaner.api.wiki.definition.WikipediaDefinitions;

@ApiSpringBootTest
class UserAgentTest {

  @Autowired private CurrentUserService currentUserService;
  @Autowired private UserAgent userAgent;

  @BeforeEach
  void setUp() {
    currentUserService.logout();
  }

  @DisplayName("should describe the client, its contact and its libraries")
  @Test
  void getValue() {
    // WHEN
    final String value = userAgent.getValue();

    // THEN
    Assertions.assertThat(value).isEqualTo(expectedValue(""));
  }

  @DisplayName("should add the user page of the logged in user to the contact")
  @Test
  void getValueWithUser() {
    // GIVEN
    currentUserService.login(WikipediaDefinitions.FR, "Some user", false);

    // WHEN
    final String value = userAgent.getValue();

    // THEN
    Assertions.assertThat(value)
        .isEqualTo(expectedValue("; https://fr.wikipedia.org/wiki/User:Some_user"));
  }

  @DisplayName("should encode the user page of a user with non ASCII characters")
  @Test
  void getValueWithNonAsciiUser() {
    // GIVEN
    currentUserService.login(WikipediaDefinitions.FR, "Élise", false);

    // WHEN
    final String value = userAgent.getValue();

    // THEN
    Assertions.assertThat(value)
        .isEqualTo(expectedValue("; https://fr.wikipedia.org/wiki/User:%C3%89lise"));
  }

  @DisplayName("should not add the user page of a demo user to the contact")
  @Test
  void getValueWithDemoUser() {
    // GIVEN
    currentUserService.login(WikipediaDefinitions.FR, "Some user", true);

    // WHEN
    final String value = userAgent.getValue();

    // THEN
    Assertions.assertThat(value).isEqualTo(expectedValue(""));
  }

  @DisplayName("should use the version of the project")
  @Test
  void getVersion() {
    // WHEN
    final String version = UserAgent.getVersion();

    // THEN
    Assertions.assertThat(version).matches("\\d+\\.\\d+\\.\\d+-SNAPSHOT");
  }

  private static String expectedValue(final String userContact) {
    return "WPCleanerNG/%s (https://github.com/WPCleaner/wpcleaner-ng%s) spring-web/%s Java/%d"
        .formatted(
            UserAgent.getVersion(),
            userContact,
            SpringVersion.getVersion(),
            Runtime.version().feature());
  }
}
