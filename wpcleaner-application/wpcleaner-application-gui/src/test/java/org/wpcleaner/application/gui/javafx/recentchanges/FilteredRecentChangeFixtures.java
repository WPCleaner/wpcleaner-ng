package org.wpcleaner.application.gui.javafx.recentchanges;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.net.URI;
import java.util.List;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.application.gui.javafx.recentchanges.options.RecentChangesFilter;

public final class FilteredRecentChangeFixtures {

  private FilteredRecentChangeFixtures() {}

  public static FilteredRecentChange create(@Nullable final Integer ns, final String title) {
    return new FilteredRecentChange(
        "comment",
        0,
        URI.create("https://diff"),
        RecentChangesFilter.ACCEPT_ALL,
        ns,
        URI.create("https://page"),
        1,
        2,
        3,
        List.of(),
        null,
        title,
        "User");
  }
}
