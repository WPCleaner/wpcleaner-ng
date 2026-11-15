package org.wpcleaner.api.hook.login;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.wpcleaner.api.api.query.meta.siteinfo.General;
import org.wpcleaner.api.api.query.meta.siteinfo.SiteInfo;
import org.wpcleaner.api.repository.linter.LinterCategory;
import org.wpcleaner.api.repository.linter.LinterCategoryRepository;
import org.wpcleaner.api.repository.linter.LinterPriority;
import org.wpcleaner.api.wiki.definition.WikiDefinition;

@Service
public class LinterCategoryExtractor {

  private final LinterCategoryRepository repository;

  public LinterCategoryExtractor(final LinterCategoryRepository repository) {
    this.repository = repository;
  }

  public void extract(final WikiDefinition wiki, final SiteInfo siteInfo) {
    final Map<String, List<String>> linter =
        Optional.ofNullable(siteInfo.general()).map(General::linter).orElse(Map.of());
    repository.setCategories(
        wiki,
        linter.entrySet().stream()
            .flatMap(
                entry ->
                    LinterPriority.findByValue(entry.getKey()).stream()
                        .flatMap(
                            priority ->
                                entry.getValue().stream()
                                    .map(name -> new LinterCategory(name, priority))))
            .toList());
  }
}
