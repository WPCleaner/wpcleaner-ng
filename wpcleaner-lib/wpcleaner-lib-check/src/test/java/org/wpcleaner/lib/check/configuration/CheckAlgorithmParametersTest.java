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
