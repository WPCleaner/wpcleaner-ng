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
class CrDefaultSortCategoryFixTest {

  @Autowired private CrDefaultSortCategoryFix fix;
  @Autowired private WPCleanerConfigurationRepository wpcleanerRepository;

  @BeforeEach
  void setUp() {
    FormattingTestHelper.givenConfiguration(wpcleanerRepository, Map.of());
  }

  @DisplayName("should leave the text unchanged without configuration")
  @Test
  void unchangedWithoutOption() {
    assertFixed("{{DEFAULTSORT:A}}\n\n[[Catégorie:B]]", "{{DEFAULTSORT:A}}\n\n[[Catégorie:B]]");
  }

  @DisplayName("should normalize line ends between DEFAULTSORT and categories")
  @Test
  void normalizeLineEnds() {
    givenOption("1");
    assertFixed("{{DEFAULTSORT:A}}\n\n[[Catégorie:B]]", "{{DEFAULTSORT:A}}\n[[Catégorie:B]]");
    assertFixed("{{DEFAULTSORT:A}} [[Catégorie:B]]", "{{DEFAULTSORT:A}}\n[[Catégorie:B]]");
  }

  @DisplayName("should leave DEFAULTSORT unchanged when not followed by a category")
  @Test
  void unchangedWithoutCategory() {
    givenOption("1");
    assertFixed("{{DEFAULTSORT:A}}\n\nText", "{{DEFAULTSORT:A}}\n\nText");
  }

  private void givenOption(final String value) {
    FormattingTestHelper.givenConfiguration(
        wpcleanerRepository, Map.of("auto_cr_defaultsort_category", value));
  }

  private void assertFixed(final String text, final String expected) {
    Assertions.assertThat(FormattingTestHelper.fix(fix, text)).isEqualTo(expected);
  }
}
