package org.wpcleaner.application.gui.javafx.analysis;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.api.api.ApiError;
import org.wpcleaner.api.api.expandtemplates.ExpandTemplates;
import org.wpcleaner.api.api.expandtemplates.ExpandTemplatesParameters;
import org.wpcleaner.api.api.expandtemplates.ExpandTemplatesQuery;
import org.wpcleaner.api.api.expandtemplates.MockApiExpandTemplates;
import org.wpcleaner.api.repository.magicword.MagicWord;
import org.wpcleaner.api.repository.magicword.MagicWordRepository;
import org.wpcleaner.api.wiki.definition.WikiDefinition;

final class ExpandTemplatesExpectations {

  private ExpandTemplatesExpectations() {}

  static void givenIfFunction(
      final MagicWordRepository magicWordRepository, final WikiDefinition wiki) {
    magicWordRepository.addMagicWord(
        wiki, new MagicWord("if", List.of("si", "if"), false, false, true, false));
  }

  static void givenExpandedText(
      final MockApiExpandTemplates mockApiExpandTemplates,
      final WikiDefinition wiki,
      final String title,
      final String text,
      @Nullable final String wikiText) {
    mockApiExpandTemplates
        .givenExpandTemplates(wiki, createQuery(title, text))
        .willReturn(
            new ExpandTemplates(
                List.of(), null, Map.of(), List.of(), List.of(), List.of(), null, Map.of(), null,
                null, wikiText));
  }

  static void givenExpansionErrors(
      final MockApiExpandTemplates mockApiExpandTemplates,
      final WikiDefinition wiki,
      final String title,
      final String text,
      final List<ApiError> errors) {
    mockApiExpandTemplates
        .givenExpandTemplates(wiki, createQuery(title, text))
        .willReturnErrors(errors);
  }

  private static ExpandTemplatesQuery createQuery(final String title, final String text) {
    return ExpandTemplatesQuery.emptyBuilder()
        .properties(Set.of(ExpandTemplatesParameters.Properties.WIKITEXT))
        .text(text)
        .title(title)
        .build();
  }
}
