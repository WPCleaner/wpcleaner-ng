package org.wpcleaner.lib.check.configuration;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.assertj.core.api.Assertions;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.client.MockRestServiceServer;
import org.wpcleaner.api.ApiSpringBootTest;
import org.wpcleaner.api.analysis.pagetitle.PageTitle;
import org.wpcleaner.api.api.ConnectedUser;
import org.wpcleaner.api.api.query.prop.revisions.MockApiRevisions;
import org.wpcleaner.api.hook.login.ConfigurationPageReader;
import org.wpcleaner.api.hook.login.LoginWarning;
import org.wpcleaner.api.hook.login.WPCleanerConfigurationExpectations;
import org.wpcleaner.api.progress.ProgressTracker;
import org.wpcleaner.api.repository.configuration.WPCleanerConfiguration;
import org.wpcleaner.api.repository.configuration.WPCleanerConfigurationRepository;
import org.wpcleaner.api.repository.namespace.NamespaceRepository;
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
  @Autowired private NamespaceRepository namespaceRepository;

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
    givenTranslationPage(TRANSLATION, Map.of());
    givenParametersPage(null);

    // WHEN
    loader.execute(WIKI, ProgressTracker.NOOP);

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
              Assertions.assertThat(algorithm.parameters()).isNull();
            });
    Assertions.assertThat(configuration.getAlgorithm(1))
        .hasValueSatisfying(algorithm -> Assertions.assertThat(algorithm.isActive()).isFalse());
    Assertions.assertThat(configuration.getAlgorithm(2)).isEmpty();
  }

  @DisplayName("should let the user configuration override the configuration of the wiki")
  @Test
  void loadConfigurationWithUserOverride() {
    // GIVEN
    givenTranslationPage(
        TRANSLATION, Map.of("error_048_prio_frwiki", "0", "error_048_prio_enwiki", "1"));

    // WHEN
    loader.execute(WIKI, ProgressTracker.NOOP);

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
    loader.execute(WIKI, ProgressTracker.NOOP);

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
    loader.execute(wiki, ProgressTracker.NOOP);

    // THEN
    mediaWikiServer.verify();
    Assertions.assertThat(repository.getConfiguration(wiki))
        .isEqualTo(CheckWikiConfiguration.EMPTY);
  }

  @DisplayName("should load the whitelists of the algorithms")
  @Test
  void loadWhitelists() {
    // GIVEN
    final String whitelistPage = "Projet:Correction syntaxique/Erreur syntaxique 048/Liste blanche";
    final String configuredWhitelistPage = whitelistPage.replace(' ', '_');
    givenTranslationPage(
        """
         error_048_prio_frwiki=3 END
         error_048_whitelistpage_frwiki=%s END
         error_002_prio_frwiki=3 END
         error_002_whitelist_frwiki=
          Lyon
          Marseille END
        """
            .formatted(configuredWhitelistPage),
        Map.of());
    mockApiRevisions
        .givenRetrieveRevisionsByTitle(
            WIKI, List.of(configuredWhitelistPage), ConfigurationPageReader.CONTENT_QUERY)
        .willReturn(
            List.of(
                WPCleanerConfigurationExpectations.createPage(
                    whitelistPage,
                    "Liste blanche de {{numéro|48}}.\n* [[Paris]] : raison\n* [[:Nice|Nice]]")));
    givenParametersPage(null);

    // WHEN
    loader.execute(WIKI, ProgressTracker.NOOP);

    // THEN
    mediaWikiServer.verify();
    final CheckWikiConfiguration configuration = repository.getConfiguration(WIKI);
    Assertions.assertThat(configuration.getAlgorithm(48))
        .map(CheckAlgorithmConfiguration::whitelist)
        .hasValue(Set.of(new PageTitle(0, "Paris"), new PageTitle(0, "Nice")));
    Assertions.assertThat(configuration.getAlgorithm(2))
        .map(CheckAlgorithmConfiguration::whitelist)
        .hasValue(Set.of(new PageTitle(0, "Lyon"), new PageTitle(0, "Marseille")));
  }

  @DisplayName("should retrieve the whitelists only for active algorithms available")
  @Test
  void loadWhitelistsOfInactiveOrUnavailableAlgorithms() {
    // GIVEN
    givenTranslationPage(
        """
         error_048_prio_frwiki=0 END
         error_048_whitelistpage_frwiki=Liste blanche 048 END
         error_001_prio_frwiki=3 END
         error_001_whitelistpage_frwiki=Liste blanche 001 END
        """,
        Map.of());

    // WHEN
    loader.execute(WIKI, ProgressTracker.NOOP);

    // THEN
    mediaWikiServer.verify();
    final CheckWikiConfiguration configuration = repository.getConfiguration(WIKI);
    Assertions.assertThat(configuration.getAlgorithm(48))
        .map(CheckAlgorithmConfiguration::whitelist)
        .hasValue(Set.of());
    Assertions.assertThat(configuration.getAlgorithm(1))
        .map(CheckAlgorithmConfiguration::whitelist)
        .hasValue(Set.of());
  }

  private void givenTranslationPage(
      final String translation, final Map<String, String> userProperties) {
    wpcleanerRepository.setConfiguration(
        WIKI,
        new WPCleanerConfiguration(
            Map.of("check_wiki_translation_page", TRANSLATION_PAGE), userProperties));
    WPCleanerConfigurationExpectations.givenPage(
        mockApiRevisions, WIKI, TRANSLATION_PAGE, translation);
  }

  @DisplayName("should load the parameters of the algorithms from their YAML configuration page")
  @Test
  void loadParameters() {
    // GIVEN
    givenTranslationPage(
        """
         error_048_prio_frwiki=3 END
         error_048_imagemap_frwiki=false END
        """,
        Map.of());
    givenParametersPage(
        """
        Configuration of error 48.
        <syntaxhighlight lang="yaml">
        # Links in these templates are ignored
        ignore_templates:
          - Lien web
        ignore_groups:
          - citations
        imagemap: true
        template_groups:
          citations: [Ouvrage, Article]
        </syntaxhighlight>
        """);

    // WHEN
    final List<LoginWarning> warnings = loader.execute(WIKI, ProgressTracker.NOOP);

    // THEN
    mediaWikiServer.verify();
    Assertions.assertThat(warnings).isEmpty();
    Assertions.assertThat(repository.getConfiguration(WIKI).getAlgorithm(48))
        .hasValueSatisfying(
            algorithm -> {
              Assertions.assertThat(algorithm.getBoolean("imagemap")).isTrue();
              Assertions.assertThat(algorithm.getStrings("ignore_templates"))
                  .containsExactly("Lien web");
              Assertions.assertThat(algorithm.getStrings("ignore_groups"))
                  .containsExactly("citations");
              Assertions.assertThat(
                      algorithm.getTemplateGroups("template_groups").getTemplateNames("+citations"))
                  .containsExactlyInAnyOrder("Ouvrage", "Article");
            });
  }

  @DisplayName("should keep the legacy parameters and warn when the YAML configuration is invalid")
  @Test
  void loadInvalidParameters() {
    // GIVEN
    givenTranslationPage(
        """
         error_048_prio_frwiki=3 END
         error_048_imagemap_frwiki=true END
        """,
        Map.of());
    givenParametersPage(
        """
        <syntaxhighlight lang="yaml">
        imagemap: [false
        </syntaxhighlight>
        """);

    // WHEN
    final List<LoginWarning> warnings = loader.execute(WIKI, ProgressTracker.NOOP);

    // THEN
    mediaWikiServer.verify();
    Assertions.assertThat(warnings)
        .singleElement()
        .satisfies(
            warning -> {
              Assertions.assertThat(warning.page()).isEqualTo(getParametersPage());
              Assertions.assertThat(warning.message()).startsWith("Invalid YAML: ");
            });
    Assertions.assertThat(repository.getConfiguration(WIKI).getAlgorithm(48))
        .hasValueSatisfying(
            algorithm -> {
              Assertions.assertThat(algorithm.parameters()).isNull();
              Assertions.assertThat(algorithm.getBoolean("imagemap")).isTrue();
            });
  }

  @DisplayName("should ignore and warn about a configuration page without a YAML block")
  @Test
  void loadParametersWithoutYaml() {
    // GIVEN
    givenTranslationPage(" error_048_prio_frwiki=3 END", Map.of());
    givenParametersPage("<syntaxhighlight lang=\"properties\">imagemap=true</syntaxhighlight>");

    // WHEN
    final List<LoginWarning> warnings = loader.execute(WIKI, ProgressTracker.NOOP);

    // THEN
    mediaWikiServer.verify();
    Assertions.assertThat(warnings)
        .containsExactly(new LoginWarning(getParametersPage(), "No YAML configuration found"));
    Assertions.assertThat(repository.getConfiguration(WIKI).getAlgorithm(48))
        .hasValueSatisfying(algorithm -> Assertions.assertThat(algorithm.parameters()).isNull());
  }

  private void givenParametersPage(@Nullable final String content) {
    WPCleanerConfigurationExpectations.givenPage(
        mockApiRevisions, WIKI, getParametersPage(), content);
  }

  private String getParametersPage() {
    return CheckAlgorithmParametersReader.getParametersPage(
        namespaceRepository.getNamespaces(WIKI), 48);
  }
}
