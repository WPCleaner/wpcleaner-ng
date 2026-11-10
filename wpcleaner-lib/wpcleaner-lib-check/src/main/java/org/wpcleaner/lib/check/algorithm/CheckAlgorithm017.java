package org.wpcleaner.lib.check.algorithm;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;
import org.wpcleaner.api.analysis.PageAnalysis;
import org.wpcleaner.api.analysis.category.CategoryElement;
import org.wpcleaner.api.analysis.pagetitle.PageTitle;
import org.wpcleaner.api.analysis.template.TemplateElement;
import org.wpcleaner.api.analysis.template.TemplateParameter;
import org.wpcleaner.api.analysis.title.TitleElement;
import org.wpcleaner.api.repository.namespace.CommonNamespaces;
import org.wpcleaner.lib.check.CheckAlgorithm;
import org.wpcleaner.lib.check.CheckError;
import org.wpcleaner.lib.check.Replacement;
import org.wpcleaner.lib.check.configuration.CheckAlgorithmConfiguration;
import org.wpcleaner.lib.check.configuration.CheckWikiConfigurationRepository;
import org.wpcleaner.lib.check.configuration.TemplateParameters;

@Component
public class CheckAlgorithm017 implements CheckAlgorithm {

  private static final int ALGORITHM_NUMBER = 17;
  private static final String PARAMETER_IGNORE_TEMPLATES = "ignore_templates";
  private static final String PARAMETER_IGNORE_PARAMETERS = "ignore_parameters";
  private static final char SPACE = ' ';
  private static final char LINE_END = '\n';

  private final CheckWikiConfigurationRepository checkWikiRepository;

  public CheckAlgorithm017(final CheckWikiConfigurationRepository checkWikiRepository) {
    this.checkWikiRepository = checkWikiRepository;
  }

  @Override
  public int number() {
    return ALGORITHM_NUMBER;
  }

  @Override
  public List<CheckError> analyze(final PageAnalysis analysis) {
    final Predicate<CategoryElement> ignored = createIgnoredFilter(analysis);
    final int redirectLink =
        analysis.getRedirect().map(redirect -> redirect.link().begin()).orElse(-1);
    final List<CategoryElement> categories =
        analysis.getCategories().stream()
            .filter(category -> category.title() != null && category.begin() != redirectLink)
            .toList();
    final int lastTitleEnd =
        analysis.getTitles().stream().map(TitleElement::end).reduce((_, last) -> last).orElse(0);
    return categories.stream()
        .collect(
            Collectors.groupingBy(
                category -> Objects.requireNonNull(category.title()),
                LinkedHashMap::new,
                Collectors.toList()))
        .values()
        .stream()
        .flatMap(
            group -> {
              final CategoryElement kept = findKeptCategory(group, lastTitleEnd);
              return group.stream()
                  .filter(category -> !category.equals(kept))
                  .filter(Predicate.not(ignored))
                  .map(category -> createError(analysis, categories, category, kept));
            })
        .sorted(Comparator.comparingInt(CheckError::begin))
        .toList();
  }

  private Predicate<CategoryElement> createIgnoredFilter(final PageAnalysis analysis) {
    final TemplateParameters ignored =
        checkWikiRepository
            .getConfiguration(analysis.getWiki())
            .getAlgorithm(ALGORITHM_NUMBER)
            .orElseGet(() -> new CheckAlgorithmConfiguration(ALGORITHM_NUMBER, Map.of()))
            .getTemplateParameters(PARAMETER_IGNORE_TEMPLATES, PARAMETER_IGNORE_PARAMETERS);
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

  private static CategoryElement findKeptCategory(
      final List<CategoryElement> group, final int lastTitleEnd) {
    final Predicate<CategoryElement> afterLastTitle = category -> category.begin() >= lastTitleEnd;
    final Predicate<CategoryElement> withSortKey = category -> !sortKey(category).isEmpty();
    return Stream.of(afterLastTitle.and(withSortKey), afterLastTitle, withSortKey)
        .flatMap(filter -> group.stream().filter(filter).findFirst().stream())
        .findFirst()
        .orElseGet(group::getLast);
  }

  private static String sortKey(final CategoryElement category) {
    return Objects.requireNonNullElse(category.sortKey(), "");
  }

  private static CheckError createError(
      final PageAnalysis analysis,
      final List<CategoryElement> categories,
      final CategoryElement category,
      final CategoryElement kept) {
    final DeletedArea area = DeletedArea.of(analysis.getText(), category);
    return new CheckError(
        ALGORITHM_NUMBER,
        area.begin(),
        area.end(),
        List.of(new Replacement("", isAutomatic(analysis, categories, category, kept))));
  }

  private static boolean isAutomatic(
      final PageAnalysis analysis,
      final List<CategoryElement> categories,
      final CategoryElement category,
      final CategoryElement kept) {
    return category.begin() > kept.begin()
        && hasOnlyCategoriesBetween(analysis.getText(), categories, kept.end(), category.begin())
        && sortKey(category).equals(sortKey(kept))
        && analysis.getFunctions().stream()
            .noneMatch(function -> category.isInside(function) || kept.isInside(function));
  }

  private static boolean hasOnlyCategoriesBetween(
      final String text,
      final List<CategoryElement> categories,
      final int keptEnd,
      final int categoryBegin) {
    int index = keptEnd;
    while (index < categoryBegin) {
      final int current = index;
      if (text.charAt(current) == SPACE || text.charAt(current) == LINE_END) {
        index++;
      } else {
        final Optional<CategoryElement> next =
            categories.stream().filter(category -> category.begin() == current).findFirst();
        if (next.isEmpty()) {
          return false;
        }
        index = next.get().end();
      }
    }
    return true;
  }

  private record DeletedArea(int begin, int end) {

    static DeletedArea of(final String text, final CategoryElement category) {
      final int begin = skipSpacesBackward(text, category.begin());
      final int end = skipSpacesForward(text, category.end());
      final boolean lineBegin = begin == 0 || text.charAt(begin - 1) == LINE_END;
      final boolean lineEnd = end >= text.length() || text.charAt(end) == LINE_END;
      if (!lineBegin || !lineEnd) {
        return new DeletedArea(
            lineBegin ? begin : category.begin(), lineEnd ? end : category.end());
      }
      return new DeletedArea(begin, Math.min(end + 1, text.length()));
    }

    private static int skipSpacesBackward(final String text, final int index) {
      int begin = index;
      while (begin > 0 && text.charAt(begin - 1) == SPACE) {
        begin--;
      }
      return begin;
    }

    private static int skipSpacesForward(final String text, final int index) {
      int end = index;
      while (end < text.length() && text.charAt(end) == SPACE) {
        end++;
      }
      return end;
    }
  }
}
