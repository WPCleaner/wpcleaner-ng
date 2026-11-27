package org.wpcleaner.lib.check.algorithm.a0xx.a00x.a001;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.wpcleaner.api.analysis.PageAnalysis;
import org.wpcleaner.api.analysis.template.TemplateElement;
import org.wpcleaner.api.repository.namespace.CommonNamespaces;
import org.wpcleaner.api.utils.GT;
import org.wpcleaner.lib.check.CheckAlgorithm;
import org.wpcleaner.lib.check.CheckError;
import org.wpcleaner.lib.check.Replacement;

@Component
public class CheckAlgorithm001 implements CheckAlgorithm {

  private static final int ALGORITHM_NUMBER = 1;
  private static final char NAMESPACE_SEPARATOR = ':';

  @Override
  public int number() {
    return ALGORITHM_NUMBER;
  }

  @Override
  public List<CheckError> analyze(final PageAnalysis analysis) {
    return analysis.getTemplates().stream()
        .filter(template -> hasTemplateNamespace(analysis, template))
        .flatMap(template -> createError(analysis, template).stream())
        .toList();
  }

  private static boolean hasTemplateNamespace(
      final PageAnalysis analysis, final TemplateElement template) {
    return template.name().indexOf(NAMESPACE_SEPARATOR) > 0
        && analysis
            .normalizeTitle(template.name(), CommonNamespaces.MAIN.id)
            .filter(title -> title.namespace() == CommonNamespaces.TEMPLATE.id)
            .isPresent();
  }

  private static Optional<CheckError> createError(
      final PageAnalysis analysis, final TemplateElement template) {
    final String text = analysis.getText();
    final String name = template.name();
    final String namespace = name.substring(0, name.indexOf(NAMESPACE_SEPARATOR) + 1);
    final int namespaceBegin = text.indexOf(namespace, template.begin());
    if (namespaceBegin < 0 || namespaceBegin + namespace.length() > template.end()) {
      return Optional.empty();
    }
    final String replacement =
        text.substring(template.begin(), namespaceBegin)
            + text.substring(namespaceBegin + namespace.length(), template.end()).stripLeading();
    if (!isTemplate(analysis, replacement)) {
      return Optional.empty();
    }
    return Optional.of(
        new CheckError(
            ALGORITHM_NUMBER,
            template.begin(),
            template.end(),
            List.of(
                new Replacement(
                    replacement,
                    GT._T(
                        "Remove %s namespace from template name",
                        namespace.substring(0, namespace.length() - 1).strip()),
                    analysis.getPageTitle().namespace() == CommonNamespaces.MAIN.id))));
  }

  private static boolean isTemplate(final PageAnalysis analysis, final String replacement) {
    return analysis.withText(replacement).getTemplates().stream()
        .anyMatch(template -> template.begin() == 0);
  }
}
