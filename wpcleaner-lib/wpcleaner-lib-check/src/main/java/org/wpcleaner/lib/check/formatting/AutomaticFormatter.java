package org.wpcleaner.lib.check.formatting;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import org.springframework.stereotype.Service;
import org.wpcleaner.api.analysis.PageAnalysis;
import org.wpcleaner.api.repository.configuration.WPCleanerConfigurationRepository;
import org.wpcleaner.api.repository.configuration.WPCleanerParameter;
import org.wpcleaner.api.repository.namespace.CommonNamespaces;

@Service
public class AutomaticFormatter {

  private final WPCleanerConfigurationRepository wpcleanerRepository;
  private final List<FormattingFix> fixes;

  public AutomaticFormatter(
      final WPCleanerConfigurationRepository wpcleanerRepository,
      final SpaceAroundTitleFix spaceAroundTitleFix,
      final LinkDefaultSortCategoryFix linkDefaultSortCategoryFix,
      final LanguageLinksAfterCategoryFix languageLinksAfterCategoryFix,
      final CrBeforeCategoryFix crBeforeCategoryFix,
      final CrDefaultSortCategoryFix crDefaultSortCategoryFix,
      final CrBetweenCategoryFix crBetweenCategoryFix,
      final EndOfArticleFix endOfArticleFix) {
    this.wpcleanerRepository = wpcleanerRepository;
    this.fixes =
        List.of(
            spaceAroundTitleFix,
            linkDefaultSortCategoryFix,
            languageLinksAfterCategoryFix,
            crBeforeCategoryFix,
            crDefaultSortCategoryFix,
            crBetweenCategoryFix,
            endOfArticleFix);
  }

  public String tidyArticle(final PageAnalysis analysis) {
    if (analysis.getPageTitle().namespace() != CommonNamespaces.MAIN.id
        || !wpcleanerRepository
            .getConfiguration(analysis.getWiki())
            .getBoolean(WPCleanerParameter.AUTO_ACTIVE)) {
      return analysis.getText();
    }
    PageAnalysis current = analysis;
    for (final FormattingFix fix : fixes) {
      final String text = fix.fix(current);
      if (!text.equals(current.getText())) {
        current = current.withText(text);
      }
    }
    return current.getText();
  }
}
