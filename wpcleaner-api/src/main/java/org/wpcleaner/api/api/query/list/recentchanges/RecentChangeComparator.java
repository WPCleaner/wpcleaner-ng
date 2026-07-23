package org.wpcleaner.api.api.query.list.recentchanges;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Comparator;
import java.util.Objects;

public record RecentChangeComparator() implements Comparator<RecentChange> {

  public static final RecentChangeComparator INSTANCE = new RecentChangeComparator();

  @Override
  public int compare(final RecentChange rc1, final RecentChange rc2) {
    if (!Objects.equals(rc1.timestamp(), rc2.timestamp())) {
      if (rc1.timestamp() == null) {
        return -1;
      }
      if (rc2.timestamp() == null) {
        return 1;
      }
      return rc2.timestamp().compareTo(rc1.timestamp());
    }
    if (!Objects.equals(rc1.rcid(), rc2.rcid())) {
      if (rc1.rcid() == null) {
        return -1;
      }
      if (rc2.rcid() == null) {
        return 1;
      }
      return rc2.rcid().compareTo(rc1.rcid());
    }
    return 0;
  }
}
