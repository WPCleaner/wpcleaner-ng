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
class LanguageLinksAfterCategoryFixTest {

  @Autowired private LanguageLinksAfterCategoryFix fix;
  @Autowired private WPCleanerConfigurationRepository wpcleanerRepository;

  @BeforeEach
  void setUp() {
    FormattingTestHelper.givenConfiguration(wpcleanerRepository, Map.of());
  }

  @DisplayName("should leave the text unchanged without configuration")
  @Test
  void unchangedWithoutOption() {
    assertFixed("Text\n[[en:A]]\n[[Catégorie:C]]\n", "Text\n[[en:A]]\n[[Catégorie:C]]\n");
  }

  @DisplayName("should move language links after categories")
  @Test
  void moveLanguageLinks() {
    givenOption("true");
    assertFixed(
        "Text\n[[en:A]]\n[[de:B]]\n{{DEFAULTSORT:D}}\n[[Catégorie:C]]\n",
        "Text\n{{DEFAULTSORT:D}}\n[[Catégorie:C]]\n[[en:A]]\n[[de:B]]\n");
  }

  @DisplayName("should leave language links already after categories unchanged")
  @Test
  void unchangedLanguageLinks() {
    givenOption("true");
    assertFixed("Text\n[[Catégorie:C]]\n[[en:A]]\n", "Text\n[[Catégorie:C]]\n[[en:A]]\n");
    assertFixed(
        "Text\n[[en:A]]\nOther\n[[Catégorie:C]]\n", "Text\n[[en:A]]\nOther\n[[Catégorie:C]]\n");
  }

  private void givenOption(final String value) {
    FormattingTestHelper.givenConfiguration(
        wpcleanerRepository, Map.of("auto_langlink_after_category", value));
  }

  private void assertFixed(final String text, final String expected) {
    Assertions.assertThat(FormattingTestHelper.fix(fix, text)).isEqualTo(expected);
  }
}
