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
class CrBeforeCategoryFixTest {

  @Autowired private CrBeforeCategoryFix fix;
  @Autowired private WPCleanerConfigurationRepository wpcleanerRepository;

  @BeforeEach
  void setUp() {
    FormattingTestHelper.givenConfiguration(wpcleanerRepository, Map.of());
  }

  @DisplayName("should leave the text unchanged without configuration")
  @Test
  void unchangedWithoutOption() {
    assertFixed("Text\n[[Catégorie:A]]", "Text\n[[Catégorie:A]]");
  }

  @DisplayName("should normalize line ends before the categories")
  @Test
  void normalizeLineEnds() {
    givenOption("2");
    assertFixed("Text\n[[Catégorie:A]]", "Text\n\n[[Catégorie:A]]");
    assertFixed("Text\n\n \n\n[[Catégorie:A]]", "Text\n\n[[Catégorie:A]]");
  }

  @DisplayName("should normalize line ends before DEFAULTSORT")
  @Test
  void normalizeLineEndsBeforeDefaultSort() {
    givenOption("2");
    assertFixed(
        "Text\n{{DEFAULTSORT:B}}\n[[Catégorie:A]]", "Text\n\n{{DEFAULTSORT:B}}\n[[Catégorie:A]]");
  }

  @DisplayName("should leave categories unchanged before a title or on the same line")
  @Test
  void unchangedCategories() {
    givenOption("2");
    assertFixed(
        "Text\n[[Catégorie:A]]\n== Title ==\nOther", "Text\n[[Catégorie:A]]\n== Title ==\nOther");
    assertFixed("Text [[Catégorie:A]]", "Text [[Catégorie:A]]");
  }

  private void givenOption(final String value) {
    FormattingTestHelper.givenConfiguration(
        wpcleanerRepository, Map.of("auto_cr_before_category", value));
  }

  private void assertFixed(final String text, final String expected) {
    Assertions.assertThat(FormattingTestHelper.fix(fix, text)).isEqualTo(expected);
  }
}
