package org.wpcleaner.api.hook.login;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.client.MockRestServiceServer;
import org.wpcleaner.api.ApiSpringBootTest;
import org.wpcleaner.api.api.ConnectedUser;
import org.wpcleaner.api.api.query.prop.revisions.MockApiRevisions;
import org.wpcleaner.api.repository.configuration.WPCleanerConfiguration;
import org.wpcleaner.api.repository.configuration.WPCleanerConfigurationRepository;
import org.wpcleaner.api.repository.configuration.WPCleanerParameter;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.api.wiki.definition.WikipediaDefinitions;

@ApiSpringBootTest
class WPCleanerConfigurationLoaderTest {

  private static final WikiDefinition WIKI = WikipediaDefinitions.FR;
  private static final String USERNAME = "Jane Doe";
  private static final String TRANSLATION_PAGE = "Projet:Correction syntaxique/Traduction";

  @Autowired private WPCleanerConfigurationLoader loader;
  @Autowired private WPCleanerConfigurationRepository repository;
  @Autowired private MockApiRevisions mockApiRevisions;
  @Autowired private MockRestServiceServer mediaWikiServer;

  @BeforeEach
  void setUp() {
    mediaWikiServer.reset();
    repository.logout(new ConnectedUser(false, WIKI, "", false, List.of(), List.of()));
  }

  @DisplayName("should load the general and the user configurations")
  @Test
  void loadConfiguration() {
    // GIVEN
    WPCleanerConfigurationExpectations.givenConfiguration(
        mockApiRevisions,
        WIKI,
        USERNAME,
        "# Comment\ncheck_wiki_translation_page=%s END".formatted(TRANSLATION_PAGE),
        "error_048_prio_frwiki=0 END\ncheck_wiki_translation_page=Utilisateur:Jane Doe END");

    // WHEN
    loader.load(WIKI, USERNAME);

    // THEN
    mediaWikiServer.verify();
    final WPCleanerConfiguration configuration = repository.getConfiguration(WIKI);
    Assertions.assertThat(configuration.getString(WPCleanerParameter.CHECK_WIKI_TRANSLATION_PAGE))
        .contains(TRANSLATION_PAGE);
    Assertions.assertThat(configuration.generalProperties())
        .containsExactly(Map.entry("check_wiki_translation_page", TRANSLATION_PAGE));
    Assertions.assertThat(configuration.userProperties())
        .containsEntry("error_048_prio_frwiki", "0");
  }

  @DisplayName("should load an empty configuration when the pages are missing")
  @Test
  void loadMissingConfiguration() {
    // GIVEN
    WPCleanerConfigurationExpectations.givenNoConfiguration(mockApiRevisions, WIKI, USERNAME);

    // WHEN
    loader.load(WIKI, USERNAME);

    // THEN
    mediaWikiServer.verify();
    Assertions.assertThat(repository.getConfiguration(WIKI))
        .isEqualTo(WPCleanerConfiguration.EMPTY);
  }

  @DisplayName("should load the general configuration only once for its own user")
  @Test
  void loadConfigurationForGeneralUser() {
    // GIVEN
    WPCleanerConfigurationExpectations.givenConfiguration(
        mockApiRevisions,
        WIKI,
        WPCleanerConfigurationLoader.GENERAL_USERNAME,
        "check_wiki_translation_page=%s END".formatted(TRANSLATION_PAGE),
        null);

    // WHEN
    loader.load(WIKI, WPCleanerConfigurationLoader.GENERAL_USERNAME);

    // THEN
    mediaWikiServer.verify();
    Assertions.assertThat(
            repository
                .getConfiguration(WIKI)
                .getString(WPCleanerParameter.CHECK_WIKI_TRANSLATION_PAGE))
        .contains(TRANSLATION_PAGE);
  }

  @DisplayName("should forget the configuration at logout")
  @Test
  void logout() {
    // GIVEN
    WPCleanerConfigurationExpectations.givenConfiguration(
        mockApiRevisions,
        WIKI,
        USERNAME,
        "check_wiki_translation_page=%s END".formatted(TRANSLATION_PAGE),
        null);
    loader.load(WIKI, USERNAME);

    // WHEN
    repository.logout(new ConnectedUser(true, WIKI, USERNAME, false, List.of(), List.of()));

    // THEN
    Assertions.assertThat(repository.getConfiguration(WIKI))
        .isEqualTo(WPCleanerConfiguration.EMPTY);
  }
}
