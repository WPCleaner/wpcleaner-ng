package org.wpcleaner.api.repository.protocol;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ProtocolTest {

  @DisplayName("Should find protocol by value when exists, or return empty optional")
  @Test
  void findProtocol() {
    // GIVEN
    final Protocol http = new Protocol("http://");
    final Protocol https = new Protocol("https://");
    final List<Protocol> protocols = List.of(http, https);

    // WHEN
    final Optional<Protocol> foundHttp = Protocol.findProtocol(protocols, "http://");
    final Optional<Protocol> foundHttps = Protocol.findProtocol(protocols, "https://");
    final Optional<Protocol> foundFtp = Protocol.findProtocol(protocols, "ftp://");

    // THEN
    Assertions.assertThat(foundHttp).hasValue(http);
    Assertions.assertThat(foundHttps).hasValue(https);
    Assertions.assertThat(foundFtp).isEmpty();
  }
}
