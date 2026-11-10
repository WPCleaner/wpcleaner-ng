package org.wpcleaner.lib.check.configuration;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public record TemplateParameters(Set<String> templates, Map<String, Set<String>> parameters) {

  public static final TemplateParameters EMPTY = new TemplateParameters(Set.of(), Map.of());

  public TemplateParameters {
    templates = Set.copyOf(templates);
    parameters =
        parameters.entrySet().stream()
            .collect(
                Collectors.toUnmodifiableMap(
                    Map.Entry::getKey, entry -> Set.copyOf(entry.getValue())));
  }

  public static TemplateParameters of(
      final List<String> templates, final List<Map.Entry<String, List<String>>> parameters) {
    final Map<String, Set<String>> parametersByTemplate = new HashMap<>();
    parameters.forEach(
        entry -> {
          final String template = entry.getKey().trim();
          entry.getValue().stream()
              .map(String::trim)
              .filter(parameter -> !parameter.isEmpty())
              .forEach(
                  parameter ->
                      parametersByTemplate
                          .computeIfAbsent(template, _ -> new HashSet<>())
                          .add(parameter));
        });
    return new TemplateParameters(
        templates.stream()
            .map(String::trim)
            .filter(template -> !template.isEmpty())
            .collect(Collectors.toSet()),
        parametersByTemplate);
  }
}
