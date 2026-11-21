package org.wpcleaner.lib.check.formatting;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Map;
import org.wpcleaner.api.analysis.PageAnalysisTestFactory;
import org.wpcleaner.api.repository.configuration.WPCleanerConfiguration;
import org.wpcleaner.api.repository.configuration.WPCleanerConfigurationRepository;

final class FormattingTestHelper {

  private FormattingTestHelper() {}

  static void givenConfiguration(
      final WPCleanerConfigurationRepository wpcleanerRepository,
      final Map<String, String> properties) {
    wpcleanerRepository.setConfiguration(
        PageAnalysisTestFactory.WIKI, new WPCleanerConfiguration(properties, Map.of()));
  }

  static String fix(final FormattingFix fix, final String text) {
    return fix.fix(PageAnalysisTestFactory.createPageAnalysis(text));
  }
}
