package org.wpcleaner.api.wiki.definition;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.wpcleaner.api.wiki.builder.WikiBuilder;

class WikiDefinitionTest {

  @DisplayName("apiUrl of a known wiki has a single separator after the host")
  @Test
  void apiUrlOfKnownWiki() {
    Assertions.assertThat(WikipediaDefinitions.EN.apiUrl())
        .isEqualTo("https://en.wikipedia.org/w/api.php");
  }

  @DisplayName("pageUrl of a known wiki has a single separator after the host")
  @Test
  void pageUrlOfKnownWiki() {
    Assertions.assertThat(WikipediaDefinitions.EN.pageUrl("Wikipedia:WPCleanerNG/Login"))
        .isEqualTo("https://en.wikipedia.org/wiki/Wikipedia:WPCleanerNG/Login");
  }

  @DisplayName("Paths without leading separator are completed")
  @Test
  void pathsWithoutLeadingSeparator() {
    final WikiDefinition wiki =
        WikiBuilder.ltr("en", "Example", "wiki.example.org")
            .withApiPath("w/api.php")
            .withIndexPath("w/index.php")
            .withWikiPath("wiki")
            .build();

    Assertions.assertThat(wiki.apiUrl()).isEqualTo("https://wiki.example.org/w/api.php");
    Assertions.assertThat(wiki.pageUrl("Main Page"))
        .isEqualTo("https://wiki.example.org/wiki/Main_Page");
    Assertions.assertThat(wiki.pageUri("Main Page"))
        .hasToString("https://wiki.example.org/wiki/Main_Page");
    Assertions.assertThat(wiki.indexPath()).isEqualTo("/w/index.php");
  }
}
