package org.wpcleaner.lib.check.algorithm;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;
import org.wpcleaner.api.analysis.PageAnalysis;
import org.wpcleaner.api.analysis.category.CategoryElement;
import org.wpcleaner.api.analysis.internallink.InternalLinkBuilder;
import org.wpcleaner.api.analysis.internallink.InternalLinkElement;
import org.wpcleaner.api.repository.configuration.WPCleanerConfigurationRepository;
import org.wpcleaner.api.repository.configuration.WPCleanerParameter;
import org.wpcleaner.api.repository.namespace.CommonNamespaces;
import org.wpcleaner.lib.check.CheckAlgorithm;
import org.wpcleaner.lib.check.CheckError;
import org.wpcleaner.lib.check.Replacement;

@Component
public class CheckAlgorithm064 implements CheckAlgorithm {

  private static final int ALGORITHM_NUMBER = 64;
  private static final char APOSTROPHE = '\'';
  private static final char UNDERSCORE = '_';
  private static final char SPACE = ' ';
  private static final char PIPE = '|';

  private final WPCleanerConfigurationRepository wpcleanerRepository;

  public CheckAlgorithm064(final WPCleanerConfigurationRepository wpcleanerRepository) {
    this.wpcleanerRepository = wpcleanerRepository;
  }

  @Override
  public int number() {
    return ALGORITHM_NUMBER;
  }

  @Override
  public List<CheckError> analyze(final PageAnalysis analysis) {
    final String apostropheTemplate =
        wpcleanerRepository
            .getConfiguration(analysis.getWiki())
            .getString(WPCleanerParameter.APOSTROPHE_TEMPLATE)
            .orElse(null);
    return Stream.concat(
            analysis.getInternalLinks().stream()
                .flatMap(
                    link ->
                        LinkTextMatch.find(analysis, link).stream()
                            .map(
                                match ->
                                    createError(
                                        analysis.getText(), link, match, apostropheTemplate))),
            analysis.getCategories().stream()
                .filter(category -> isSortKeySameAsCategory(analysis, category))
                .map(category -> createError(analysis.getText(), category)))
        .sorted(Comparator.comparingInt(CheckError::begin))
        .toList();
  }

  private static boolean isSortKeySameAsCategory(
      final PageAnalysis analysis, final CategoryElement category) {
    final String sortKey = category.sortKey();
    return sortKey != null
        && analysis
            .normalizeTitle(sortKey, CommonNamespaces.MAIN.id)
            .filter(title -> title.equals(category.title()))
            .isPresent();
  }

  private static CheckError createError(final String text, final CategoryElement category) {
    final int pipe = text.lastIndexOf(PIPE, category.end());
    return new CheckError(
        ALGORITHM_NUMBER,
        category.begin(),
        category.end(),
        List.of(new Replacement(text.substring(category.begin(), pipe) + "]]", false)));
  }

  private static CheckError createError(
      final String text,
      final InternalLinkElement link,
      final LinkTextMatch match,
      @Nullable final String apostropheTemplate) {
    final int begin = match.risk() ? skipApostrophesBackward(text, link.begin()) : link.begin();
    final int end = match.risk() ? skipApostrophesForward(text, link.end()) : link.end();
    final Set<String> replacements =
        createReplacements(
            link,
            match,
            text.substring(begin, link.begin()),
            text.substring(link.end(), end),
            apostropheTemplate);
    final String automaticReplacement = replacements.iterator().next();
    return new CheckError(
        ALGORITHM_NUMBER,
        begin,
        end,
        replacements.stream()
            .map(
                replacement ->
                    new Replacement(
                        replacement, match.automatic() && replacement.equals(automaticReplacement)))
            .toList());
  }

  private static int skipApostrophesBackward(final String text, final int index) {
    int begin = index;
    while (begin > 0 && text.charAt(begin - 1) == APOSTROPHE) {
      begin--;
    }
    return begin;
  }

  private static int skipApostrophesForward(final String text, final int index) {
    int end = index;
    while (end < text.length() && text.charAt(end) == APOSTROPHE) {
      end++;
    }
    return end;
  }

  private static Set<String> createReplacements(
      final InternalLinkElement link,
      final LinkTextMatch match,
      final String prefix,
      final String suffix,
      @Nullable final String apostropheTemplate) {
    final List<String> linkTexts = List.of(match.text(), match.text().replace(UNDERSCORE, SPACE));
    final Set<String> replacements = new LinkedHashSet<>();
    for (final String before : withoutApostrophes(match.before())) {
      for (final String after : withoutApostrophes(match.after())) {
        for (final String linkText : linkTexts) {
          final String replacement =
              before
                  + InternalLinkBuilder.to(linkText).withColon(link.leadingColon()).build()
                  + after;
          addReplacements(replacements, prefix, replacement, suffix, apostropheTemplate);
        }
      }
    }
    return replacements;
  }

  private static List<String> withoutApostrophes(final String padding) {
    return List.of(padding, padding.replace(String.valueOf(APOSTROPHE), ""));
  }

  private static void addReplacements(
      final Set<String> replacements,
      final String prefix,
      final String replacement,
      final String suffix,
      @Nullable final String apostropheTemplate) {
    replacements.add(prefix + replacement + suffix);
    if (apostropheTemplate != null && !(prefix + suffix).isEmpty()) {
      final String apostrophe = "{{" + apostropheTemplate + "}}";
      replacements.add(
          prefix.replace(String.valueOf(APOSTROPHE), apostrophe)
              + replacement
              + suffix.replace(String.valueOf(APOSTROPHE), apostrophe));
    }
  }
}
