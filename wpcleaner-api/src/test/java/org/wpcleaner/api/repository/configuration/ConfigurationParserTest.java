package org.wpcleaner.api.repository.configuration;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ConfigurationParserTest {

  @DisplayName("should parse a value ending on the same line")
  @Test
  void parseSingleLineValue() {
    // GIVEN
    final String text = " error_048_prio_frwiki=3 END";

    // WHEN
    final Map<String, String> properties = ConfigurationParser.parse(text);

    // THEN
    Assertions.assertThat(properties).containsExactly(Map.entry("error_048_prio_frwiki", "3"));
  }

  @DisplayName("should parse a value spanning several lines")
  @Test
  void parseMultiLineValue() {
    // GIVEN
    final String text =
        """
        general_anchor_templates=
          Ancre|1|true END
        general_url_templates=
          Lire en ligne|lien
          Ouvrage|lire en ligne
         END
        """;

    // WHEN
    final Map<String, String> properties = ConfigurationParser.parse(text);

    // THEN
    Assertions.assertThat(properties)
        .containsOnly(
            Map.entry("general_anchor_templates", "Ancre|1|true"),
            Map.entry("general_url_templates", "Lire en ligne|lien\n  Ouvrage|lire en ligne"));
  }

  @DisplayName("should ignore comments and lines that are not properties")
  @Test
  void parseIgnoredLines() {
    // GIVEN
    final String text =
        """
        # check_wiki_force= true END
        |URL-1=Projet:Correction_syntaxique
        <syntaxhighlight lang="Properties">
         check_wiki_project_page=P:CS END
        """;

    // WHEN
    final Map<String, String> properties = ConfigurationParser.parse(text);

    // THEN
    Assertions.assertThat(properties).containsExactly(Map.entry("check_wiki_project_page", "P:CS"));
  }

  @DisplayName("should parse empty values")
  @Test
  void parseEmptyValues() {
    // GIVEN
    final String text =
        """
        start_text_frwiki= END
        description_text_frwiki=END
        project=frwiki END
        """;

    // WHEN
    final Map<String, String> properties = ConfigurationParser.parse(text);

    // THEN
    Assertions.assertThat(properties)
        .containsOnly(
            Map.entry("start_text_frwiki", ""),
            Map.entry("description_text_frwiki", ""),
            Map.entry("project", "frwiki"));
  }

  @DisplayName("should keep the rest of the text for a value without end")
  @Test
  void parseValueWithoutEnd() {
    // GIVEN
    final String text = "check_wiki_comment=[[P:CS|Correction syntaxique]]\nsuite";

    // WHEN
    final Map<String, String> properties = ConfigurationParser.parse(text);

    // THEN
    Assertions.assertThat(properties)
        .containsExactly(Map.entry("check_wiki_comment", "[[P:CS|Correction syntaxique]]\nsuite"));
  }

  @DisplayName("should keep the last value of a property defined several times")
  @Test
  void parseRedefinedProperty() {
    // GIVEN
    final String text = "project=enwiki END\nproject=frwiki END";

    // WHEN
    final Map<String, String> properties = ConfigurationParser.parse(text);

    // THEN
    Assertions.assertThat(properties).containsExactly(Map.entry("project", "frwiki"));
  }
}
