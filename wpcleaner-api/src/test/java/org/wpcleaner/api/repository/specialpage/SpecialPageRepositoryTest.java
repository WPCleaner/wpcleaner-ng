package org.wpcleaner.api.repository.specialpage;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.api.wiki.definition.WikipediaDefinitions;

class SpecialPageRepositoryTest {

  private static final WikiDefinition WIKI = WikipediaDefinitions.FR;

  @DisplayName("Should find special pages by real name or alias, ignoring case and underscores")
  @Test
  void findSpecialPage() {
    // GIVEN
    final SpecialPageRepository repository = new SpecialPageRepository();
    repository.addSpecialPage(WIKI, new SpecialPage("Allpages", List.of("Toutes les pages")));
    repository.addSpecialPage(
        WIKI, new SpecialPage("Prefixindex", List.of("Index", "PrefixIndex")));

    // WHEN
    final List<String> names =
        List.of("Prefixindex", "index", " PREFIXINDEX ", "Toutes_les_pages", "Other");

    // THEN
    Assertions.assertThat(names)
        .map(name -> repository.findSpecialPage(WIKI, name).map(SpecialPage::realName))
        .extracting(found -> found.orElse(null))
        .containsExactly("Prefixindex", "Prefixindex", "Prefixindex", "Allpages", null);
    Assertions.assertThat(repository.findSpecialPage(WikipediaDefinitions.EN, "Index")).isEmpty();
  }
}
