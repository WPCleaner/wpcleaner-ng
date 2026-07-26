package org.wpcleaner.application.gui.javafx.recentchanges;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.Set;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.wpcleaner.api.api.query.list.recentchanges.RecentChange;

class RecentChangesFilterTest {

  @DisplayName("matchesSubPages returns true always when subPages is BOTH")
  @Test
  void testMatchesSubPagesBoth() {
    final RecentChangesFilter filter =
        new RecentChangesFilter(
            "Test", Set.of(), null, Set.of(), Set.of(), RecentChangesFilter.SubPages.BOTH);

    final RecentChange rcNoSlash = createRecentChange("MainPage");
    final RecentChange rcWithSlash = createRecentChange("MainPage/SubPage");
    final RecentChange rcNullTitle = createRecentChange(null);

    Assertions.assertThat(filter.matchesSubPages(rcNoSlash)).isTrue();
    Assertions.assertThat(filter.matchesSubPages(rcWithSlash)).isTrue();
    Assertions.assertThat(filter.matchesSubPages(rcNullTitle)).isTrue();
  }

  @DisplayName(
      "matchesSubPages returns true only if title does not contain slash when subPages is TOP_PAGES")
  @Test
  void testMatchesSubPagesTopPages() {
    final RecentChangesFilter filter =
        new RecentChangesFilter(
            "Test", Set.of(), null, Set.of(), Set.of(), RecentChangesFilter.SubPages.TOP_PAGES);

    final RecentChange rcNoSlash = createRecentChange("MainPage");
    final RecentChange rcWithSlash = createRecentChange("MainPage/SubPage");
    final RecentChange rcNullTitle = createRecentChange(null);

    Assertions.assertThat(filter.matchesSubPages(rcNoSlash)).isTrue();
    Assertions.assertThat(filter.matchesSubPages(rcWithSlash)).isFalse();
    Assertions.assertThat(filter.matchesSubPages(rcNullTitle)).isTrue();
  }

  @DisplayName(
      "matchesSubPages returns true only if title contains slash when subPages is SUB_PAGES")
  @Test
  void testMatchesSubPagesSubPages() {
    final RecentChangesFilter filter =
        new RecentChangesFilter(
            "Test", Set.of(), null, Set.of(), Set.of(), RecentChangesFilter.SubPages.SUB_PAGES);

    final RecentChange rcNoSlash = createRecentChange("MainPage");
    final RecentChange rcWithSlash = createRecentChange("MainPage/SubPage");
    final RecentChange rcNullTitle = createRecentChange(null);

    Assertions.assertThat(filter.matchesSubPages(rcNoSlash)).isFalse();
    Assertions.assertThat(filter.matchesSubPages(rcWithSlash)).isTrue();
    Assertions.assertThat(filter.matchesSubPages(rcNullTitle)).isFalse();
  }

  private RecentChange createRecentChange(final String title) {
    return new RecentChange(
        null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
        null, null, null, null, List.of(), null, title, null, null, null);
  }
}
