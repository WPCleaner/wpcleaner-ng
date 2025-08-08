package org.wpcleaner.api.wiki.definition;

/*
 * SPDX-FileCopyrightText: © 2024 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.InstanceOfAssertFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;

@SpringBootTest(classes = KnownDefinitionsTest.TestConfig.class)
class KnownDefinitionsTest {

  @Autowired private KnownDefinitions knownDefinitions;

  @EnableAutoConfiguration
  @ComponentScan(basePackages = "org.wpcleaner")
  static class TestConfig {}

  @Test
  @DisplayName("Verify that known definitions contain various types")
  void variousTypesOfKnowDefinitions() {
    // WHEN
    final List<WikiDefinition> definitions = knownDefinitions.getDefinitions();

    // THEN
    Assertions.assertThat(definitions)
        .isNotEmpty()
        .contains(WikibooksDefinitions.BN)
        .contains(WikipediaDefinitions.FR)
        .contains(WikiquoteDefinitions.FR)
        .contains(WikisourceDefinitions.FR)
        .contains(WikiversityDefinitions.FR)
        .contains(WikivoyageDefinitions.FR)
        .contains(WiktionaryDefinitions.FR);
  }

  @Test
  @DisplayName("Verify that wiki definition code is unique")
  void uniqueCode() {
    // WHEN
    final List<WikiDefinition> definitions = knownDefinitions.getDefinitions();

    // THEN
    definitions.forEach(
        definition ->
            Assertions.assertThat(definitions)
                .filteredOn(Predicate.not(Predicate.isEqual(definition)))
                .filteredOn("code", definition.code())
                .isEmpty());
  }

  @Test
  @DisplayName("Verify that wiki definition check wiki code is unique")
  void uniqueCheckWikiCode() {
    // WHEN
    final List<WikiDefinition> definitions = knownDefinitions.getDefinitions();

    // THEN
    definitions.stream()
        .filter(definition -> Objects.nonNull(definition.checkWikiCode()))
        .forEach(
            definition ->
                Assertions.assertThat(definitions)
                    .filteredOn(Predicate.not(Predicate.isEqual(definition)))
                    .filteredOn("checkWikiCode", definition.checkWikiCode())
                    .isEmpty());
  }

  @Test
  @DisplayName("Verify that first wiki definition is for Wikipedia")
  void wikipediaIsFirst() {
    // WHEN
    final List<WikiDefinition> definitions = knownDefinitions.getDefinitions();

    // THEN
    Assertions.assertThat(definitions)
        .first(
            new InstanceOfAssertFactory<>(
                WikiDefinition.class, WikiDefinitionAssertions::assertThat))
        .assertThatHosts()
        .anyMatch(host -> host.contains(".wikipedia.org"));
  }
}
