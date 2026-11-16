package org.wpcleaner.lib.check.algorithm.a0xx.a00x.a009;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import org.springframework.stereotype.Component;
import org.wpcleaner.api.analysis.PageAnalysis;
import org.wpcleaner.api.analysis.category.CategoryElement;
import org.wpcleaner.lib.check.CheckAlgorithm;
import org.wpcleaner.lib.check.CheckError;
import org.wpcleaner.lib.check.Replacement;
import org.wpcleaner.lib.check.algorithm.IgnoredTemplates;
import org.wpcleaner.lib.check.configuration.CheckAlgorithmConfiguration;
import org.wpcleaner.lib.check.configuration.CheckWikiConfigurationRepository;

@Component
public class CheckAlgorithm009 implements CheckAlgorithm {

  private static final int ALGORITHM_NUMBER = 9;
  private static final String PARAMETER_IGNORE_TEMPLATES = "ignore_templates";
  private static final String PARAMETER_IGNORE_PARAMETERS = "ignore_parameters";
  private static final char SPACE = ' ';
  private static final char LINE_END = '\n';
  private static final int TWO_CATEGORIES = 2;
  private static final String TWO_CATEGORIES_LABEL = "[[...]]↵[[...]]";
  private static final String MORE_CATEGORIES_LABEL = "[[...]]↵...↵[[...]]";

  private final CheckWikiConfigurationRepository checkWikiRepository;

  public CheckAlgorithm009(final CheckWikiConfigurationRepository checkWikiRepository) {
    this.checkWikiRepository = checkWikiRepository;
  }

  @Override
  public int number() {
    return ALGORITHM_NUMBER;
  }

  @Override
  public List<CheckError> analyze(final PageAnalysis analysis) {
    final List<CategoryElement> categories = analysis.getCategories();
    final List<List<CategoryElement>> lines =
        groupByLine(analysis.getText(), categories).stream()
            .filter(line -> line.size() >= TWO_CATEGORIES)
            .toList();
    if (lines.isEmpty()) {
      return List.of();
    }
    final Predicate<CategoryElement> ignored = createIgnoredFilter(analysis);
    final CategoryElement firstCategory = categories.getFirst();
    return lines.stream()
        .filter(line -> !ignored.test(line.getFirst()))
        .map(line -> createError(analysis, line, line.getFirst().equals(firstCategory)))
        .toList();
  }

  private static List<List<CategoryElement>> groupByLine(
      final String text, final List<CategoryElement> categories) {
    final List<List<CategoryElement>> lines = new ArrayList<>();
    int previousEnd = 0;
    for (final CategoryElement category : categories) {
      final int lineEnd = text.indexOf(LINE_END, previousEnd);
      if (lines.isEmpty() || (lineEnd >= 0 && lineEnd < category.begin())) {
        lines.add(new ArrayList<>());
      }
      lines.getLast().add(category);
      previousEnd = category.end();
    }
    return lines;
  }

  private Predicate<CategoryElement> createIgnoredFilter(final PageAnalysis analysis) {
    return IgnoredTemplates.createIgnoredFilter(
        analysis,
        checkWikiRepository
            .getConfiguration(analysis.getWiki())
            .getAlgorithm(ALGORITHM_NUMBER)
            .orElseGet(() -> new CheckAlgorithmConfiguration(ALGORITHM_NUMBER, Map.of()))
            .getTemplateParameters(PARAMETER_IGNORE_TEMPLATES, PARAMETER_IGNORE_PARAMETERS));
  }

  private static CheckError createError(
      final PageAnalysis analysis, final List<CategoryElement> line, final boolean firstLine) {
    final String text = analysis.getText();
    final CategoryElement first = line.getFirst();
    final CategoryElement last = line.getLast();
    final LinePrefix prefix =
        firstLine ? LinePrefix.of(analysis, first) : LinePrefix.none(first.begin());
    final StringBuilder replacement = new StringBuilder(prefix.text());
    IntStream.range(0, line.size() - 1)
        .forEach(
            index ->
                replacement
                    .append(
                        text.substring(line.get(index).begin(), line.get(index + 1).begin())
                            .strip())
                    .append(LINE_END));
    replacement.append(text, last.begin(), last.end());
    return new CheckError(
        ALGORITHM_NUMBER,
        prefix.begin(),
        last.end(),
        List.of(
            new Replacement(
                replacement.toString(),
                line.size() > TWO_CATEGORIES ? MORE_CATEGORIES_LABEL : TWO_CATEGORIES_LABEL,
                isAutomatic(analysis, line))));
  }

  private static boolean isAutomatic(
      final PageAnalysis analysis, final List<CategoryElement> line) {
    final String text = analysis.getText();
    final CategoryElement first = line.getFirst();
    return !first.isInsideOne(analysis.getTemplates())
        && !first.isInsideOne(analysis.getFunctions())
        && IntStream.range(0, line.size() - 1)
            .allMatch(
                index ->
                    text.substring(line.get(index).end(), line.get(index + 1).begin())
                        .chars()
                        .allMatch(character -> character == SPACE));
  }
}
