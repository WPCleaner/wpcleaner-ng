package org.wpcleaner.lib.check.algorithm;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import org.wpcleaner.api.repository.configuration.ConfigurationParser;

record GeneralAbbreviation(String text, String replacement) {

  private static final int ELEMENT_COUNT = 3;

  static List<GeneralAbbreviation> of(final List<String> lines) {
    return lines.stream()
        .map(line -> ConfigurationParser.splitLine(line, ELEMENT_COUNT))
        .filter(elements -> elements.size() == ELEMENT_COUNT)
        .map(
            elements ->
                new GeneralAbbreviation(elements.getFirst().trim(), elements.getLast().trim()))
        .filter(abbreviation -> !abbreviation.text().isEmpty())
        .toList();
  }
}
