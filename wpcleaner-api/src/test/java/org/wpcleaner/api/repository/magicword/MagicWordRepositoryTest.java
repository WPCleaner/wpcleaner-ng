package org.wpcleaner.api.repository.magicword;

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

class MagicWordRepositoryTest {

  private static final WikiDefinition WIKI = WikipediaDefinitions.EN;

  @DisplayName("Should add magic words, sort them, and ignore duplicates")
  @Test
  void addMagicWord() {
    // GIVEN
    final MagicWordRepository repository = new MagicWordRepository();
    final MagicWord pagename = new MagicWord("pagename", List.of("PAGENAME"), true, true, true);
    final MagicWord ifMagicWord = new MagicWord("if", List.of("if"), false, true, false);
    final MagicWord duplicatePagename =
        new MagicWord("pagename", List.of("PAGENAME"), true, true, true);

    // WHEN
    repository.addMagicWord(WIKI, pagename);
    repository.addMagicWord(WIKI, ifMagicWord);
    repository.addMagicWord(WIKI, duplicatePagename);

    // THEN
    final List<MagicWord> magicWords = repository.getMagicWords(WIKI);
    Assertions.assertThat(magicWords).containsExactly(ifMagicWord, pagename);
  }

  @DisplayName("Should keep magic words separate for each wiki")
  @Test
  void addMagicWordForEachWiki() {
    // GIVEN
    final MagicWordRepository repository = new MagicWordRepository();
    final MagicWord pagename = new MagicWord("pagename", List.of("PAGENAME"), true, true, true);

    // WHEN
    repository.addMagicWord(WIKI, pagename);

    // THEN
    Assertions.assertThat(repository.getMagicWords(WIKI)).containsExactly(pagename);
    Assertions.assertThat(repository.getMagicWords(WikipediaDefinitions.FR)).isEmpty();
  }
}
