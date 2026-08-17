package org.wpcleaner.api.repository.protocol;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record Protocol(String value) {

  public static Optional<Protocol> findProtocol(
      final List<Protocol> protocols, final String value) {
    return protocols.stream()
        .filter(protocol -> Objects.equals(protocol.value(), value))
        .findFirst();
  }
}
