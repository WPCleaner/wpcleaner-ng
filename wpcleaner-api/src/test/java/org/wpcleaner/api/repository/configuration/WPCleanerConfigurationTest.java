package org.wpcleaner.api.repository.configuration;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class WPCleanerConfigurationTest {

  @DisplayName("should combine the general and the user lines of a combined parameter")
  @Test
  void getCombinedLines() {
    // GIVEN
    final WPCleanerConfiguration configuration =
        new WPCleanerConfiguration(
            Map.of("general_template_groups", "\n  Article|Article|Cite journal\n"),
            Map.of("general_template_groups", "Liens|Lien web"));

    // WHEN
    final List<String> lines = configuration.getLines(WPCleanerParameter.TEMPLATE_GROUPS);

    // THEN
    Assertions.assertThat(lines).containsExactly("Article|Article|Cite journal", "Liens|Lien web");
  }

  @DisplayName("should take the user lines instead of the general lines of a parameter")
  @Test
  void getUserLines() {
    // GIVEN
    final WPCleanerConfiguration configuration =
        new WPCleanerConfiguration(
            Map.of("general_apostrophe_template", "'"),
            Map.of("general_apostrophe_template", "Apostrophe"));

    // WHEN
    final List<String> lines = configuration.getLines(WPCleanerParameter.APOSTROPHE_TEMPLATE);

    // THEN
    Assertions.assertThat(lines).containsExactly("Apostrophe");
  }
}
