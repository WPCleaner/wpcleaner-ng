package org.wpcleaner.api.wiki.definition;

/*
 * SPDX-FileCopyrightText: © 2024 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class WikiDefinitionHelper {

  private WikiDefinitionHelper() {
    // Do nothing: utility class
  }

  public static Optional<WikiDefinition> findByCode(
      final List<WikiDefinition> definitions, final String code) {
    return definitions.stream()
        .filter(definition -> Objects.equals(code, definition.code()))
        .findFirst();
  }
}
