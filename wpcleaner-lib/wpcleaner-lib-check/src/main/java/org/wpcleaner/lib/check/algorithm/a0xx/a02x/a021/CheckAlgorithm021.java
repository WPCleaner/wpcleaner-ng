package org.wpcleaner.lib.check.algorithm.a0xx.a02x.a021;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;
import org.wpcleaner.api.analysis.PageAnalysis;
import org.wpcleaner.api.analysis.category.CategoryBuilder;
import org.wpcleaner.api.analysis.category.CategoryElement;
import org.wpcleaner.api.analysis.comment.CommentBuilder;
import org.wpcleaner.api.repository.namespace.CommonNamespaces;
import org.wpcleaner.api.repository.namespace.Namespace;
import org.wpcleaner.api.utils.GT;
import org.wpcleaner.lib.check.CheckAlgorithm;
import org.wpcleaner.lib.check.CheckError;
import org.wpcleaner.lib.check.Replacement;

@Component
public class CheckAlgorithm021 implements CheckAlgorithm {

  private static final int ALGORITHM_NUMBER = 21;
  private static final String ENGLISH_NAMESPACE = "Category";

  @Override
  public int number() {
    return ALGORITHM_NUMBER;
  }

  @Override
  public List<CheckError> analyze(final PageAnalysis analysis) {
    final Optional<Namespace> categoryNamespace =
        analysis.getNamespace(CommonNamespaces.CATEGORY.id);
    if (categoryNamespace.isEmpty() || isEnglish(categoryNamespace.get().name())) {
      return List.of();
    }
    final List<String> localNamespaces = getLocalNamespaces(categoryNamespace.get());
    return analysis.getCategories().stream()
        .filter(category -> category.title() != null && isEnglish(category.namespace()))
        .map(category -> createError(analysis, category, localNamespaces))
        .toList();
  }

  private static boolean isEnglish(final String namespace) {
    return ENGLISH_NAMESPACE.equalsIgnoreCase(namespace);
  }

  private static List<String> getLocalNamespaces(final Namespace namespace) {
    return Stream.concat(Stream.of(namespace.name()), namespace.aliases().stream()).toList();
  }

  private static CheckError createError(
      final PageAnalysis analysis,
      final CategoryElement category,
      final List<String> localNamespaces) {
    final Stream<Replacement> localized =
        localNamespaces.stream()
            .map(
                namespace ->
                    new Replacement(
                        CategoryBuilder.to(namespace, category.name())
                            .withSortKey(category.sortKey())
                            .build(),
                        false));
    final Replacement commentedOut =
        new Replacement(
            CommentBuilder.from(analysis.getText().substring(category.begin(), category.end()))
                .build(),
            GT._T("Comment category out"),
            false);
    return new CheckError(
        ALGORITHM_NUMBER,
        category.begin(),
        category.end(),
        Stream.concat(localized, Stream.of(commentedOut)).toList());
  }
}
