package org.wpcleaner.lib.check.configuration;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CheckAlgorithmParametersTest {

  @DisplayName("should read booleans, lists of strings and groups of strings")
  @Test
  void parse() {
    // GIVEN
    final String yaml =
        """
        # Comment
        flag: true
        text: not a boolean
        single: Lien web
        names:
          - Lien web
          - 2010
          - [nested]
        groups:
          citations: [Ouvrage, Article]
          single: Lien
        """;

    // WHEN
    final CheckAlgorithmParameters parameters = CheckAlgorithmParameters.parse(yaml);

    // THEN
    Assertions.assertThat(parameters.getBoolean("flag")).contains(true);
    Assertions.assertThat(parameters.getBoolean("text")).isEmpty();
    Assertions.assertThat(parameters.getBoolean("missing")).isEmpty();
    Assertions.assertThat(parameters.getStrings("single")).containsExactly("Lien web");
    Assertions.assertThat(parameters.getStrings("names")).containsExactly("Lien web", "2010");
    Assertions.assertThat(parameters.getStrings("missing")).isEmpty();
    Assertions.assertThat(parameters.getStringLists("groups"))
        .isEqualTo(Map.of("citations", List.of("Ouvrage", "Article"), "single", List.of("Lien")));
    Assertions.assertThat(parameters.getStringLists("names")).isEmpty();
  }

  @DisplayName("should read unquoted names as written, and only true or false as booleans")
  @Test
  void parseNamesAsWritten() {
    // GIVEN
    final String yaml =
        """
        flag: TRUE
        other: False
        legacy: yes
        names: [No, yes, on, off, 010, 1_000, 2026-11-10, 1.5]
        groups:
          No: [Off]
        """;

    // WHEN
    final CheckAlgorithmParameters parameters = CheckAlgorithmParameters.parse(yaml);

    // THEN
    Assertions.assertThat(parameters.getBoolean("flag")).contains(true);
    Assertions.assertThat(parameters.getBoolean("other")).contains(false);
    Assertions.assertThat(parameters.getBoolean("legacy")).isEmpty();
    Assertions.assertThat(parameters.getStrings("names"))
        .containsExactly("No", "yes", "on", "off", "010", "1_000", "2026-11-10", "1.5");
    Assertions.assertThat(parameters.getStringLists("groups"))
        .isEqualTo(Map.of("No", List.of("Off")));
  }

  @DisplayName("should reject a boolean in a list of names")
  @Test
  void parseBooleanName() {
    // WHEN / THEN
    Assertions.assertThatThrownBy(() -> CheckAlgorithmParameters.parse("names: [Lien, true]\n"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Value \"true\" in names is not a name, write it between quotes");
  }

  @DisplayName("should reject a boolean as the name of a group")
  @Test
  void parseBooleanGroupName() {
    // WHEN / THEN
    Assertions.assertThatThrownBy(() -> CheckAlgorithmParameters.parse("groups:\n  true: [Lien]\n"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Value \"true\" in groups is not a name, write it between quotes");
  }

  @DisplayName("should reject an empty value in a list of names")
  @Test
  void parseEmptyName() {
    // WHEN / THEN
    Assertions.assertThatThrownBy(
            () -> CheckAlgorithmParameters.parse("groups:\n  Quotes:\n    - Quote\n    -\n"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Empty value in groups/Quotes");
  }

  @DisplayName("should accept quoted booleans in a list of names")
  @Test
  void parseQuotedBooleanName() {
    // WHEN
    final CheckAlgorithmParameters parameters =
        CheckAlgorithmParameters.parse("names: [\"true\", 'False']\n");

    // THEN
    Assertions.assertThat(parameters.getStrings("names")).containsExactly("true", "False");
  }

  @DisplayName("should read lists of objects")
  @Test
  void parseObjects() {
    // GIVEN
    final String yaml =
        """
        objects:
          - template: Suivi
            parameters: [catégorie, 2]
          - not an object
        """;

    // WHEN
    final CheckAlgorithmParameters parameters = CheckAlgorithmParameters.parse(yaml);

    // THEN
    Assertions.assertThat(parameters.getObjects("objects"))
        .singleElement()
        .satisfies(
            object -> {
              Assertions.assertThat(object.getStrings("template")).containsExactly("Suivi");
              Assertions.assertThat(object.getStrings("parameters"))
                  .containsExactly("catégorie", "2");
            });
    Assertions.assertThat(parameters.getObjects("missing")).isEmpty();
  }

  @DisplayName("should reject a boolean in a list of names inside an object")
  @Test
  void parseBooleanNameInObject() {
    // WHEN / THEN
    Assertions.assertThatThrownBy(
            () -> CheckAlgorithmParameters.parse("objects:\n  - parameters: [a, false]\n"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Value \"false\" in objects/parameters is not a name, write it between quotes");
  }

  @DisplayName("should read an empty YAML document as empty parameters")
  @Test
  void parseEmpty() {
    // WHEN
    final CheckAlgorithmParameters parameters = CheckAlgorithmParameters.parse("# Nothing yet\n");

    // THEN
    Assertions.assertThat(parameters).isEqualTo(CheckAlgorithmParameters.EMPTY);
  }

  @DisplayName("should reject a YAML document that isn't a mapping")
  @Test
  void parseNotMapping() {
    // WHEN / THEN
    Assertions.assertThatThrownBy(() -> CheckAlgorithmParameters.parse("- a\n- b\n"))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @DisplayName("should reject an invalid YAML document")
  @Test
  void parseInvalid() {
    // WHEN / THEN
    Assertions.assertThatThrownBy(() -> CheckAlgorithmParameters.parse("flag: [true\n"))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @DisplayName("should not create Java objects from YAML tags")
  @Test
  void parseJavaObject() {
    // WHEN / THEN
    Assertions.assertThatThrownBy(
            () -> CheckAlgorithmParameters.parse("object: !!java.io.File [\"/tmp\"]\n"))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
