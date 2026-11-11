package org.wpcleaner.lib.check.configuration;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public record TemplateDefinition(String template, Map<String, String> parameters) {

  public TemplateDefinition {
    parameters = Map.copyOf(parameters);
  }

  static Optional<TemplateDefinition> of(
      final String template, final List<String> fields, final List<String> parameterNames) {
    final Map<String, String> parameters = new LinkedHashMap<>();
    for (int index = 0; index < Math.min(fields.size(), parameterNames.size()); index++) {
      final String parameterName = parameterNames.get(index).trim();
      if (!parameterName.isEmpty()) {
        parameters.put(fields.get(index), parameterName);
      }
    }
    return Optional.of(template.trim())
        .filter(name -> !name.isEmpty())
        .map(name -> new TemplateDefinition(name, parameters));
  }

  public Optional<String> parameter(final String field) {
    return Optional.ofNullable(parameters.get(field));
  }
}
