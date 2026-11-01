package org.wpcleaner.api.repository.protocol;

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

class ProtocolRepositoryTest {

  private static final WikiDefinition WIKI = WikipediaDefinitions.EN;

  @DisplayName("Should add protocols, sort them, and ignore duplicates")
  @Test
  void addProtocol() {
    // GIVEN
    final ProtocolRepository repository = new ProtocolRepository();
    final Protocol https = new Protocol("https://");
    final Protocol http = new Protocol("http://");
    final Protocol duplicateHttps = new Protocol("https://");

    // WHEN
    repository.addProtocol(WIKI, https);
    repository.addProtocol(WIKI, http);
    repository.addProtocol(WIKI, duplicateHttps);

    // THEN
    final List<Protocol> protocols = repository.getProtocols(WIKI);
    Assertions.assertThat(protocols).hasSize(2);
    Assertions.assertThat(protocols.get(0).value()).isEqualTo("http://");
    Assertions.assertThat(protocols.get(1).value()).isEqualTo("https://");
  }

  @DisplayName("Should keep protocols separate for each wiki")
  @Test
  void addProtocolForEachWiki() {
    // GIVEN
    final ProtocolRepository repository = new ProtocolRepository();
    final Protocol https = new Protocol("https://");

    // WHEN
    repository.addProtocol(WIKI, https);

    // THEN
    Assertions.assertThat(repository.getProtocols(WIKI)).containsExactly(https);
    Assertions.assertThat(repository.getProtocols(WikipediaDefinitions.FR)).isEmpty();
  }
}
