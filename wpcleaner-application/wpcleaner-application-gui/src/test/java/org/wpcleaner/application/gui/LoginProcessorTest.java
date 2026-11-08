package org.wpcleaner.application.gui;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
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
import org.wpcleaner.lib.check.CheckAlgorithm;
import org.wpcleaner.lib.check.CheckAlgorithmRegistry;

@GuiSpringBootTest
class LoginProcessorTest {

  private static final WikiDefinition WIKI = WikipediaDefinitions.FR;
  private static final String USERNAME = "Jane Doe";

  @Autowired private CurrentUserService currentUserService;
  @Autowired private GuiLogin guiLogin;
  @Autowired private WPCleanerConfigurationRepository configurationRepository;
  @Autowired private CheckAlgorithmRegistry algorithmRegistry;

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

  @DisplayName("should load the Check Wiki configuration at login")
  @Test
  void loginWithCheckWikiConfiguration() {
    // GIVEN
    final GuiLogin.LoginData data =
        GuiLogin.LoginData.of(GuiLogin.USER_NAMESPACES)
            .withCheckWikiConfiguration(
                " error_048_prio_frwiki=3 END\n error_048_head_frwiki=Lien vers l’article END");

    // WHEN
    guiLogin.login(WIKI, USERNAME, data);

    // THEN
    final List<CheckAlgorithm> algorithms = algorithmRegistry.getActiveAlgorithms(WIKI);
    Assertions.assertThat(algorithms).extracting(CheckAlgorithm::number).containsExactly(48);
    Assertions.assertThat(algorithmRegistry.getName(WIKI, algorithms.getFirst()))
        .isEqualTo("Lien vers l’article");
  }
}
