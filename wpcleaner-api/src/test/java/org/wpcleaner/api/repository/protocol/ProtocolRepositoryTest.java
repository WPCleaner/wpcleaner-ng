package org.wpcleaner.api.repository.protocol;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ProtocolRepositoryTest {

  @DisplayName("Should add protocols, sort them, and ignore duplicates")
  @Test
  void addProtocol() {
    // GIVEN
    final ProtocolRepository repository = new ProtocolRepository();
    final Protocol https = new Protocol("https://");
    final Protocol http = new Protocol("http://");
    final Protocol duplicateHttps = new Protocol("https://");

    // WHEN
    repository.addProtocol(https);
    repository.addProtocol(http);
    repository.addProtocol(duplicateHttps);

    // THEN
    final List<Protocol> protocols = repository.getProtocols();
    Assertions.assertThat(protocols).hasSize(2);
    Assertions.assertThat(protocols.get(0).value()).isEqualTo("http://");
    Assertions.assertThat(protocols.get(1).value()).isEqualTo("https://");
  }
}
