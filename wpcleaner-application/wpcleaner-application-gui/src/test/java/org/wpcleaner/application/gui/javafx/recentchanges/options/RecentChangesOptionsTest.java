package org.wpcleaner.application.gui.javafx.recentchanges.options;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.wpcleaner.api.api.query.list.recentchanges.RecentChange;
import org.wpcleaner.api.api.query.list.recentchanges.RecentChangesParameters;

class RecentChangesOptionsTest {

  @DisplayName("matchesFilters returns ACCEPT_ALL when filter list is empty")
  @Test
  void testMatchesFiltersEmpty() {
    final RecentChangesOptions options = createOptions(List.of());
    final RecentChange rc = createRecentChange("MainPage", "edit", "Alice");

    final Optional<RecentChangesFilter> result = options.matchesFilters(rc, List.of());

    Assertions.assertThat(result).contains(RecentChangesFilter.ACCEPT_ALL);
  }

  @DisplayName("matchesFilters returns empty when all filters return NO_MATCH")
  @Test
  void testMatchesFiltersAllNoMatch() {
    final RecentChangesFilter filter1 = createFilter("Filter1", false, "Bob");
    final RecentChangesFilter filter2 = createFilter("Filter2", false, "Charlie");
    final RecentChangesOptions options = createOptions(List.of(filter1, filter2));
    final RecentChange rc = createRecentChange("MainPage", "edit", "Alice");

    final Optional<RecentChangesFilter> result = options.matchesFilters(rc, List.of());

    Assertions.assertThat(result).isEmpty();
  }

  @DisplayName("matchesFilters returns first matching filter when it matches")
  @Test
  void testMatchesFiltersMatch() {
    final RecentChangesFilter filter1 = createFilter("Filter1", false, "Bob");
    final RecentChangesFilter filter2 = createFilter("Filter2", false, "Alice");
    final RecentChangesFilter filter3 = createFilter("Filter3", false, "Alice");
    final RecentChangesOptions options = createOptions(List.of(filter1, filter2, filter3));
    final RecentChange rc = createRecentChange("MainPage", "edit", "Alice");

    final Optional<RecentChangesFilter> result = options.matchesFilters(rc, List.of());

    Assertions.assertThat(result).contains(filter2);
  }

  @DisplayName("matchesFilters returns empty when first matching filter is rejected")
  @Test
  void testMatchesFiltersRejected() {
    final RecentChangesFilter filter1 = createFilter("Filter1", false, "Bob");
    final RecentChangesFilter filter2 = createFilter("Filter2", true, "Alice");
    final RecentChangesFilter filter3 = createFilter("Filter3", false, "Alice");
    final RecentChangesOptions options = createOptions(List.of(filter1, filter2, filter3));
    final RecentChange rc = createRecentChange("MainPage", "edit", "Alice");

    final Optional<RecentChangesFilter> result = options.matchesFilters(rc, List.of());

    Assertions.assertThat(result).isEmpty();
  }

  @DisplayName("matchesFilters skips NO_MATCH filters until reaching MATCH or REJECTED")
  @Test
  void testMatchesFiltersSkipsNoMatchBeforeMatch() {
    final RecentChangesFilter filter1 = createFilter("Filter1", true, "Bob");
    final RecentChangesFilter filter2 = createFilter("Filter2", false, "Alice");
    final RecentChangesOptions options = createOptions(List.of(filter1, filter2));
    final RecentChange rc = createRecentChange("MainPage", "edit", "Alice");

    final Optional<RecentChangesFilter> result = options.matchesFilters(rc, List.of());

    Assertions.assertThat(result).contains(filter2);
  }

  private static RecentChangesOptions createOptions(final List<RecentChangesFilter> filters) {
    return new RecentChangesOptions(
        "TestOptions",
        Set.of(),
        Set.of(RecentChangesParameters.Show.NOT_BOT),
        null,
        Set.of(RecentChangesParameters.Type.EDIT),
        false,
        filters);
  }

  private static RecentChangesFilter createFilter(
      final String name, final boolean reject, final String user) {
    return new RecentChangesFilter(
        name,
        Set.of(),
        reject,
        null,
        Set.of(),
        Set.of(),
        Set.of(user),
        RecentChangesFilter.SubPages.BOTH);
  }

  private static RecentChange createRecentChange(
      final String title, final String type, final String user) {
    return new RecentChange(
        false, false, null, null, null, null, null, false, null, null, null, null, null, null,
        false, null, false, null, null, List.of(), null, title, type, user, null);
  }
}
