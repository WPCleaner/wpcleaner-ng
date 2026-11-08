package org.wpcleaner.lib.check.server;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.client.MockRestServiceServer;
import org.wpcleaner.api.ApiSpringBootTest;
import org.wpcleaner.api.wiki.builder.WikiBuilder;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.api.wiki.definition.WikipediaDefinitions;

@ApiSpringBootTest
class CheckWikiServerTest {

  private static final WikiDefinition WIKI = WikipediaDefinitions.FR;

  @Autowired private CheckWikiServer checkWikiServer;
  @Autowired private MockRestServiceServer mediaWikiServer;

  @BeforeEach
  void setUp() {
    mediaWikiServer.reset();
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
}
