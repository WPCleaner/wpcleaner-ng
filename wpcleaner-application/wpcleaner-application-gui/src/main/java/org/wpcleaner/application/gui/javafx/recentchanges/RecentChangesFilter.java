package org.wpcleaner.application.gui.javafx.recentchanges;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Objects;
import java.util.Set;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.api.api.query.list.recentchanges.RecentChange;
import org.wpcleaner.api.api.query.list.recentchanges.RecentChangesParameters;

public record RecentChangesFilter(
    String name,
    Set<Integer> namespace,
    @Nullable Severity severity,
    Set<String> tag,
    Set<RecentChangesParameters.Type> type) {

  public static final RecentChangesFilter ACCEPT_ALL =
      new RecentChangesFilter("Accept all", Set.of(), null, Set.of(), Set.of());

  public boolean matches(final RecentChange rc) {
    return matchesNamespace(rc) && matchesTag(rc) && matchesType(rc);
  }

  private boolean matchesNamespace(final RecentChange rc) {
    return namespace.isEmpty() || namespace.contains(rc.ns());
  }

  private boolean matchesTag(final RecentChange rc) {
    return tag.isEmpty() || rc.tags().stream().anyMatch(tag::contains);
  }

  private boolean matchesType(final RecentChange rc) {
    return type.isEmpty()
        || type.stream().map(type -> type.value).anyMatch(type -> Objects.equals(type, rc.type()));
  }
}
