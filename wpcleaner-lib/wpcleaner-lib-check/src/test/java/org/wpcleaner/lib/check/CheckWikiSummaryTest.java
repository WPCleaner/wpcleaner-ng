package org.wpcleaner.lib.check;

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
import org.wpcleaner.api.ApiSpringBootTest;
import org.wpcleaner.api.repository.configuration.WPCleanerConfiguration;
import org.wpcleaner.api.repository.configuration.WPCleanerConfigurationRepository;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.api.wiki.definition.WikipediaDefinitions;
import org.wpcleaner.lib.check.configuration.CheckWikiConfiguration;
import org.wpcleaner.lib.check.configuration.CheckWikiConfigurationRepository;

@ApiSpringBootTest
class CheckWikiSummaryTest {

  private static final WikiDefinition WIKI = WikipediaDefinitions.FR;
  private static final String HEAD = "Lien interne vers le titre de l’article";

  @Autowired private CheckWikiSummary summary;
  @Autowired private CheckAlgorithmRegistry algorithmRegistry;
  @Autowired private CheckWikiConfigurationRepository checkWikiRepository;
  @Autowired private WPCleanerConfigurationRepository wpcleanerRepository;

  @BeforeEach
  void setUp() {
    checkWikiRepository.setConfiguration(
        WIKI,
        CheckWikiConfiguration.of(
            Map.of("error_048_prio_frwiki", "3", "error_048_head_frwiki", HEAD),
            Map.of(),
            "frwiki"));
  }

  @DisplayName("should add the names of the fixed algorithms after the comment")
  @Test
  void createWithComment() {
    // GIVEN
    givenConfiguration(Map.of("check_wiki_comment", "[[P:CS|Correction syntaxique]]"));

    // WHEN
    final String result = summary.create(WIKI, List.of(getAlgorithm()));

    // THEN
    Assertions.assertThat(result).isEqualTo("[[P:CS|Correction syntaxique]] (%s)".formatted(HEAD));
  }

  @DisplayName("should put the names of the fixed algorithms in the comment")
  @Test
  void createWithCommentPlaceholder() {
    // GIVEN
    givenConfiguration(Map.of("check_wiki_comment", "Correction : {0}"));

    // WHEN
    final String result = summary.create(WIKI, List.of(getAlgorithm()));

    // THEN
    Assertions.assertThat(result).isEqualTo("Correction : %s".formatted(HEAD));
  }

  @DisplayName("should mention the project page without comment")
  @Test
  void createWithProjectPage() {
    // GIVEN
    givenConfiguration(Map.of("check_wiki_project_page", "P:CS"));

    // WHEN
    final String result = summary.create(WIKI, List.of(getAlgorithm()));

    // THEN
    Assertions.assertThat(result).isEqualTo("Fixed using [[P:CS]] (%s)".formatted(HEAD));
  }

  @DisplayName("should mention the Check Wiki project without comment nor project page")
  @Test
  void createWithoutConfiguration() {
    // GIVEN
    givenConfiguration(Map.of());

    // WHEN
    final String result = summary.create(WIKI, List.of(getAlgorithm()));

    // THEN
    Assertions.assertThat(result).isEqualTo("Fixed using Check Wiki project (%s)".formatted(HEAD));
  }

  @DisplayName("should be empty without fixed algorithm")
  @Test
  void createWithoutFixedAlgorithm() {
    // GIVEN
    givenConfiguration(Map.of("check_wiki_comment", "[[P:CS|Correction syntaxique]]"));

    // WHEN
    final String result = summary.create(WIKI, List.of());

    // THEN
    Assertions.assertThat(result).isEmpty();
  }

  private void givenConfiguration(final Map<String, String> properties) {
    wpcleanerRepository.setConfiguration(WIKI, new WPCleanerConfiguration(properties, Map.of()));
  }

  private CheckAlgorithm getAlgorithm() {
    return algorithmRegistry.findAlgorithm(48).orElseThrow();
  }
}
