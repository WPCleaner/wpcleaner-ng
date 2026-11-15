package org.wpcleaner.lib.check.algorithm.a0xx.a00x.a003;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;
import org.wpcleaner.api.analysis.PageAnalysis;
import org.wpcleaner.api.analysis.pagetitle.PageTitle;
import org.wpcleaner.api.analysis.template.TemplateElement;
import org.wpcleaner.api.repository.namespace.CommonNamespaces;

record ReferencesTemplates(Set<PageTitle> titles, List<Pattern> patterns) {

  private static final String TEMPLATE_START = "\\{\\{[ ]?";

  ReferencesTemplates {
    titles = Set.copyOf(titles);
    patterns = List.copyOf(patterns);
  }

  static ReferencesTemplates of(final PageAnalysis analysis, final List<String> names) {
    final List<String> trimmedNames =
        names.stream().map(String::trim).filter(name -> !name.isEmpty()).toList();
    return new ReferencesTemplates(
        trimmedNames.stream()
            .flatMap(name -> analysis.normalizeTitle(name, CommonNamespaces.TEMPLATE.id).stream())
            .collect(Collectors.toSet()),
        trimmedNames.stream().flatMap(name -> toPattern(name).stream()).toList());
  }

  boolean isUsedIn(final PageAnalysis analysis) {
    final String text = analysis.getText();
    return analysis.getTemplates().stream().anyMatch(template -> matches(text, template));
  }

  private boolean matches(final String text, final TemplateElement template) {
    return titles.contains(template.title())
        || patterns.stream()
            .anyMatch(
                pattern ->
                    pattern.matcher(text).region(template.begin(), text.length()).lookingAt());
  }

  private static Optional<Pattern> toPattern(final String name) {
    final String firstCharacter = name.substring(0, 1);
    final String first =
        Character.isLetter(name.charAt(0))
            ? "["
                + firstCharacter.toLowerCase(Locale.ROOT)
                + firstCharacter.toUpperCase(Locale.ROOT)
                + "]"
            : firstCharacter;
    try {
      return Optional.of(Pattern.compile(TEMPLATE_START + first + name.substring(1)));
    } catch (final PatternSyntaxException _) {
      return Optional.empty();
    }
  }
}
