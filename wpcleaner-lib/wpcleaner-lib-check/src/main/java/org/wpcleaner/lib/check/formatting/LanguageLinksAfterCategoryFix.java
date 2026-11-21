package org.wpcleaner.lib.check.formatting;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import org.springframework.stereotype.Component;
import org.wpcleaner.api.analysis.Element;
import org.wpcleaner.api.analysis.PageAnalysis;
import org.wpcleaner.api.analysis.category.CategoryElement;
import org.wpcleaner.api.analysis.languagelink.LanguageLinkElement;
import org.wpcleaner.api.repository.configuration.WPCleanerConfigurationRepository;
import org.wpcleaner.api.repository.configuration.WPCleanerParameter;

@Component
public class LanguageLinksAfterCategoryFix implements FormattingFix {

  private final WPCleanerConfigurationRepository wpcleanerRepository;

  public LanguageLinksAfterCategoryFix(final WPCleanerConfigurationRepository wpcleanerRepository) {
    this.wpcleanerRepository = wpcleanerRepository;
  }

  @Override
  public String fix(final PageAnalysis analysis) {
    final String text = analysis.getText();
    final List<CategoryElement> categories = analysis.getCategories();
    final List<LanguageLinkElement> links = analysis.getLanguageLinks();
    if (!wpcleanerRepository
            .getConfiguration(analysis.getWiki())
            .getBoolean(WPCleanerParameter.AUTO_LANGLINK_AFTER_CATEGORY)
        || categories.isEmpty()
        || links.isEmpty()
        || links.getFirst().begin() >= categories.getLast().end()) {
      return text;
    }
    final List<TextChange> changes = new ArrayList<>();
    int linkIndex = 0;
    while (linkIndex < links.size()) {
      final int groupBegin =
          FormattingText.skipWhitespaceBefore(text, links.get(linkIndex).begin());
      while (linkIndex + 1 < links.size()
          && FormattingText.isOnlyWhitespaceBetween(
              text, links.get(linkIndex), links.get(linkIndex + 1))) {
        linkIndex++;
      }
      final int groupEnd = links.get(linkIndex).end();
      final OptionalInt categoriesEnd = findCategoriesEnd(analysis, groupEnd);
      if (categoriesEnd.isPresent()) {
        changes.add(
            new TextChange(
                groupBegin,
                categoriesEnd.getAsInt(),
                text.substring(groupEnd, categoriesEnd.getAsInt())
                    + text.substring(groupBegin, groupEnd)));
      }
      linkIndex++;
    }
    return TextChange.apply(text, changes);
  }

  private static OptionalInt findCategoriesEnd(final PageAnalysis analysis, final int begin) {
    final String text = analysis.getText();
    OptionalInt categoriesEnd = OptionalInt.empty();
    Optional<Element> element =
        FormattingText.findCategoryOrDefaultSortAt(
            analysis, FormattingText.skipWhitespaceAfter(text, begin));
    while (element.isPresent()) {
      final int elementEnd = element.get().end();
      categoriesEnd = OptionalInt.of(elementEnd);
      element =
          FormattingText.findCategoryOrDefaultSortAt(
              analysis, FormattingText.skipWhitespaceAfter(text, elementEnd));
    }
    return categoriesEnd;
  }
}
