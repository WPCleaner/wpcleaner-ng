package org.wpcleaner.application.gui.javafx.recentchanges;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.api.api.query.list.recentchanges.RecentChange;
import org.wpcleaner.api.api.query.list.recentchanges.RecentChangesParameters;
import org.wpcleaner.api.utils.GT;

public record RecentChangesOptions(
    String name,
    Set<Integer> namespace,
    Set<RecentChangesParameters.Show> show,
    @Nullable String tag,
    Set<RecentChangesParameters.Type> type,
    boolean topOnly,
    List<RecentChangesFilter> filters) {

  public static final RecentChangesOptions DEFAULT_OPTIONS =
      new RecentChangesOptions(
          GT._T("Default options"),
          Set.of(),
          Set.of(RecentChangesParameters.Show.NOT_BOT),
          null,
          Set.of(RecentChangesParameters.Type.EDIT, RecentChangesParameters.Type.NEW),
          false,
          List.of());

  public Optional<RecentChangesFilter> matchesFilters(final RecentChange rc) {
    if (filters.isEmpty()) {
      return Optional.of(RecentChangesFilter.ACCEPT_ALL);
    }
    return filters.stream().filter(filter -> filter.matches(rc)).findFirst();
  }
}
