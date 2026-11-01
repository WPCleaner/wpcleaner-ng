package org.wpcleaner.api.repository.extensiontag;

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

class ExtensionTagRepositoryTest {

  private static final WikiDefinition WIKI = WikipediaDefinitions.EN;

  @DisplayName("Should add extension tags, sort them, and ignore duplicates")
  @Test
  void addExtensionTag() {
    // GIVEN
    final ExtensionTagRepository repository = new ExtensionTagRepository();
    final ExtensionTag ref = new ExtensionTag("ref");
    final ExtensionTag nowiki = new ExtensionTag("nowiki");
    final ExtensionTag duplicateRef = new ExtensionTag("ref");

    // WHEN
    repository.addExtensionTag(WIKI, ref);
    repository.addExtensionTag(WIKI, nowiki);
    repository.addExtensionTag(WIKI, duplicateRef);

    // THEN
    final List<ExtensionTag> extensionTags = repository.getExtensionTags(WIKI);
    Assertions.assertThat(extensionTags).containsExactly(nowiki, ref);
  }

  @DisplayName("Should keep extension tags separate for each wiki")
  @Test
  void addExtensionTagForEachWiki() {
    // GIVEN
    final ExtensionTagRepository repository = new ExtensionTagRepository();
    final ExtensionTag ref = new ExtensionTag("ref");

    // WHEN
    repository.addExtensionTag(WIKI, ref);

    // THEN
    Assertions.assertThat(repository.getExtensionTags(WIKI)).containsExactly(ref);
    Assertions.assertThat(repository.getExtensionTags(WikipediaDefinitions.FR)).isEmpty();
  }
}
