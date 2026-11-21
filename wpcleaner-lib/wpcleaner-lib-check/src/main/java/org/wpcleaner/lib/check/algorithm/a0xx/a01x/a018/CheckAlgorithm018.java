package org.wpcleaner.lib.check.algorithm.a0xx.a01x.a018;

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
import org.wpcleaner.api.analysis.pagetitle.PageTitle;
import org.wpcleaner.api.repository.namespace.CommonNamespaces;
import org.wpcleaner.api.repository.namespace.Namespace;
import org.wpcleaner.api.utils.StringUtils;
import org.wpcleaner.lib.check.CheckAlgorithm;
import org.wpcleaner.lib.check.CheckError;
import org.wpcleaner.lib.check.Replacement;

@Component
public class CheckAlgorithm018 implements CheckAlgorithm {

  private static final int ALGORITHM_NUMBER = 18;

  @Override
  public int number() {
    return ALGORITHM_NUMBER;
  }

  @Override
  public List<CheckError> analyze(final PageAnalysis analysis) {
    final Optional<String> localNamespace =
        analysis.getNamespace(CommonNamespaces.CATEGORY.id).map(Namespace::name);
    return analysis.getCategories().stream()
        .filter(category -> hasLowerCaseNamespace(category) || hasLowerCaseName(category))
        .map(category -> createError(category, localNamespace))
        .toList();
  }

  private static boolean hasLowerCaseNamespace(final CategoryElement category) {
    return category.title() != null && startsWithLowerCase(category.namespace());
  }

  private static boolean hasLowerCaseName(final CategoryElement category) {
    final PageTitle title = category.title();
    return title != null
        && startsWithLowerCase(category.name())
        && title.text().codePointAt(0) != category.name().codePointAt(0);
  }

  private static boolean startsWithLowerCase(final String value) {
    return !value.isEmpty() && Character.isLowerCase(value.codePointAt(0));
  }

  private static CheckError createError(
      final CategoryElement category, final Optional<String> localNamespace) {
    final String namespace = StringUtils.firstLetterTitleCase(category.namespace());
    final String name = StringUtils.firstLetterTitleCase(category.name());
    final Stream<Replacement> automatic =
        Stream.of(new Replacement(createCategory(namespace, name, category), true));
    final Stream<Replacement> localized =
        localNamespace.filter(local -> !local.equals(namespace)).stream()
            .map(local -> new Replacement(createCategory(local, name, category), false));
    return new CheckError(
        ALGORITHM_NUMBER,
        category.begin(),
        category.end(),
        Stream.concat(automatic, localized).toList());
  }

  private static String createCategory(
      final String namespace, final String name, final CategoryElement category) {
    return CategoryBuilder.to(namespace, name).withSortKey(category.sortKey()).build();
  }
}
