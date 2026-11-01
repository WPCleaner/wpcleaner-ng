package org.wpcleaner.api.repository.magiclink;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.api.wiki.definition.WikipediaDefinitions;

class MagicLinkRepositoryTest {

  private static final WikiDefinition WIKI = WikipediaDefinitions.EN;

  @DisplayName("Should enable magic links for each wiki")
  @Test
  void enableMagicLink() {
    // GIVEN
    final MagicLinkRepository repository = new MagicLinkRepository();

    // WHEN
    repository.enableMagicLink(WIKI, MagicLinkType.ISBN);
    repository.enableMagicLink(WIKI, MagicLinkType.PMID);
    repository.enableMagicLink(WIKI, MagicLinkType.ISBN);

    // THEN
    Assertions.assertThat(repository.getMagicLinks(WIKI))
        .containsExactlyInAnyOrder(MagicLinkType.ISBN, MagicLinkType.PMID);
    Assertions.assertThat(repository.getMagicLinks(WikipediaDefinitions.FR)).isEmpty();
  }
}
