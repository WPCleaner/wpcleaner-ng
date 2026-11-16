package org.wpcleaner.lib.check.algorithm.a0xx.a00x.a006;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;
import org.wpcleaner.api.analysis.Element;
import org.wpcleaner.api.analysis.PageAnalysis;
import org.wpcleaner.api.analysis.function.FunctionElement;
import org.wpcleaner.api.analysis.template.TemplateParameter;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.lib.check.CheckAlgorithm;
import org.wpcleaner.lib.check.CheckError;
import org.wpcleaner.lib.check.Replacement;

@Component
public class CheckAlgorithm006 implements CheckAlgorithm {

  private static final int ALGORITHM_NUMBER = 6;
  private static final String DEFAULT_SORT = "defaultsort";
  private static final String FUNCTION_END = "}}";

  @Override
  public int number() {
    return ALGORITHM_NUMBER;
  }

  @Override
  public List<CheckError> analyze(final PageAnalysis analysis) {
    return analysis.getFunctions().stream()
        .filter(function -> DEFAULT_SORT.equals(function.magicWord()))
        .flatMap(function -> analyze(analysis, function).stream())
        .toList();
  }

  private static Optional<CheckError> analyze(
      final PageAnalysis analysis, final FunctionElement function) {
    return function.parameters().stream()
        .findFirst()
        .flatMap(sortKey -> analyze(analysis, function, sortKey));
  }

  private static Optional<CheckError> analyze(
      final PageAnalysis analysis,
      final FunctionElement function,
      final TemplateParameter sortKey) {
    final String text = analysis.getText();
    final SortKeyScan scan = scan(analysis, sortKey);
    if (scan.specialCharacters()) {
      return Optional.of(
          new CheckError(
              ALGORITHM_NUMBER,
              function.begin(),
              function.end(),
              Stream.ofNullable(scan.replacement())
                  .map(
                      replacement ->
                          new Replacement(
                              text.substring(function.begin(), sortKey.begin())
                                  + replacement
                                  + FUNCTION_END,
                              true))
                  .toList()));
    }
    return function.parameters().stream()
        .skip(1)
        .findFirst()
        .map(
            parameter ->
                new CheckError(
                    ALGORITHM_NUMBER, parameter.begin() - 1, parameter.begin(), List.of()));
  }

  private static SortKeyScan scan(final PageAnalysis analysis, final TemplateParameter sortKey) {
    final String text = analysis.getText();
    final WikiDefinition wiki = analysis.getWiki();
    final String value = sortKey.value().trim();
    final int begin = sortKey.begin() + sortKey.value().indexOf(value);
    final int end = begin + value.length();
    final List<Element> skippedElements = findSkippedElements(analysis);
    final StringBuilder fixedValue = new StringBuilder();
    boolean specialCharacters = false;
    boolean replaced = false;
    int index = begin;
    while (index < end) {
      final int skippedEnd = findSkippedEnd(skippedElements, index);
      if (skippedEnd > index) {
        fixedValue.append(text, index, skippedEnd);
        index = skippedEnd;
      } else {
        final char character = text.charAt(index);
        if (SpecialCharacters.isAuthorized(character, wiki)) {
          fixedValue.append(character);
        } else {
          final Optional<String> replacement = SpecialCharacters.findReplacement(character, wiki);
          specialCharacters = true;
          replaced |= replacement.isPresent();
          fixedValue.append(replacement.orElse(String.valueOf(character)));
        }
        index++;
      }
    }
    return new SortKeyScan(specialCharacters, replaced ? fixedValue.toString().trim() : null);
  }

  private static List<Element> findSkippedElements(final PageAnalysis analysis) {
    return Stream.of(
            analysis.getTemplates(),
            analysis.getFunctions(),
            analysis.getParameters(),
            analysis.getComments())
        .flatMap(Collection::stream)
        .<Element>map(element -> element)
        .toList();
  }

  private static int findSkippedEnd(final List<Element> skippedElements, final int index) {
    return skippedElements.stream()
        .filter(element -> element.begin() == index)
        .mapToInt(Element::end)
        .max()
        .orElse(index);
  }

  private record SortKeyScan(boolean specialCharacters, @Nullable String replacement) {}
}
