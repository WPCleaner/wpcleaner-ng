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
class CrBetweenCategoryFixTest {

  @Autowired private CrBetweenCategoryFix fix;
  @Autowired private WPCleanerConfigurationRepository wpcleanerRepository;

  @BeforeEach
  void setUp() {
    FormattingTestHelper.givenConfiguration(wpcleanerRepository, Map.of());
  }

  @DisplayName("should leave the text unchanged without configuration")
  @Test
  void unchangedWithoutOption() {
    assertFixed("[[Catégorie:A]]\n\n[[Catégorie:B]]", "[[Catégorie:A]]\n\n[[Catégorie:B]]");
  }

  @DisplayName("should normalize line ends between categories")
  @Test
  void normalizeLineEnds() {
    givenOption("1");
    assertFixed(
        "[[Catégorie:A]]\n\n[[Catégorie:B]] [[Catégorie:C]]\n[[Catégorie:D]]",
        "[[Catégorie:A]]\n[[Catégorie:B]]\n[[Catégorie:C]]\n[[Catégorie:D]]");
  }

  @DisplayName("should leave categories separated by text unchanged")
  @Test
  void unchangedCategoriesSeparatedByText() {
    givenOption("1");
    assertFixed(
        "[[Catégorie:A]]\nText\n\n[[Catégorie:B]]", "[[Catégorie:A]]\nText\n\n[[Catégorie:B]]");
  }

  private void givenOption(final String value) {
    FormattingTestHelper.givenConfiguration(
        wpcleanerRepository, Map.of("auto_cr_between_category", value));
  }

  private void assertFixed(final String text, final String expected) {
    Assertions.assertThat(FormattingTestHelper.fix(fix, text)).isEqualTo(expected);
  }
}
