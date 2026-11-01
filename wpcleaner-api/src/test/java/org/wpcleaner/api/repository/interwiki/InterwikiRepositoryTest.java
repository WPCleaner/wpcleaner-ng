package org.wpcleaner.api.repository.interwiki;

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

class InterwikiRepositoryTest {

  private static final WikiDefinition WIKI = WikipediaDefinitions.EN;

  @DisplayName("Should add interwiki and sort by prefix, ignoring duplicates")
  @Test
  void addInterwiki() {
    // GIVEN
    final InterwikiRepository repository = new InterwikiRepository();
    final Interwiki first =
        new Interwiki("en", true, "https://en.wikipedia.org/wiki/$1", "English", null);
    final Interwiki second =
        new Interwiki("fr", true, "https://fr.wikipedia.org/wiki/$1", "French", null);
    final Interwiki duplicateEn = new Interwiki("en", false, "https://example.com/$1", null, "");

    // WHEN
    repository.addInterwiki(WIKI, second);
    repository.addInterwiki(WIKI, first);
    repository.addInterwiki(WIKI, duplicateEn);

    // THEN
    final List<Interwiki> interwikis = repository.getInterwikis(WIKI);
    Assertions.assertThat(interwikis).hasSize(2);
    Assertions.assertThat(interwikis.get(0).prefix()).isEqualTo("en");
    Assertions.assertThat(interwikis.get(0).local()).isTrue();
    Assertions.assertThat(interwikis.get(0).url()).isEqualTo("https://en.wikipedia.org/wiki/$1");
    Assertions.assertThat(interwikis.get(0).language()).isEqualTo("English");
    Assertions.assertThat(interwikis.get(0).deprecated()).isNull();

    Assertions.assertThat(interwikis.get(1).prefix()).isEqualTo("fr");
  }

  @DisplayName("Should keep interwikis separate for each wiki")
  @Test
  void addInterwikiForEachWiki() {
    // GIVEN
    final InterwikiRepository repository = new InterwikiRepository();
    final Interwiki interwiki =
        new Interwiki("fr", true, "https://fr.wikipedia.org/wiki/$1", "French", null);

    // WHEN
    repository.addInterwiki(WIKI, interwiki);

    // THEN
    Assertions.assertThat(repository.getInterwikis(WIKI)).containsExactly(interwiki);
    Assertions.assertThat(repository.getInterwikis(WikipediaDefinitions.FR)).isEmpty();
  }
}
