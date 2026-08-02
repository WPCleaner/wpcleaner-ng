package org.wpcleaner.application.gui.javafx.recentchanges;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Comparator;

public final class FilteredRecentChangeComparator {

  public static final Comparator<FilteredRecentChange> INSTANCE =
      Comparator.comparing(
              FilteredRecentChange::timestamp, Comparator.nullsLast(Comparator.reverseOrder()))
          .thenComparing(
              FilteredRecentChange::rcId, Comparator.nullsLast(Comparator.reverseOrder()));
}
