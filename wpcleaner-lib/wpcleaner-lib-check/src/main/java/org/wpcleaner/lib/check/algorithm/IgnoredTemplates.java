package org.wpcleaner.lib.check.algorithm;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.wpcleaner.api.analysis.PageAnalysis;
import org.wpcleaner.api.analysis.category.CategoryElement;
import org.wpcleaner.api.analysis.pagetitle.PageTitle;
import org.wpcleaner.api.analysis.template.TemplateElement;
import org.wpcleaner.api.analysis.template.TemplateParameter;
import org.wpcleaner.api.repository.namespace.CommonNamespaces;
import org.wpcleaner.lib.check.configuration.TemplateParameters;

public final class IgnoredTemplates {

  private IgnoredTemplates() {}

  public static Predicate<CategoryElement> createIgnoredFilter(
      final PageAnalysis analysis, final TemplateParameters ignored) {
    final Set<PageTitle> templates =
        ignored.templates().stream()
            .flatMap(name -> normalizeTemplate(analysis, name).stream())
            .collect(Collectors.toSet());
    final Map<PageTitle, Set<String>> parameters = new LinkedHashMap<>();
    ignored
        .parameters()
        .forEach(
            (name, names) ->
                normalizeTemplate(analysis, name).ifPresent(title -> parameters.put(title, names)));
    return category ->
        analysis.getTemplates().stream()
            .filter(category::isInside)
            .max(Comparator.comparingInt(TemplateElement::begin))
            .filter(
                template ->
                    templates.contains(template.title())
                        || parameters.getOrDefault(template.title(), Set.of()).stream()
                            .anyMatch(name -> isInParameter(template, category, name)))
            .isPresent();
  }

  private static Optional<PageTitle> normalizeTemplate(
      final PageAnalysis analysis, final String name) {
    return analysis.normalizeTitle(name, CommonNamespaces.TEMPLATE.id);
  }

  private static boolean isInParameter(
      final TemplateElement template, final CategoryElement category, final String name) {
    int position = 0;
    for (final TemplateParameter parameter : template.parameters()) {
      final String parameterName = parameter.name();
      if (parameterName == null) {
        position++;
      }
      if (parameter.begin() <= category.begin() && category.end() <= parameter.end()) {
        return name.equals(
            parameterName == null ? Integer.toString(position) : parameterName.trim());
      }
    }
    return false;
  }
}
