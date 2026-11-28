package org.wpcleaner.lib.check.algorithm.a0xx.a01x.a016;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.wpcleaner.api.analysis.Element;
import org.wpcleaner.api.analysis.PageAnalysis;
import org.wpcleaner.api.analysis.internallink.InternalLinkElement;
import org.wpcleaner.api.analysis.template.TemplateElement;
import org.wpcleaner.api.utils.GT;
import org.wpcleaner.lib.check.CheckAlgorithm;
import org.wpcleaner.lib.check.CheckError;
import org.wpcleaner.lib.check.Information;
import org.wpcleaner.lib.check.Replacement;
import org.wpcleaner.lib.check.Suggestion;
import org.wpcleaner.lib.check.configuration.CheckAlgorithmConfiguration;
import org.wpcleaner.lib.check.configuration.CheckWikiConfigurationRepository;

@Component
public class CheckAlgorithm016 implements CheckAlgorithm {

  private static final int ALGORITHM_NUMBER = 16;
  private static final String PARAMETER_ONLY_TEMPLATES = "only_templates";
  private static final char PIPE = '|';
  private static final int LINK_END_LENGTH = 2;

  private final CheckWikiConfigurationRepository checkWikiRepository;

  public CheckAlgorithm016(final CheckWikiConfigurationRepository checkWikiRepository) {
    this.checkWikiRepository = checkWikiRepository;
  }

  @Override
  public int number() {
    return ALGORITHM_NUMBER;
  }

  @Override
  public List<CheckError> analyze(final PageAnalysis analysis) {
    final CheckAlgorithmConfiguration configuration =
        checkWikiRepository
            .getConfiguration(analysis.getWiki())
            .getAlgorithm(ALGORITHM_NUMBER)
            .orElseGet(() -> new CheckAlgorithmConfiguration(ALGORITHM_NUMBER, Map.of()));
    final EnabledControlCharacters enabled = new EnabledControlCharacters(configuration);
    final List<Element> areas =
        configuration.getBoolean(PARAMETER_ONLY_TEMPLATES)
            ? findOutermostTemplates(analysis)
            : List.of(new Area(0, analysis.getText().length()));
    return areas.stream().flatMap(area -> analyzeArea(analysis, enabled, area).stream()).toList();
  }

  private static List<Element> findOutermostTemplates(final PageAnalysis analysis) {
    final List<Element> templates = new ArrayList<>();
    int lastEnd = 0;
    for (final TemplateElement template :
        analysis.getTemplates().stream().sorted(Element.COMPARATOR).toList()) {
      if (template.begin() >= lastEnd) {
        templates.add(template);
        lastEnd = template.end();
      }
    }
    return List.copyOf(templates);
  }

  private static List<CheckError> analyzeArea(
      final PageAnalysis analysis, final EnabledControlCharacters enabled, final Element area) {
    final String text = analysis.getText();
    final List<CheckError> errors = new ArrayList<>();
    int index = area.begin();
    while (index < area.end()) {
      final int codePoint = text.codePointAt(index);
      if (enabled.find(codePoint).isPresent()) {
        final int begin = index > area.begin() ? text.offsetByCodePoints(index, -1) : index;
        final int end = findEnd(text, index, area.end(), enabled);
        errors.add(
            createError(
                analysis, index, ControlCharacterRange.of(text, begin, end, enabled), begin, end));
        index = end;
      } else {
        index += Character.charCount(codePoint);
      }
    }
    return List.copyOf(errors);
  }

  private static int findEnd(
      final String text,
      final int control,
      final int areaEnd,
      final EnabledControlCharacters enabled) {
    int end = control + Character.charCount(text.codePointAt(control));
    while (end < areaEnd
        && (enabled.find(text.codePointBefore(end)).isPresent()
            || enabled.find(text.codePointAt(end)).isPresent())) {
      end += Character.charCount(text.codePointAt(end));
    }
    return end;
  }

  private static CheckError createError(
      final PageAnalysis analysis,
      final int firstControl,
      final ControlCharacterRange range,
      final int begin,
      final int end) {
    final boolean automatic =
        range.automatic()
            && analysis.getTitle().codePoints().noneMatch(range.controls()::contains)
            && isFixableInRedirect(analysis, firstControl, range)
            && !isInLinkTarget(analysis, firstControl);
    final String original = range.original();
    final String replacement = range.replacement();
    final List<Suggestion> suggestions = new ArrayList<>();
    range.controls().stream().map(CheckAlgorithm016::createInformation).forEach(suggestions::add);
    if (!replacement.equals(original)) {
      suggestions.add(
          new Replacement(replacement, GT._T("Remove all control characters"), automatic));
    }
    if (!automatic || replacement.equals(original)) {
      range.alternatives().stream()
          .filter(alternative -> !alternative.equals(original) && !alternative.equals(replacement))
          .forEach(alternative -> suggestions.add(new Replacement(alternative, false)));
    }
    return new CheckError(ALGORITHM_NUMBER, begin, end, suggestions);
  }

  private static Information createInformation(final int codePoint) {
    return new Information(
        String.format(
            Locale.ROOT,
            "U+%04X – %s",
            codePoint,
            ControlCharacter.of(codePoint).map(ControlCharacter::description).orElse("")));
  }

  private static boolean isFixableInRedirect(
      final PageAnalysis analysis, final int firstControl, final ControlCharacterRange range) {
    return analysis
        .getRedirect()
        .map(
            redirect ->
                analysis.getCategories().stream()
                        .anyMatch(category -> contains(category, firstControl))
                    || (range.safeInRedirect() && contains(redirect.link(), firstControl)))
        .orElse(true);
  }

  private static boolean isInLinkTarget(final PageAnalysis analysis, final int firstControl) {
    final String text = analysis.getText();
    return analysis.getInternalLinks().stream()
        .filter(link -> contains(link, firstControl))
        .findFirst()
        .filter(link -> link.anchor() != null)
        .filter(link -> firstControl < findTargetEnd(text, link) - 1)
        .isPresent();
  }

  private static int findTargetEnd(final String text, final InternalLinkElement link) {
    return link.text() == null ? link.end() - LINK_END_LENGTH : text.indexOf(PIPE, link.begin());
  }

  private static boolean contains(final Element element, final int index) {
    return element.begin() <= index && index < element.end();
  }

  private record Area(int begin, int end) implements Element {}
}
