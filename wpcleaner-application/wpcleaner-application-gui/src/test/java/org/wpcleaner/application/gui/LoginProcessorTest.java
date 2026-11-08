package org.wpcleaner.application.gui;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.wpcleaner.api.api.CurrentUserService;
import org.wpcleaner.api.repository.configuration.WPCleanerConfigurationRepository;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.api.wiki.definition.WikipediaDefinitions;

@GuiSpringBootTest
class LoginProcessorTest {

  private static final WikiDefinition WIKI = WikipediaDefinitions.FR;
  private static final String USERNAME = "Jane Doe";

  @Autowired private CurrentUserService currentUserService;
  @Autowired private GuiLogin guiLogin;
  @Autowired private WPCleanerConfigurationRepository configurationRepository;

  @BeforeEach
  void setUp() {
    currentUserService.logout();
  }

  @AfterEach
  void tearDown() {
    currentUserService.logout();
  }

  @DisplayName("should use the user name without the bot name for a bot password")
  @Test
  void loginWithBotPassword() {
    // GIVEN
    final GuiLogin.LoginData data =
        GuiLogin.LoginData.of(GuiLogin.USER_NAMESPACES)
            .withUserConfiguration("error_048_prio_frwiki=0 END");

    // WHEN
    guiLogin.login(WIKI, USERNAME + "@WPCleaner", data);

    // THEN
    Assertions.assertThat(currentUserService.getCurrentUser().username()).isEqualTo(USERNAME);
    Assertions.assertThat(configurationRepository.getConfiguration(WIKI).userProperties())
        .containsEntry("error_048_prio_frwiki", "0");
  }
}
