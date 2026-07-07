package org.wpcleaner.api.repository.namespace;

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
public class NamespaceRepository {

  private final List<Namespace> namespaces = new ArrayList<>();

  public void addNamespace(final Namespace namespace) {
    if (namespaces.stream().map(Namespace::id).anyMatch(id -> Objects.equals(id, namespace.id()))) {
      return;
    }
    namespaces.add(namespace);
    namespaces.sort(Comparator.comparingInt(Namespace::id));
  }
}
