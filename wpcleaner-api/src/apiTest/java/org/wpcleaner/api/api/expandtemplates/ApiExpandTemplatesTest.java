package org.wpcleaner.api.api.expandtemplates;

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
class ApiExpandTemplatesTest {

  @Autowired private ApiExpandTemplates apiExpandTemplates;

  @DisplayName("Expand a text with all properties")
  @Test
  void expandText() {
    // GIVEN
    final ExpandTemplatesQuery query =
        ExpandTemplatesQuery.emptyBuilder()
            .includeComments(true)
            .properties(EnumSet.allOf(ExpandTemplatesParameters.Properties.class))
            .text(
                "<!-- c -->{{PAGENAME}} {{#if:x|yes|no}} {{DISPLAYTITLE:WPCleanerNG}}"
                    + " {{#invoke:WPCleanerNG missing module|main}} {{CURRENTTIME}}")
            .title("WPCleanerNG")
            .build();

    // WHEN
    final ExpandTemplates expandTemplates =
        apiExpandTemplates.expandTemplates(WikimediaDefinitions.META, query);

    // THEN
    Assertions.assertThat(expandTemplates.wikiText())
        .as("wikiText")
        .startsWith("<!-- c -->WPCleanerNG yes ");
    Assertions.assertThat(expandTemplates.parseTree()).as("parseTree").contains("<comment>");
    Assertions.assertThat(expandTemplates.properties())
        .as("properties")
        .containsEntry("displaytitle", "WPCleanerNG");
    Assertions.assertThat(expandTemplates.categories()).as("categories").isNotEmpty();
    Assertions.assertThat(expandTemplates.volatileOutput()).as("volatileOutput").isNotNull();
    Assertions.assertThat(expandTemplates.ttl()).as("ttl").isNotNull();
    Assertions.assertThat(expandTemplates.encodedJsConfigVars())
        .as("encodedJsConfigVars")
        .isNotNull();
  }
}
