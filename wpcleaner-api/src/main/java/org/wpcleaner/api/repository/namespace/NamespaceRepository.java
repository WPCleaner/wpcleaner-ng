package org.wpcleaner.api.repository.namespace;

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
public class NamespaceRepository implements LogoutHook {

  private final Map<WikiDefinition, List<Namespace>> namespaces = new HashMap<>();
  private final Lock namespacesLock = new ReentrantLock();

  public void addNamespace(final WikiDefinition wiki, final Namespace namespace) {
    namespacesLock.lock();
    try {
      final List<Namespace> wikiNamespaces =
          namespaces.computeIfAbsent(wiki, _ -> new ArrayList<>());
      if (wikiNamespaces.stream()
          .map(Namespace::id)
          .anyMatch(id -> Objects.equals(id, namespace.id()))) {
        return;
      }
      wikiNamespaces.add(namespace);
      wikiNamespaces.sort(Comparator.comparingInt(Namespace::id));
    } finally {
      namespacesLock.unlock();
    }
  }

  public List<Namespace> getNamespaces(final WikiDefinition wiki) {
    namespacesLock.lock();
    try {
      return List.copyOf(namespaces.getOrDefault(wiki, List.of()));
    } finally {
      namespacesLock.unlock();
    }
  }

  @Override
  public void logout(final ConnectedUser user) {
    namespacesLock.lock();
    try {
      namespaces.clear();
    } finally {
      namespacesLock.unlock();
    }
  }
}
