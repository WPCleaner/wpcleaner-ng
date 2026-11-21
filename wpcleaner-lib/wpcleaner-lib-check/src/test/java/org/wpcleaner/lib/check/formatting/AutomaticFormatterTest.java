package org.wpcleaner.lib.check.formatting;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.HashMap;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.wpcleaner.api.analysis.PageAnalysisTestFactory;
import org.wpcleaner.api.repository.configuration.WPCleanerConfigurationRepository;
import org.wpcleaner.lib.check.CheckSpringBootTest;

@CheckSpringBootTest
class AutomaticFormatterTest {

  private static final String TEXT =
      "==Title==\nText\n[[en:A]]\n[[Catégorie:B]]\n\n[[Catégorie:C]]\n{{DEFAULTSORT:D}}\n\n\n";
  private static final Map<String, String> ALL_OPTIONS =
      Map.of(
          "auto_space_around_title", "1",
          "auto_link_defaultsort_category", "true",
          "auto_langlink_after_category", "true",
          "auto_cr_before_category", "2",
          "auto_cr_defaultsort_category", "1",
          "auto_cr_between_category", "1");

  @Autowired private AutomaticFormatter formatter;
  @Autowired private WPCleanerConfigurationRepository wpcleanerRepository;

  @BeforeEach
  void setUp() {
    FormattingTestHelper.givenConfiguration(wpcleanerRepository, Map.of());
  }

  @DisplayName("should leave the text unchanged when automatic formatting is not active")
  @Test
  void unchangedWhenNotActive() {
    FormattingTestHelper.givenConfiguration(wpcleanerRepository, ALL_OPTIONS);
    assertFormatted("Paris", TEXT, TEXT);
  }

  @DisplayName("should leave the text unchanged outside the main namespace")
  @Test
  void unchangedOutsideMainNamespace() {
    givenActive();
    assertFormatted("Utilisateur:Paris", TEXT, TEXT);
  }

  @DisplayName("should apply all the fixes in order")
  @Test
  void applyAllFixes() {
    givenActive();
    assertFormatted(
        "Paris",
        TEXT,
        "== Title ==\nText\n\n{{DEFAULTSORT:D}}\n[[Catégorie:B]]\n[[Catégorie:C]]\n[[en:A]]\n");
  }

  private void givenActive() {
    final Map<String, String> properties = new HashMap<>(ALL_OPTIONS);
    properties.put("auto_active", "true");
    FormattingTestHelper.givenConfiguration(wpcleanerRepository, properties);
  }

  private void assertFormatted(final String title, final String text, final String expected) {
    Assertions.assertThat(
            formatter.tidyArticle(PageAnalysisTestFactory.createPageAnalysis(title, text)))
        .isEqualTo(expected);
  }
}
