package org.wpcleaner.application.gui.javafx.recentchanges.options;

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
            "Test",
            Set.of(),
            null,
            Set.of(),
            Set.of(),
            Set.of(),
            RecentChangesFilter.SubPages.BOTH);

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
            "Test",
            Set.of(),
            null,
            Set.of(),
            Set.of(),
            Set.of(),
            RecentChangesFilter.SubPages.TOP_PAGES);

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
            "Test",
            Set.of(),
            null,
            Set.of(),
            Set.of(),
            Set.of(),
            RecentChangesFilter.SubPages.SUB_PAGES);

    final RecentChange rcNoSlash = createRecentChange("MainPage");
    final RecentChange rcWithSlash = createRecentChange("MainPage/SubPage");
    final RecentChange rcNullTitle = createRecentChange(null);

    Assertions.assertThat(filter.matchesSubPages(rcNoSlash)).isFalse();
    Assertions.assertThat(filter.matchesSubPages(rcWithSlash)).isTrue();
    Assertions.assertThat(filter.matchesSubPages(rcNullTitle)).isFalse();
  }

  @DisplayName(
      "matchesType with EDIT_NEW accepts edit changes only if a corresponding new change exists")
  @Test
  void testMatchesEditNew() {
    final RecentChangesFilter filter =
        new RecentChangesFilter(
            "Test",
            Set.of(),
            null,
            Set.of(),
            Set.of(RecentChangesFilter.Type.EDIT_NEW),
            Set.of(),
            RecentChangesFilter.SubPages.BOTH);

    final RecentChange newPageRc = createRecentChange("NewPage", "new", 42);
    final RecentChange editPageRcWithTitle = createRecentChange("NewPage", "edit", null);
    final RecentChange editPageRcWithPageId = createRecentChange("AnotherPage", "edit", 42);
    final RecentChange editUnrelatedRc = createRecentChange("OtherPage", "edit", 99);

    final List<RecentChange> recentChanges =
        List.of(newPageRc, editPageRcWithTitle, editPageRcWithPageId, editUnrelatedRc);

    Assertions.assertThat(filter.matches(editPageRcWithTitle, recentChanges))
        .isEqualTo(RecentChangesFilter.Result.MATCH);
    Assertions.assertThat(filter.matches(editPageRcWithPageId, recentChanges))
        .isEqualTo(RecentChangesFilter.Result.MATCH);
    Assertions.assertThat(filter.matches(editUnrelatedRc, recentChanges))
        .isEqualTo(RecentChangesFilter.Result.NO_MATCH);
    Assertions.assertThat(filter.matches(newPageRc, recentChanges))
        .isEqualTo(RecentChangesFilter.Result.NO_MATCH);
  }

  @DisplayName("matches returns MATCH when user set is empty")
  @Test
  void testMatchesUserWhenEmpty() {
    final RecentChangesFilter filter =
        new RecentChangesFilter(
            "Test",
            Set.of(),
            null,
            Set.of(),
            Set.of(),
            Set.of(),
            RecentChangesFilter.SubPages.BOTH);

    final RecentChange rcWithUser = createRecentChangeWithUser("Alice");
    final RecentChange rcNullUser = createRecentChangeWithUser(null);

    Assertions.assertThat(filter.matches(rcWithUser, List.of()))
        .isEqualTo(RecentChangesFilter.Result.MATCH);
    Assertions.assertThat(filter.matches(rcNullUser, List.of()))
        .isEqualTo(RecentChangesFilter.Result.MATCH);
  }

  @DisplayName("matches returns MATCH only when recent change user is in the non-empty user set")
  @Test
  void testMatchesUserWhenNonEmpty() {
    final RecentChangesFilter filter =
        new RecentChangesFilter(
            "Test",
            Set.of(),
            null,
            Set.of(),
            Set.of(),
            Set.of("Alice", "Bob"),
            RecentChangesFilter.SubPages.BOTH);

    final RecentChange rcAlice = createRecentChangeWithUser("Alice");
    final RecentChange rcBob = createRecentChangeWithUser("Bob");
    final RecentChange rcCharlie = createRecentChangeWithUser("Charlie");
    final RecentChange rcNullUser = createRecentChangeWithUser(null);

    Assertions.assertThat(filter.matches(rcAlice, List.of()))
        .isEqualTo(RecentChangesFilter.Result.MATCH);
    Assertions.assertThat(filter.matches(rcBob, List.of()))
        .isEqualTo(RecentChangesFilter.Result.MATCH);
    Assertions.assertThat(filter.matches(rcCharlie, List.of()))
        .isEqualTo(RecentChangesFilter.Result.NO_MATCH);
    Assertions.assertThat(filter.matches(rcNullUser, List.of()))
        .isEqualTo(RecentChangesFilter.Result.NO_MATCH);
  }

  @DisplayName("matches returns REJECTED when reject is true and internal criteria match")
  @Test
  void testMatchesRejectWhenMatching() {
    final RecentChangesFilter filter =
        new RecentChangesFilter(
            "RejectFilter",
            Set.of(),
            true,
            null,
            Set.of(),
            Set.of(),
            Set.of("Spammer"),
            RecentChangesFilter.SubPages.BOTH);

    final RecentChange matchingRc = createRecentChangeWithUser("Spammer");
    final RecentChange nonMatchingRc = createRecentChangeWithUser("LegitUser");

    Assertions.assertThat(filter.matches(matchingRc, List.of()))
        .isEqualTo(RecentChangesFilter.Result.REJECTED);
    Assertions.assertThat(filter.matches(nonMatchingRc, List.of()))
        .isEqualTo(RecentChangesFilter.Result.NO_MATCH);
  }

  private RecentChange createRecentChangeWithUser(final String user) {
    return new RecentChange(
        false,
        false,
        null,
        null,
        null,
        null,
        null,
        false,
        null,
        null,
        null,
        null,
        null,
        null,
        false,
        null,
        false,
        null,
        null,
        List.of(),
        null,
        "MainPage",
        null,
        user,
        null);
  }

  private RecentChange createRecentChange(
      final String title, final String type, final Integer pageId) {
    return new RecentChange(
        false, false, null, null, null, null, null, false, null, null, null, null, pageId, null,
        false, null, false, null, null, List.of(), null, title, type, null, null);
  }

  private RecentChange createRecentChange(final String title) {
    return createRecentChange(title, null, null);
  }
}
