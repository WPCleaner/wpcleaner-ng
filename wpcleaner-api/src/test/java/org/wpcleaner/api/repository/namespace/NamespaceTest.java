package org.wpcleaner.api.repository.namespace;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.stream.Stream;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.wpcleaner.api.repository.CaseType;

class NamespaceTest {

  @DisplayName("should accept possible names")
  @MethodSource("possibleNames")
  @ParameterizedTest(name = "{0}")
  void testPossibleName(final String useCase, final Namespace namespace, final String actualName) {
    // WHEN
    final boolean result = namespace.isPossibleName(actualName);

    // THEN
    Assertions.assertThat(result).isTrue();
  }

  static Stream<Arguments> possibleNames() {
    final Namespace categoryNamespace =
        new Namespace(
            CommonNamespaces.CATEGORY.id,
            "Category",
            "Catégorie",
            List.of("Cat"),
            CaseType.FIRST_LETTER);
    return Stream.of(
        Arguments.of("Canonical", categoryNamespace, "Category"),
        Arguments.of("Canonical lowercase", categoryNamespace, "category"),
        Arguments.of("Local name", categoryNamespace, "Catégorie"),
        Arguments.of("Local name lowercase", categoryNamespace, "catégorie"),
        Arguments.of("Alias", categoryNamespace, "Cat"),
        Arguments.of("Alias lowercase", categoryNamespace, "cat"));
  }
}
