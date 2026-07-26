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
import org.wpcleaner.api.utils.GT;

public record RecentChangesFilter(
    String name,
    Set<Integer> namespace,
    @Nullable Severity severity,
    Set<String> tag,
    Set<RecentChangesParameters.Type> type,
    SubPages subPages) {

  public static final RecentChangesFilter ACCEPT_ALL =
      new RecentChangesFilter(
          GT._T("Accept all"), Set.of(), null, Set.of(), Set.of(), SubPages.BOTH);

  public enum SubPages {
    BOTH,
    TOP_PAGES,
    SUB_PAGES
  }

  public RecentChangesFilter(
      final String name,
      final Set<Integer> namespace,
      @Nullable final Severity severity,
      final Set<String> tag,
      final Set<RecentChangesParameters.Type> type,
      @Nullable final SubPages subPages) {
    this.name = name;
    this.namespace = namespace;
    this.severity = severity;
    this.tag = tag;
    this.type = type;
    this.subPages = Objects.requireNonNullElse(subPages, SubPages.BOTH);
  }

  public boolean matches(final RecentChange rc) {
    return matchesNamespace(rc) && matchesTag(rc) && matchesType(rc) && matchesSubPages(rc);
  }

  public boolean matchesSubPages(final RecentChange rc) {
    return switch (subPages) {
      case BOTH -> true;
      case TOP_PAGES -> rc.title() == null || !rc.title().contains("/");
      case SUB_PAGES -> rc.title() != null && rc.title().contains("/");
    };
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
