package org.wpcleaner.api.hook.login;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.client.MockRestServiceServer;
import org.wpcleaner.api.ApiSpringBootTest;
import org.wpcleaner.api.api.query.prop.revisions.MockApiRevisions;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.api.wiki.definition.WikipediaDefinitions;

@ApiSpringBootTest
class ConfigurationPageReaderTest {

  private static final WikiDefinition WIKI = WikipediaDefinitions.FR;

  @Autowired private ConfigurationPageReader pageReader;
  @Autowired private MockApiRevisions mockApiRevisions;
  @Autowired private MockRestServiceServer mediaWikiServer;

  @BeforeEach
  void setUp() {
    mediaWikiServer.reset();
  }

  @DisplayName("should retrieve the contents of the pages by groups of 50 pages")
  @Test
  void retrieveContents() {
    // GIVEN
    final List<String> titles =
        IntStream.rangeClosed(1, 51).mapToObj("Liste blanche %03d"::formatted).toList();
    WPCleanerConfigurationExpectations.givenPages(
        mockApiRevisions, WIKI, titles.subList(0, 50), Map.of(titles.getFirst(), "* [[Paris]]"));
    WPCleanerConfigurationExpectations.givenPages(
        mockApiRevisions, WIKI, titles.subList(50, 51), Map.of(titles.getLast(), "* [[Lyon]]"));

    // WHEN
    final Map<String, String> contents = pageReader.retrieveContents(WIKI, titles);

    // THEN
    mediaWikiServer.verify();
    Assertions.assertThat(contents)
        .containsOnly(
            Map.entry(titles.getFirst(), "* [[Paris]]"), Map.entry(titles.getLast(), "* [[Lyon]]"));
  }
}
