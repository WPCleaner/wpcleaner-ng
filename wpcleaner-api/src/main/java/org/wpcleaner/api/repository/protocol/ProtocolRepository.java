package org.wpcleaner.api.repository.protocol;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.springframework.stereotype.Service;
import org.wpcleaner.api.api.ConnectedUser;
import org.wpcleaner.api.api.LogoutHook;
import org.wpcleaner.api.wiki.definition.WikiDefinition;

@Service
public class ProtocolRepository implements LogoutHook {

  private final Map<WikiDefinition, List<Protocol>> protocols = new HashMap<>();
  private final Lock protocolsLock = new ReentrantLock();

  public void addProtocol(final WikiDefinition wiki, final Protocol protocol) {
    protocolsLock.lock();
    try {
      final List<Protocol> wikiProtocols = protocols.computeIfAbsent(wiki, _ -> new ArrayList<>());
      if (wikiProtocols.stream()
          .map(Protocol::value)
          .anyMatch(value -> Objects.equals(value, protocol.value()))) {
        return;
      }
      wikiProtocols.add(protocol);
      wikiProtocols.sort(Comparator.comparing(Protocol::value));
    } finally {
      protocolsLock.unlock();
    }
  }

  public List<Protocol> getProtocols(final WikiDefinition wiki) {
    protocolsLock.lock();
    try {
      return List.copyOf(protocols.getOrDefault(wiki, List.of()));
    } finally {
      protocolsLock.unlock();
    }
  }

  @Override
  public void logout(final ConnectedUser user) {
    protocolsLock.lock();
    try {
      protocols.clear();
    } finally {
      protocolsLock.unlock();
    }
  }
}
