package org.wpcleaner.api.api.move;

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
import org.wpcleaner.api.api.ApiException;
import org.wpcleaner.api.wiki.definition.WikimediaDefinitions;

@SpringBootTest(classes = SpringBootTestConfig.class)
class ApiMoveTest {

  @Autowired private ApiMove apiMove;

  @DisplayName("Move rejected because of an invalid token")
  @Test
  void moveWithInvalidToken() {
    // GIVEN
    final MoveQuery query =
        new MoveQueryByTitle(
            "Meta:Sandbox",
            MoveQueryCommon.emptyBuilder()
                .to("Meta:Sandbox/WPCleaner test")
                .reason("WPCleaner test")
                .token("invalid-token")
                .build());

    // WHEN / THEN
    Assertions.assertThatThrownBy(() -> apiMove.move(WikimediaDefinitions.META, query))
        .as("exception")
        .isInstanceOf(ApiException.class)
        .extracting(exception -> ((ApiException) exception).getDetails())
        .asString()
        .as("details")
        .startsWith("badtoken");
  }
}
