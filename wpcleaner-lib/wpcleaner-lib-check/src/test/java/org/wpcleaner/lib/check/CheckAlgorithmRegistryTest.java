package org.wpcleaner.lib.check;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.wpcleaner.api.analysis.PageAnalysisTestFactory;
import org.wpcleaner.api.analysis.pagetitle.PageTitle;
import org.wpcleaner.api.api.ConnectedUser;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.api.wiki.definition.WikipediaDefinitions;
import org.wpcleaner.lib.check.configuration.CheckWikiConfiguration;
import org.wpcleaner.lib.check.configuration.CheckWikiConfigurationRepository;

@CheckSpringBootTest
class CheckAlgorithmRegistryTest {

  private static final WikiDefinition WIKI = WikipediaDefinitions.FR;
  private static final String HEAD = "Lien interne vers le titre de l’article";

  @Autowired private CheckAlgorithmRegistry registry;
  @Autowired private CheckWikiConfigurationRepository configurationRepository;

  @BeforeEach
  void setUp() {
    configurationRepository.logout(new ConnectedUser(false, WIKI, "", false, List.of(), List.of()));
  }

  @DisplayName("should find the algorithms by their number")
  @Test
  void findAlgorithm() {
    // GIVEN
    final int number = 48;

    // WHEN
    final Optional<CheckAlgorithm> algorithm = registry.findAlgorithm(number);

    // THEN
    Assertions.assertThat(algorithm).map(CheckAlgorithm::number).contains(number);
    Assertions.assertThat(registry.findAlgorithm(0)).isEmpty();
  }

  @DisplayName("should name the algorithms with their number")
  @Test
  void name() {
    // GIVEN
    final CheckAlgorithm algorithm = registry.findAlgorithm(48).orElseThrow();

    // WHEN
    final String name = algorithm.name();

    // THEN
    Assertions.assertThat(name).isEqualTo("Error No. 48");
  }

  @DisplayName("should give the algorithms active on the wiki")
  @Test
  void getActiveAlgorithms() {
    // GIVEN
    givenConfiguration(Map.of("error_048_prio_frwiki", "3"));

    // WHEN
    final List<CheckAlgorithm> algorithms = registry.getActiveAlgorithms(WIKI);

    // THEN
    Assertions.assertThat(algorithms).extracting(CheckAlgorithm::number).containsExactly(48);
  }

  @DisplayName("should not give the algorithms deactivated on the wiki")
  @Test
  void getActiveAlgorithmsWhenDeactivated() {
    // GIVEN
    givenConfiguration(Map.of("error_048_prio_frwiki", "0"));

    // WHEN
    final List<CheckAlgorithm> algorithms = registry.getActiveAlgorithms(WIKI);

    // THEN
    Assertions.assertThat(algorithms).isEmpty();
  }

  @DisplayName("should not give the algorithms without configuration on the wiki")
  @Test
  void getActiveAlgorithmsWithoutConfiguration() {
    // WHEN
    final List<CheckAlgorithm> algorithms = registry.getActiveAlgorithms(WIKI);

    // THEN
    Assertions.assertThat(algorithms).isEmpty();
  }

  @DisplayName("should not give the algorithms whose whitelist contains the analyzed page")
  @Test
  void getActiveAlgorithmsForWhitelistedPage() {
    // GIVEN
    configurationRepository.setConfiguration(
        WIKI,
        CheckWikiConfiguration.of(Map.of("error_048_prio_frwiki", "3"), Map.of(), "frwiki")
            .withWhitelists(_ -> Set.of(new PageTitle(0, "Paris"))));

    // WHEN
    final List<CheckAlgorithm> whitelistedAlgorithms =
        registry.getActiveAlgorithms(PageAnalysisTestFactory.createPageAnalysis("Paris", ""));
    final List<CheckAlgorithm> otherAlgorithms =
        registry.getActiveAlgorithms(PageAnalysisTestFactory.createPageAnalysis("Lyon", ""));

    // THEN
    Assertions.assertThat(whitelistedAlgorithms).isEmpty();
    Assertions.assertThat(otherAlgorithms).extracting(CheckAlgorithm::number).containsExactly(48);
  }

  @DisplayName("should name the algorithms with their short description on the wiki")
  @Test
  void getName() {
    // GIVEN
    givenConfiguration(Map.of("error_048_prio_frwiki", "3", "error_048_head_frwiki", HEAD));
    final CheckAlgorithm algorithm = registry.findAlgorithm(48).orElseThrow();

    // WHEN
    final String name = registry.getName(WIKI, algorithm);

    // THEN
    Assertions.assertThat(name).isEqualTo(HEAD);
  }

  @DisplayName("should name the algorithms with their number without short description")
  @Test
  void getNameWithoutShortDescription() {
    // GIVEN
    givenConfiguration(Map.of("error_048_prio_frwiki", "3"));
    final CheckAlgorithm algorithm = registry.findAlgorithm(48).orElseThrow();

    // WHEN
    final String name = registry.getName(WIKI, algorithm);

    // THEN
    Assertions.assertThat(name).isEqualTo("Error No. 48");
  }

  private void givenConfiguration(final Map<String, String> properties) {
    configurationRepository.setConfiguration(
        WIKI, CheckWikiConfiguration.of(properties, Map.of(), "frwiki"));
  }
}
