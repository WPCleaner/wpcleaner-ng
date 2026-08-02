package org.wpcleaner.api.api.query.list.recentchanges;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RecentChangeComparatorTest {

  @DisplayName("RecentChangeComparator sorts by timestamp and rcid descending")
  @Test
  void testRecentChangeComparatorSorting() {
    final RecentChange change1 =
        new RecentChange(
            false,
            false,
            "comment",
            null,
            null,
            null,
            null,
            false,
            100,
            0,
            50,
            1000,
            12,
            "parsed comment",
            false,
            1234, // rcId = 1234
            false,
            5678,
            "hash",
            List.of(),
            Instant.parse("2026-06-18T12:00:00Z"),
            "Title 1",
            "edit",
            "User 1",
            101);

    final RecentChange change2 =
        new RecentChange(
            false,
            false,
            "comment",
            null,
            null,
            null,
            null,
            false,
            100,
            0,
            50,
            1000,
            12,
            "parsed comment",
            false,
            1235, // rcId = 1235 (higher)
            false,
            5678,
            "hash",
            List.of(),
            Instant.parse("2026-06-18T12:00:00Z"), // same timestamp
            "Title 2",
            "edit",
            "User 2",
            102);

    final RecentChange change3 =
        new RecentChange(
            false,
            false,
            "comment",
            null,
            null,
            null,
            null,
            false,
            100,
            0,
            50,
            1000,
            12,
            "parsed comment",
            false,
            1230, // rcId = 1230
            false,
            5678,
            "hash",
            List.of(),
            Instant.parse("2026-06-18T13:00:00Z"), // newer timestamp
            "Title 3",
            "edit",
            "User 3",
            103);

    final List<RecentChange> list = new ArrayList<>();
    list.add(change1);
    list.add(change2);
    list.add(change3);

    list.sort(RecentChangeComparator.INSTANCE);

    // Order should be: newer timestamp first (change3), then higher rcId (change2), then change1
    Assertions.assertThat(list).containsExactly(change3, change2, change1);
  }
}
