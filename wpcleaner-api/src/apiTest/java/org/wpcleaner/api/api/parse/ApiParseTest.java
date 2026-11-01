package org.wpcleaner.api.api.parse;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.EnumSet;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.wpcleaner.api.SpringBootTestConfig;
import org.wpcleaner.api.wiki.definition.WikimediaDefinitions;

@SpringBootTest(classes = SpringBootTestConfig.class)
class ApiParseTest {

  @Autowired private ApiParse apiParse;

  @DisplayName("Parse a text with all properties")
  @Test
  void parseText() {
    // GIVEN
    final ParseQuery query =
        new ParseQuery(
            "WPCleanerNG parse test",
            "== Title ==\n[[Main Page|main]] [[Category:Test]] {{tl|x}}<!-- c --> https://example.org/",
            EnumSet.allOf(ParseParameters.Properties.class));

    // WHEN
    final Parse parse = apiParse.parse(WikimediaDefinitions.META, query);

    // THEN
    Assertions.assertThat(parse.title()).as("title").isEqualTo("WPCleanerNG parse test");
    Assertions.assertThat(parse.links()).as("links").contains(new Link(0, "Main Page", true));
    Assertions.assertThat(parse.categories())
        .as("categories")
        .anySatisfy(category -> Assertions.assertThat(category.category()).isEqualTo("Test"));
    Assertions.assertThat(parse.externalLinks())
        .as("externalLinks")
        .contains("https://example.org/");
    Assertions.assertThat(parse.parseTree()).as("parseTree").contains("<comment>");
    Assertions.assertThat(parse.tableOfContents()).as("tableOfContents").isNotNull();
    Assertions.assertThat(parse.tableOfContents().sections())
        .as("sections")
        .anySatisfy(section -> Assertions.assertThat(section.headingLevel()).isEqualTo(2));
  }
}
