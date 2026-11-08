package org.wpcleaner.api.api;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.SpringVersion;
import org.wpcleaner.api.ApiSpringBootTest;

@ApiSpringBootTest
class UserAgentTest {

  @Autowired private UserAgent userAgent;

  @DisplayName("should describe the client, its contact and its libraries")
  @Test
  void getValue() {
    // WHEN
    final String value = userAgent.getValue();

    // THEN
    Assertions.assertThat(value)
        .isEqualTo(
            "WPCleanerNG/%s (https://github.com/WPCleaner/wpcleaner-ng) spring-web/%s Java/%d"
                .formatted(
                    UserAgent.getVersion(),
                    SpringVersion.getVersion(),
                    Runtime.version().feature()));
  }

  @DisplayName("should use the version of the project")
  @Test
  void getVersion() {
    // WHEN
    final String version = UserAgent.getVersion();

    // THEN
    Assertions.assertThat(version).matches("\\d+\\.\\d+\\.\\d+-SNAPSHOT");
  }
}
