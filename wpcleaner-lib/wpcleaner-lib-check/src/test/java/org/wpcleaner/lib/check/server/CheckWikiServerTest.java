package org.wpcleaner.lib.check.server;

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
import org.wpcleaner.api.analysis.PageAnalysis;
import org.wpcleaner.api.analysis.PageAnalysisTestFactory;
import org.wpcleaner.api.api.ConnectedUser;
import org.wpcleaner.api.wiki.builder.WikiBuilder;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.api.wiki.definition.WikipediaDefinitions;
import org.wpcleaner.lib.check.CheckAlgorithm;
import org.wpcleaner.lib.check.CheckAlgorithmRegistry;
import org.wpcleaner.lib.check.CheckError;
import org.wpcleaner.lib.check.CheckSpringBootTest;
import org.wpcleaner.lib.check.configuration.CheckWikiConfiguration;
import org.wpcleaner.lib.check.configuration.CheckWikiConfigurationRepository;

@CheckSpringBootTest
class CheckWikiServerTest {

  private static final WikiDefinition WIKI = WikipediaDefinitions.FR;

  @Autowired private CheckWikiServer checkWikiServer;
  @Autowired private MockRestServiceServer mediaWikiServer;
  @Autowired private CheckAlgorithmRegistry registry;
  @Autowired private CheckWikiConfigurationRepository configurationRepository;

  @BeforeEach
  void setUp() {
    mediaWikiServer.reset();
    configurationRepository.logout(new ConnectedUser(false, WIKI, "", false, List.of(), List.of()));
  }

  @DisplayName("should retrieve the pages with an error from the Check Wiki server")
  @Test
  void retrievePages() {
    // GIVEN
    final List<String> titles = List.of("Abbaye d'Abondance", "Équipe du Chili féminine");
    new MockCheckWikiServer(mediaWikiServer).givenPages(WIKI, 48, 100, titles);

    // WHEN
    final List<String> pages = checkWikiServer.retrievePages(WIKI, 48, 100);

    // THEN
    mediaWikiServer.verify();
    Assertions.assertThat(pages).isEqualTo(titles);
  }

  @DisplayName("should retrieve no page for a wiki without Check Wiki code")
  @Test
  void retrievePagesWithoutCheckWikiCode() {
    // GIVEN
    final WikiDefinition wiki = WikiBuilder.ltr("en", "Test", "test.example.org").build();

    // WHEN
    final List<String> pages = checkWikiServer.retrievePages(wiki, 48, 100);

    // THEN
    mediaWikiServer.verify();
    Assertions.assertThat(pages).isEmpty();
  }

  @DisplayName("should mark the fixed errors on the Check Wiki server")
  @Test
  void markAsFixed() {
    // GIVEN
    givenConfiguration("3");
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(WIKI, "Paris", "");
    new MockCheckWikiServer(mediaWikiServer).givenMarkAsFixed(WIKI, 48, "Paris");

    // WHEN
    final List<CheckAlgorithm> marked =
        checkWikiServer.markAsFixed(analysis, List.of(algorithm048()));

    // THEN
    mediaWikiServer.verify();
    Assertions.assertThat(marked).containsExactly(algorithm048());
  }

  @DisplayName("should not mark the fixed errors of a page outside the main namespace")
  @Test
  void markAsFixedOutsideMainNamespace() {
    // GIVEN
    givenConfiguration("3");
    final PageAnalysis analysis =
        PageAnalysisTestFactory.createPageAnalysis(WIKI, "Utilisateur:Paris", "");

    // WHEN
    final List<CheckAlgorithm> marked =
        checkWikiServer.markAsFixed(analysis, List.of(algorithm048()));

    // THEN
    mediaWikiServer.verify();
    Assertions.assertThat(marked).isEmpty();
  }

  @DisplayName("should not mark the fixed errors reserved to bots")
  @Test
  void markAsFixedBotOnly() {
    // GIVEN
    givenConfiguration("4");
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(WIKI, "Paris", "");

    // WHEN
    final List<CheckAlgorithm> marked =
        checkWikiServer.markAsFixed(analysis, List.of(algorithm048()));

    // THEN
    mediaWikiServer.verify();
    Assertions.assertThat(marked).isEmpty();
  }

  @DisplayName("should not mark the fixed errors without a list on the Check Wiki server")
  @Test
  void markAsFixedWithoutList() {
    // GIVEN
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(WIKI, "Paris", "");

    // WHEN
    final List<CheckAlgorithm> marked =
        checkWikiServer.markAsFixed(analysis, List.of(new UnlistedAlgorithm()));

    // THEN
    mediaWikiServer.verify();
    Assertions.assertThat(marked).isEmpty();
  }

  @DisplayName("should not mark the fixed errors for a wiki without Check Wiki code")
  @Test
  void markAsFixedWithoutCheckWikiCode() {
    // GIVEN
    final WikiDefinition wiki = WikiBuilder.ltr("en", "Test", "test.example.org").build();
    final PageAnalysis analysis = PageAnalysisTestFactory.createPageAnalysis(wiki, "Paris", "");

    // WHEN
    final List<CheckAlgorithm> marked =
        checkWikiServer.markAsFixed(analysis, List.of(algorithm048()));

    // THEN
    mediaWikiServer.verify();
    Assertions.assertThat(marked).isEmpty();
  }

  private void givenConfiguration(final String priority) {
    configurationRepository.setConfiguration(
        WIKI,
        CheckWikiConfiguration.of(Map.of("error_048_prio_frwiki", priority), Map.of(), "frwiki"));
  }

  private CheckAlgorithm algorithm048() {
    return registry.findAlgorithm(48).orElseThrow();
  }

  private static final class UnlistedAlgorithm implements CheckAlgorithm {

    @Override
    public int number() {
      return 501;
    }

    @Override
    public List<CheckError> analyze(final PageAnalysis analysis) {
      return List.of();
    }
  }
}
