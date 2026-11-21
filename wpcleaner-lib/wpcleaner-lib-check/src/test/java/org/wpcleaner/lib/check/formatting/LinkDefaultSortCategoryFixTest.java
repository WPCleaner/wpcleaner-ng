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
class LinkDefaultSortCategoryFixTest {

  @Autowired private LinkDefaultSortCategoryFix fix;
  @Autowired private WPCleanerConfigurationRepository wpcleanerRepository;

  @BeforeEach
  void setUp() {
    FormattingTestHelper.givenConfiguration(wpcleanerRepository, Map.of());
  }

  @DisplayName("should leave the text unchanged without configuration")
  @Test
  void unchangedWithoutOption() {
    assertFixed(
        "Text\n{{DEFAULTSORT:A}}\n\nOther\n[[Catégorie:B]]",
        "Text\n{{DEFAULTSORT:A}}\n\nOther\n[[Catégorie:B]]");
  }

  @DisplayName("should move DEFAULTSORT just before the first category")
  @Test
  void moveDefaultSortBeforeCategory() {
    givenOption("true");
    assertFixed(
        "Text\n{{DEFAULTSORT:A}}\n\nOther\n[[Catégorie:B]]",
        "Text\n\nOther\n{{DEFAULTSORT:A}}\n[[Catégorie:B]]");
  }

  @DisplayName("should move DEFAULTSORT placed after the categories")
  @Test
  void moveDefaultSortAfterCategories() {
    givenOption("true");
    assertFixed(
        "Text\n[[Catégorie:B]]\n[[Catégorie:C]]\n{{DEFAULTSORT:A}}\n",
        "Text\n{{DEFAULTSORT:A}}\n[[Catégorie:B]]\n[[Catégorie:C]]\n");
  }

  @DisplayName("should leave DEFAULTSORT unchanged when already linked or separated by text")
  @Test
  void unchangedDefaultSort() {
    givenOption("true");
    assertFixed(
        "Text\n{{DEFAULTSORT:A}}\n\n[[Catégorie:B]]", "Text\n{{DEFAULTSORT:A}}\n\n[[Catégorie:B]]");
    assertFixed(
        "Text\n[[Catégorie:B]]\nOther\n{{DEFAULTSORT:A}}",
        "Text\n[[Catégorie:B]]\nOther\n{{DEFAULTSORT:A}}");
  }

  private void givenOption(final String value) {
    FormattingTestHelper.givenConfiguration(
        wpcleanerRepository, Map.of("auto_link_defaultsort_category", value));
  }

  private void assertFixed(final String text, final String expected) {
    Assertions.assertThat(FormattingTestHelper.fix(fix, text)).isEqualTo(expected);
  }
}
