package org.wpcleaner.lib.check.algorithm.a0xx.a02x.a022;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;
import org.wpcleaner.api.analysis.PageAnalysis;
import org.wpcleaner.api.analysis.category.CategoryBuilder;
import org.wpcleaner.api.analysis.category.CategoryElement;
import org.wpcleaner.api.repository.CaseType;
import org.wpcleaner.api.repository.namespace.CommonNamespaces;
import org.wpcleaner.api.repository.namespace.Namespace;
import org.wpcleaner.api.utils.StringUtils;
import org.wpcleaner.lib.check.CheckAlgorithm;
import org.wpcleaner.lib.check.CheckError;
import org.wpcleaner.lib.check.Replacement;

@Component
public class CheckAlgorithm022 implements CheckAlgorithm {

  private static final int ALGORITHM_NUMBER = 22;

  @Override
  public int number() {
    return ALGORITHM_NUMBER;
  }

  @Override
  public List<CheckError> analyze(final PageAnalysis analysis) {
    final Optional<Namespace> categoryNamespace =
        analysis.getNamespace(CommonNamespaces.CATEGORY.id);
    return analysis.getCategories().stream()
        .filter(category -> category.title() != null && !containsComment(analysis, category))
        .map(category -> createError(analysis, category, categoryNamespace))
        .flatMap(Optional::stream)
        .toList();
  }

  private static boolean containsComment(
      final PageAnalysis analysis, final CategoryElement category) {
    return analysis.getComments().stream()
        .anyMatch(comment -> comment.begin() < category.end() && comment.end() > category.begin());
  }

  private static Optional<CheckError> createError(
      final PageAnalysis analysis,
      final CategoryElement category,
      final Optional<Namespace> categoryNamespace) {
    final String sortKey = stripTrailingWhitespace(category.sortKey());
    final String fixed = createCategory(category.namespace(), category.name(), sortKey);
    if (fixed.equals(analysis.getText().substring(category.begin(), category.end()))) {
      return Optional.empty();
    }
    final String name = normalizeFirstLetter(category.name(), categoryNamespace);
    final Stream<Replacement> automatic =
        Stream.of(new Replacement(createCategory(category.namespace(), name, sortKey), true));
    final Stream<Replacement> localized =
        categoryNamespace
            .map(Namespace::name)
            .filter(local -> !local.equals(category.namespace()))
            .stream()
            .map(local -> new Replacement(createCategory(local, name, sortKey), false));
    return Optional.of(
        new CheckError(
            ALGORITHM_NUMBER,
            category.begin(),
            category.end(),
            Stream.concat(automatic, localized).toList()));
  }

  private static String normalizeFirstLetter(
      final String name, final Optional<Namespace> categoryNamespace) {
    return categoryNamespace.map(Namespace::caseType).orElse(CaseType.FIRST_LETTER)
            == CaseType.FIRST_LETTER
        ? StringUtils.firstLetterTitleCase(name)
        : name;
  }

  @Nullable
  private static String stripTrailingWhitespace(@Nullable final String sortKey) {
    if (sortKey == null || sortKey.isBlank()) {
      return sortKey;
    }
    int end = sortKey.length();
    while (end > 0 && Character.isWhitespace(sortKey.charAt(end - 1))) {
      end--;
    }
    return sortKey.substring(0, end);
  }

  private static String createCategory(
      final String namespace, final String name, @Nullable final String sortKey) {
    return CategoryBuilder.to(namespace, name).withSortKey(sortKey).build();
  }
}
