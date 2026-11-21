package org.wpcleaner.lib.check.formatting;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.wpcleaner.api.repository.configuration.WPCleanerConfigurationRepository;
import org.wpcleaner.lib.check.CheckSpringBootTest;

@CheckSpringBootTest
class SpaceAroundTitleFixTest {

  @Autowired private SpaceAroundTitleFix fix;
  @Autowired private WPCleanerConfigurationRepository wpcleanerRepository;

  @BeforeEach
  void setUp() {
    FormattingTestHelper.givenConfiguration(wpcleanerRepository, Map.of());
  }

  @DisplayName("should leave titles unchanged without configuration")
  @Test
  void unchangedWithoutOption() {
    assertFixed("==Title==\nText", "==Title==\nText");
  }

  @DisplayName("should normalize the number of spaces inside titles")
  @Test
  void normalizeSpaces() {
    givenOption("1");
    assertFixed(
        "==Title==\nText\n===  Other  ===\nText\n== Correct ==\n",
        "== Title ==\nText\n=== Other ===\nText\n== Correct ==\n");
  }

  @DisplayName("should keep the number of spaces inside the accepted range")
  @Test
  void keepSpacesInRange() {
    givenOption("0-1");
    assertFixed("==  Title==\nText\n== Other ==\n", "== Title==\nText\n== Other ==\n");
  }

  @DisplayName("should leave unbalanced titles unchanged")
  @Test
  void unchangedUnbalancedTitles() {
    givenOption("1");
    assertFixed("==Title===\nText", "==Title===\nText");
  }

  private void givenOption(final String value) {
    FormattingTestHelper.givenConfiguration(
        wpcleanerRepository, Map.of("auto_space_around_title", value));
  }

  private void assertFixed(final String text, final String expected) {
    Assertions.assertThat(FormattingTestHelper.fix(fix, text)).isEqualTo(expected);
  }
}
