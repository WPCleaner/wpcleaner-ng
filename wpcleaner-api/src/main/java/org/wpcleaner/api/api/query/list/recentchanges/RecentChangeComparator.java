package org.wpcleaner.api.api.query.list.recentchanges;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Comparator;

public final class RecentChangeComparator {

  public static final Comparator<RecentChange> INSTANCE =
      Comparator.comparing(RecentChange::timestamp, Comparator.nullsLast(Comparator.reverseOrder()))
          .thenComparing(RecentChange::rcId, Comparator.nullsLast(Comparator.reverseOrder()));
}
