package org.wpcleaner.api.repository.protocol;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;

@Service
public class ProtocolRepository {

  private final List<Protocol> protocols = new ArrayList<>();

  public void addProtocol(final Protocol protocol) {
    if (protocols.stream()
        .map(Protocol::value)
        .anyMatch(value -> Objects.equals(value, protocol.value()))) {
      return;
    }
    protocols.add(protocol);
    protocols.sort(Comparator.comparing(Protocol::value));
  }

  public List<Protocol> getProtocols() {
    return List.copyOf(protocols);
  }
}
