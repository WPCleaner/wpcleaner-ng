package org.wpcleaner.lib.check.algorithm;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import org.wpcleaner.api.analysis.Element;
import org.wpcleaner.api.analysis.PageAnalysis;
import org.wpcleaner.api.analysis.pagetitle.PageTitle;
import org.wpcleaner.api.analysis.tag.HtmlTagType;
import org.wpcleaner.api.repository.namespace.CommonNamespaces;
import org.wpcleaner.lib.check.CheckAlgorithm;
import org.wpcleaner.lib.check.CheckError;
import org.wpcleaner.lib.check.Replacement;
import org.wpcleaner.lib.check.configuration.CheckAlgorithmConfiguration;
import org.wpcleaner.lib.check.configuration.CheckWikiConfigurationRepository;

@Component
public class CheckAlgorithm061 implements CheckAlgorithm {

  private static final int ALGORITHM_NUMBER = 61;
  private static final String PARAMETER_FORCE_SEPARATOR = "force_separator";
  private static final String PARAMETER_SEPARATOR = "separator";
  private static final String PARAMETER_TEMPLATES = "templates";
  private static final String PUNCTUATION = ",.;:";
  private static final String PUNCTUATION_BEFORE = ",.;:!?()\"-–—";
  private static final String GROUPING_PUNCTUATION = ",;.'′’-&";
  private static final String LINE_STARTS = "*#;:!";
  private static final String DEFINITION_TERM = ";";
  private static final Set<String> SAFE_AFTER = Set.of(".", ",", ":", ";");
  private static final Set<String> KEPT_BEFORE = Set.of(")", "\"", "\")", ")\"", "?\"");
  private static final char NEW_LINE = '\n';
  private static final char SPACE = ' ';
  private static final char CLOSING_BRACE = '}';

  private final CheckWikiConfigurationRepository checkWikiRepository;

  public CheckAlgorithm061(final CheckWikiConfigurationRepository checkWikiRepository) {
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
    final String separator = configuration.getString(PARAMETER_SEPARATOR).orElse("");
    final Set<PageTitle> templates =
        configuration.getStrings(PARAMETER_TEMPLATES).stream()
            .flatMap(
                name -> analysis.normalizeTitle(name.trim(), CommonNamespaces.TEMPLATE.id).stream())
            .collect(Collectors.toSet());
    final Context context =
        new Context(
            analysis,
            separator,
            configuration.getBoolean(PARAMETER_FORCE_SEPARATOR),
            analysis.getPageTitle().namespace() == CommonNamespaces.MAIN.id);
    final ReferenceGrouper grouper =
        new ReferenceGrouper(
            GROUPING_PUNCTUATION, separator, false, Set.of(HtmlTagType.SMALL, HtmlTagType.SUB));
    final List<CheckError> errors = new ArrayList<>();
    grouper
        .group(analysis, ReferenceGrouper.findReferences(analysis, templates))
        .forEach(
            group ->
                analyzePunctuationAfter(context, group)
                    .ifPresentOrElse(
                        errors::add,
                        () -> errors.addAll(analyzePunctuationBetween(context, group))));
    return List.copyOf(errors);
  }

  private static Optional<CheckError> analyzePunctuationAfter(
      final Context context, final List<Element> group) {
    final String text = context.analysis().getText();
    final int first = skipWhitespaceAfter(text, group.getLast().end());
    if (first >= text.length() || PUNCTUATION.indexOf(text.charAt(first)) < 0) {
      return Optional.empty();
    }
    final char punctuation = text.charAt(first);
    final int end = skipRepeated(text, first, punctuation);
    if (!isEndOfPunctuation(text, end)
        || (punctuation == ':'
            && isInDefinitionTerm(context.analysis(), group.getFirst().begin()))) {
      return Optional.empty();
    }
    return Optional.of(createError(context, group, text.substring(first, end), end));
  }

  private static CheckError createError(
      final Context context,
      final List<Element> group,
      final String punctuationAfter,
      final int end) {
    final String text = context.analysis().getText();
    final int beforeEnd = skipSpacesBefore(text, group.getFirst().begin());
    int begin = beforeEnd;
    while (begin > 0 && PUNCTUATION_BEFORE.indexOf(text.charAt(begin - 1)) >= 0) {
      begin--;
    }
    final String punctuationBefore = text.substring(begin, beforeEnd);
    final String allPunctuation = mergePunctuation(punctuationBefore, punctuationAfter);
    final boolean automatic =
        context.automaticAllowed()
            && isAutomatic(
                punctuationBefore, punctuationAfter, allPunctuation, isFollowedBySpace(text, end));
    final String references = joinReferences(text, group, context.separator());
    final String shortReferences =
        ReferenceGrouper.shortenReferences(group.size(), context.separator());
    final Set<String> punctuations = new LinkedHashSet<>();
    punctuations.add(allPunctuation);
    punctuations.add(punctuationAfter);
    if (!punctuationBefore.isEmpty()) {
      punctuations.add(punctuationBefore);
    }
    final List<Replacement> result = new ArrayList<>();
    punctuations.forEach(
        punctuation ->
            result.add(
                new Replacement(
                    punctuation + references,
                    punctuation + shortReferences,
                    automatic && result.isEmpty())));
    return new CheckError(ALGORITHM_NUMBER, begin, end, result);
  }

  private static String mergePunctuation(
      final String punctuationBefore, final String punctuationAfter) {
    return SAFE_AFTER.contains(punctuationAfter)
            && !punctuationBefore.equals(punctuationAfter)
            && !punctuationBefore.contains(punctuationAfter)
            && KEPT_BEFORE.contains(punctuationBefore)
        ? punctuationBefore + punctuationAfter
        : punctuationAfter;
  }

  private static boolean isAutomatic(
      final String punctuationBefore,
      final String punctuationAfter,
      final String allPunctuation,
      final boolean followedBySpace) {
    if (punctuationBefore.isEmpty()) {
      return punctuationAfter.length() == 1;
    }
    return followedBySpace
        && SAFE_AFTER.contains(punctuationAfter)
        && (punctuationAfter.equals(punctuationBefore) || !allPunctuation.equals(punctuationAfter));
  }

  private static int skipRepeated(final String text, final int index, final char character) {
    int current = index + 1;
    while (current < text.length() && text.charAt(current) == character) {
      current++;
    }
    return current;
  }

  private static boolean isEndOfPunctuation(final String text, final int index) {
    return index >= text.length()
        || Character.isWhitespace(text.charAt(index))
        || text.charAt(index) == CLOSING_BRACE;
  }

  private static List<CheckError> analyzePunctuationBetween(
      final Context context, final List<Element> group) {
    final String text = context.analysis().getText();
    final int beforeGroup = skipWhitespaceBefore(text, group.getFirst().begin());
    final boolean automatic =
        context.forceSeparator()
            && context.automaticAllowed()
            && beforeGroup > 0
            && PUNCTUATION.indexOf(text.charAt(beforeGroup - 1)) >= 0;
    final List<CheckError> errors = new ArrayList<>();
    for (int index = 1; index < group.size(); index++) {
      final int begin = group.get(index - 1).end();
      final int end = group.get(index).begin();
      final String between = text.substring(begin, end);
      final String withoutSeparator =
          context.separator().isEmpty() ? between : between.replace(context.separator(), "");
      if (withoutSeparator.chars().anyMatch(character -> PUNCTUATION.indexOf(character) >= 0)) {
        errors.add(
            new CheckError(
                ALGORITHM_NUMBER,
                begin,
                end,
                List.of(new Replacement(context.separator(), automatic))));
      }
    }
    return errors;
  }

  private static boolean isInDefinitionTerm(final PageAnalysis analysis, final int index) {
    return analysis.getListItems().stream()
        .anyMatch(
            item ->
                item.begin() <= index
                    && index < item.end()
                    && item.prefix().startsWith(DEFINITION_TERM));
  }

  private static String joinReferences(
      final String text, final List<Element> group, final String separator) {
    return group.stream()
        .map(reference -> text.substring(reference.begin(), reference.end()))
        .collect(Collectors.joining(separator));
  }

  private static int skipWhitespaceAfter(final String text, final int index) {
    int current = index;
    while (current < text.length() && Character.isWhitespace(text.charAt(current))) {
      if (text.charAt(current) == NEW_LINE
          && current + 1 < text.length()
          && (Character.isWhitespace(text.charAt(current + 1))
              || LINE_STARTS.indexOf(text.charAt(current + 1)) >= 0)) {
        return current;
      }
      current++;
    }
    return current;
  }

  private static int skipWhitespaceBefore(final String text, final int index) {
    int current = index;
    while (current > 0 && Character.isWhitespace(text.charAt(current - 1))) {
      current--;
    }
    return current;
  }

  private static int skipSpacesBefore(final String text, final int index) {
    int current = index;
    while (current > 0 && text.charAt(current - 1) == SPACE) {
      current--;
    }
    return current;
  }

  private static boolean isFollowedBySpace(final String text, final int index) {
    return index >= text.length() || text.charAt(index) == SPACE || text.charAt(index) == NEW_LINE;
  }

  private record Context(
      PageAnalysis analysis, String separator, boolean forceSeparator, boolean automaticAllowed) {}
}
