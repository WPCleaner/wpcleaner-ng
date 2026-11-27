package org.wpcleaner.lib.check.algorithm.a0xx.a03x.a034;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;
import org.wpcleaner.api.analysis.Element;
import org.wpcleaner.api.analysis.PageAnalysis;
import org.wpcleaner.api.analysis.function.FunctionElement;
import org.wpcleaner.api.analysis.include.IncludeAreaType;
import org.wpcleaner.api.analysis.tag.WikiTagType;
import org.wpcleaner.api.analysis.template.TemplateElement;
import org.wpcleaner.api.analysis.template.TemplateParameter;
import org.wpcleaner.api.repository.namespace.CommonNamespaces;
import org.wpcleaner.api.utils.GT;
import org.wpcleaner.lib.check.CheckAlgorithm;
import org.wpcleaner.lib.check.CheckError;
import org.wpcleaner.lib.check.ExpandedReplacement;
import org.wpcleaner.lib.check.Replacement;
import org.wpcleaner.lib.check.Suggestion;

@Component
public class CheckAlgorithm034 implements CheckAlgorithm {

  private static final int ALGORITHM_NUMBER = 34;
  private static final Set<String> REPORTED_BEHAVIOR_SWITCHES =
      Set.of("forcetoc", "index", "noindex", "nonewsectionlink");
  private static final Set<String> IGNORED_FUNCTIONS =
      Set.of("defaultsort", "displaytitle", "formatnum");
  private static final Set<String> SUBST_MODIFIERS = Set.of("subst", "safesubst");
  private static final Set<String> SUBST_PROTECTED_TAGS =
      Stream.of(WikiTagType.GALLERY, WikiTagType.REF, WikiTagType.TIMELINE)
          .map(WikiTagType::getNormalizedName)
          .collect(Collectors.toUnmodifiableSet());
  private static final String FUNCTION_IF_EXPR = "ifexpr";
  private static final String FUNCTION_INVOKE = "invoke";
  private static final String FUNCTION_PAGE_NAME = "pagename";
  private static final String FUNCTION_TAG = "tag";
  private static final String TEMPLATE_BEGIN = "{{";
  private static final String EXTRA_BRACE_BEGIN = "{{{";
  private static final String SUBST_BEGIN = "{{subst:";

  @Override
  public int number() {
    return ALGORITHM_NUMBER;
  }

  @Override
  public List<CheckError> analyze(final PageAnalysis analysis) {
    if (analysis.getPageTitle().namespace() == CommonNamespaces.TEMPLATE.id) {
      return List.of();
    }
    final List<Element> substProtectedAreas = findSubstProtectedAreas(analysis);
    final List<CheckError> candidates =
        Stream.of(
                findExtraBraces(analysis),
                findParameters(analysis),
                findFunctions(analysis, substProtectedAreas),
                findSubstTemplates(analysis),
                findBehaviorSwitches(analysis))
            .flatMap(List::stream)
            .sorted(Comparator.comparingInt(CheckError::begin))
            .toList();
    final List<CheckError> errors = new ArrayList<>();
    int analyzedEnd = 0;
    for (final CheckError candidate : candidates) {
      if (candidate.begin() >= analyzedEnd) {
        errors.add(candidate);
        analyzedEnd = candidate.end();
      }
    }
    return List.copyOf(errors);
  }

  private static List<CheckError> findExtraBraces(final PageAnalysis analysis) {
    final String text = analysis.getText();
    final List<TemplateElement> templates = analysis.getTemplates();
    return templates.stream()
        .map(TemplateElement::begin)
        .filter(begin -> begin > 0 && text.startsWith(EXTRA_BRACE_BEGIN, begin - 1))
        .filter(begin -> templates.stream().noneMatch(template -> template.begin() == begin - 2))
        .map(
            begin ->
                new CheckError(
                    ALGORITHM_NUMBER,
                    begin - 1,
                    begin + 2,
                    List.of(new Replacement(TEMPLATE_BEGIN, false))))
        .toList();
  }

  private static List<CheckError> findParameters(final PageAnalysis analysis) {
    return analysis.getParameters().stream()
        .map(
            parameter ->
                new CheckError(
                    ALGORITHM_NUMBER,
                    parameter.begin(),
                    parameter.end(),
                    parameter.defaultValue() == null
                        ? List.of()
                        : List.of(new Replacement(parameter.defaultValue(), false))))
        .toList();
  }

  private static List<CheckError> findFunctions(
      final PageAnalysis analysis, final List<Element> substProtectedAreas) {
    return analysis.getFunctions().stream()
        .filter(function -> isSubst(function.modifiers()) || !isIgnoredFunction(function))
        .map(
            function ->
                new CheckError(
                    ALGORITHM_NUMBER,
                    function.begin(),
                    function.end(),
                    createFunctionSuggestions(analysis, function, substProtectedAreas)))
        .toList();
  }

  private static boolean isIgnoredFunction(final FunctionElement function) {
    return IGNORED_FUNCTIONS.contains(function.magicWord())
        || (FUNCTION_TAG.equals(function.magicWord())
            && function.parameters().stream()
                .findFirst()
                .map(TemplateParameter::value)
                .filter(
                    tagName ->
                        WikiTagType.REF
                            .getNormalizedName()
                            .equals(tagName.trim().toLowerCase(Locale.ROOT)))
                .isPresent());
  }

  private static List<Suggestion> createFunctionSuggestions(
      final PageAnalysis analysis,
      final FunctionElement function,
      final List<Element> substProtectedAreas) {
    if (isSubst(function.modifiers())) {
      return List.of();
    }
    final List<Suggestion> suggestions = new ArrayList<>();
    if (FUNCTION_PAGE_NAME.equals(function.magicWord())) {
      suggestions.add(new Replacement(analysis.getPageTitle().text(), false));
    }
    if (FUNCTION_IF_EXPR.equals(function.magicWord())) {
      function.parameters().stream()
          .skip(1)
          .map(parameter -> new Replacement(parameter.value(), false))
          .forEach(suggestions::add);
    }
    if (!function.isInsideOne(substProtectedAreas)) {
      if (!FUNCTION_INVOKE.equals(function.magicWord())) {
        suggestions.add(
            new Replacement(
                SUBST_BEGIN + analysis.getText().substring(function.begin() + 2, function.end()),
                false));
      }
      suggestions.add(
          new ExpandedReplacement(
              analysis.getText().substring(function.begin(), function.end()), GT._T("Expand")));
    }
    return List.copyOf(suggestions);
  }

  private static List<CheckError> findSubstTemplates(final PageAnalysis analysis) {
    return analysis.getTemplates().stream()
        .filter(template -> isSubst(template.modifiers()))
        .map(
            template ->
                new CheckError(ALGORITHM_NUMBER, template.begin(), template.end(), List.of()))
        .toList();
  }

  private static List<CheckError> findBehaviorSwitches(final PageAnalysis analysis) {
    return analysis.getBehaviorSwitches().stream()
        .filter(behaviorSwitch -> REPORTED_BEHAVIOR_SWITCHES.contains(behaviorSwitch.magicWord()))
        .map(
            behaviorSwitch ->
                new CheckError(
                    ALGORITHM_NUMBER,
                    behaviorSwitch.begin(),
                    behaviorSwitch.end(),
                    List.of(new Replacement("", false))))
        .toList();
  }

  private static List<Element> findSubstProtectedAreas(final PageAnalysis analysis) {
    return Stream.<Element>concat(
            analysis.getCompleteTags().stream()
                .filter(tag -> SUBST_PROTECTED_TAGS.contains(tag.startTag().normalizedName())),
            analysis.getIncludeAreas().stream()
                .filter(includeArea -> includeArea.type() == IncludeAreaType.INCLUDE_ONLY))
        .toList();
  }

  private static boolean isSubst(final List<String> modifiers) {
    return modifiers.stream().anyMatch(SUBST_MODIFIERS::contains);
  }
}
