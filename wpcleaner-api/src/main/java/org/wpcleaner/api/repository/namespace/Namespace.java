package org.wpcleaner.api.repository.namespace;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Optional;
import org.wpcleaner.api.repository.CaseType;

public record Namespace(
    int id, String canonical, String name, List<String> aliases, CaseType caseType) {

  public static final Namespace MAIN =
      new Namespace(CommonNamespaces.MAIN.id, "Main", "", List.of(), CaseType.FIRST_LETTER);

  public boolean isPossibleName(final String possible) {
    return caseType.areEqual(possible, canonical)
        || caseType.areEqual(possible, name)
        || aliases.stream().anyMatch(alias -> caseType.areEqual(possible, alias));
  }

  public static Optional<Namespace> findNamespace(final List<Namespace> namespaces, final int id) {
    return namespaces.stream().filter(namespace -> namespace.id == id).findFirst();
  }
}
