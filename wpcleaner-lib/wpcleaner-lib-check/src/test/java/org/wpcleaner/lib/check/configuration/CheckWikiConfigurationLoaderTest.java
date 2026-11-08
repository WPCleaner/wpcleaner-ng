package org.wpcleaner.lib.check.configuration;

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
import org.wpcleaner.api.hook.login.WPCleanerConfigurationExpectations;
import org.wpcleaner.api.progress.ProgressTracker;
import org.wpcleaner.api.repository.configuration.WPCleanerConfiguration;
import org.wpcleaner.api.repository.configuration.WPCleanerConfigurationRepository;
import org.wpcleaner.api.wiki.builder.WikiBuilder;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.api.wiki.definition.WikipediaDefinitions;

@ApiSpringBootTest
class CheckWikiConfigurationLoaderTest {

  private static final WikiDefinition WIKI = WikipediaDefinitions.FR;
  private static final String TRANSLATION_PAGE = "Projet:Correction syntaxique/Traduction";
  private static final String HEAD = "Lien interne vers le titre de l’article";
  private static final String TRANSLATION =
      """
      <syntaxhighlight lang="Properties">
       project=frwiki END
       error_048_prio_frwiki=3 END
       error_048_head_frwiki=%s END
       error_048_head_enwiki=Link to the article itself END
       error_048_link_frwiki=P:CS/048 END
       error_001_prio_frwiki=0 END
      </syntaxhighlight>
      """
          .formatted(HEAD);

  @Autowired private CheckWikiConfigurationLoader loader;
  @Autowired private CheckWikiConfigurationRepository repository;
  @Autowired private WPCleanerConfigurationRepository wpcleanerRepository;
  @Autowired private MockApiRevisions mockApiRevisions;
  @Autowired private MockRestServiceServer mediaWikiServer;

  @BeforeEach
  void setUp() {
    mediaWikiServer.reset();
    final ConnectedUser user = new ConnectedUser(false, WIKI, "", false, List.of(), List.of());
    repository.logout(user);
    wpcleanerRepository.logout(user);
  }

  @DisplayName("should load the configuration of the algorithms for the wiki")
  @Test
  void loadConfiguration() {
    // GIVEN
    givenTranslationPage(Map.of());

    // WHEN
    loader.execute(WIKI, new ProgressTracker(_ -> {}));

    // THEN
    mediaWikiServer.verify();
    final CheckWikiConfiguration configuration = repository.getConfiguration(WIKI);
    Assertions.assertThat(configuration.getAlgorithm(48))
        .hasValueSatisfying(
            algorithm -> {
              Assertions.assertThat(algorithm.properties())
                  .containsOnly(
                      Map.entry("prio", "3"),
                      Map.entry("head", HEAD),
                      Map.entry("link", "P:CS/048"));
              Assertions.assertThat(algorithm.isActive()).isTrue();
              Assertions.assertThat(algorithm.shortDescription()).contains(HEAD);
            });
    Assertions.assertThat(configuration.getAlgorithm(1))
        .hasValueSatisfying(algorithm -> Assertions.assertThat(algorithm.isActive()).isFalse());
    Assertions.assertThat(configuration.getAlgorithm(2)).isEmpty();
  }

  @DisplayName("should let the user configuration override the configuration of the wiki")
  @Test
  void loadConfigurationWithUserOverride() {
    // GIVEN
    givenTranslationPage(Map.of("error_048_prio_frwiki", "0", "error_048_prio_enwiki", "1"));

    // WHEN
    loader.execute(WIKI, new ProgressTracker(_ -> {}));

    // THEN
    mediaWikiServer.verify();
    Assertions.assertThat(repository.getConfiguration(WIKI).getAlgorithm(48))
        .hasValueSatisfying(
            algorithm -> {
              Assertions.assertThat(algorithm.properties()).containsEntry("prio", "0");
              Assertions.assertThat(algorithm.isActive()).isFalse();
            });
  }

  @DisplayName("should load an empty configuration without a translation page")
  @Test
  void loadConfigurationWithoutTranslationPage() {
    // GIVEN
    wpcleanerRepository.setConfiguration(WIKI, WPCleanerConfiguration.EMPTY);

    // WHEN
    loader.execute(WIKI, new ProgressTracker(_ -> {}));

    // THEN
    mediaWikiServer.verify();
    Assertions.assertThat(repository.getConfiguration(WIKI))
        .isEqualTo(CheckWikiConfiguration.EMPTY);
  }

  @DisplayName("should load an empty configuration for a wiki without Check Wiki code")
  @Test
  void loadConfigurationWithoutCheckWikiCode() {
    // GIVEN
    final WikiDefinition wiki = WikiBuilder.ltr("en", "Test", "test.example.org").build();
    wpcleanerRepository.setConfiguration(
        wiki,
        new WPCleanerConfiguration(
            Map.of("check_wiki_translation_page", TRANSLATION_PAGE), Map.of()));

    // WHEN
    loader.execute(wiki, new ProgressTracker(_ -> {}));

    // THEN
    mediaWikiServer.verify();
    Assertions.assertThat(repository.getConfiguration(wiki))
        .isEqualTo(CheckWikiConfiguration.EMPTY);
  }

  private void givenTranslationPage(final Map<String, String> userProperties) {
    wpcleanerRepository.setConfiguration(
        WIKI,
        new WPCleanerConfiguration(
            Map.of("check_wiki_translation_page", TRANSLATION_PAGE), userProperties));
    WPCleanerConfigurationExpectations.givenPage(
        mockApiRevisions, WIKI, TRANSLATION_PAGE, TRANSLATION);
  }
}
