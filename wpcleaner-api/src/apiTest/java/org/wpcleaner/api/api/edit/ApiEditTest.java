package org.wpcleaner.api.api.edit;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.wpcleaner.api.SpringBootTestConfig;
import org.wpcleaner.api.TestLogin;
import org.wpcleaner.api.api.ApiException;
import org.wpcleaner.api.api.login.Login;
import org.wpcleaner.api.api.query.meta.tokens.ApiTokens;
import org.wpcleaner.api.api.query.meta.tokens.Tokens;
import org.wpcleaner.api.api.query.meta.tokens.TokensParameters;
import org.wpcleaner.api.wiki.definition.KnownDefinitions;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.api.wiki.definition.WikimediaDefinitions;

@DirtiesContext
@SpringBootTest(classes = SpringBootTestConfig.class)
class ApiEditTest {

  @Autowired private ApiEdit apiEdit;
  @Autowired private ApiTokens apiTokens;
  @Autowired private KnownDefinitions knownDefinitions;
  @Autowired private TestLogin testLogin;

  @DisplayName("Edit sandbox successfully")
  @Test
  void editSandbox() {
    // GIVEN
    final WikiDefinition wiki = knownDefinitions.getDefinition("test").orElseThrow();
    final Login login = testLogin.login(wiki);
    Assertions.assertThat(login.result()).as("login result").isEqualTo(Login.RESULT_SUCCESS);
    final String title = "User:%s/sandbox".formatted(login.username());
    final Tokens tokens = apiTokens.requestTokens(wiki, List.of(TokensParameters.Type.CSRF));
    final EditQuery query =
        new EditQueryByTitle(
            title,
            EditQueryCommon.emptyBuilder()
                .summary("WPCleaner test")
                .text("WPCleaner test: %s".formatted(Instant.now()))
                .token(Objects.requireNonNull(tokens.csrf()))
                .build());

    // WHEN
    final Edit edit = apiEdit.edit(wiki, query);

    // THEN
    Assertions.assertThat(edit).as("edit").isNotNull();
    Assertions.assertThat(edit.result()).as("result").isEqualTo(Edit.RESULT_SUCCESS);
    Assertions.assertThat(edit.title()).as("title").isEqualTo(title);
    Assertions.assertThat(edit.pageId()).as("pageId").isNotNull().isPositive();
    Assertions.assertThat(edit.noChange()).as("noChange").isNull();
  }

  @DisplayName("Edit rejected because of an invalid token")
  @Test
  void editWithInvalidToken() {
    // GIVEN
    final EditQuery query =
        new EditQueryByTitle(
            "Meta:Sandbox",
            EditQueryCommon.emptyBuilder()
                .appendText("WPCleaner test")
                .noCreate(true)
                .summary("WPCleaner test")
                .token("invalid-token")
                .build());

    // WHEN / THEN
    Assertions.assertThatThrownBy(() -> apiEdit.edit(WikimediaDefinitions.META, query))
        .as("exception")
        .isInstanceOf(ApiException.class)
        .extracting(exception -> ((ApiException) exception).getDetails())
        .asString()
        .as("details")
        .startsWith("badtoken");
  }
}
