package org.wpcleaner.application.gui.swing.recentchanges;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Set;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.api.api.query.list.recentchanges.RecentChangesParameters;

public record RecentChangesOptions(
    String name,
    Set<Integer> namespace,
    Set<RecentChangesParameters.Show> show,
    @Nullable String tag,
    Set<RecentChangesParameters.Type> type,
    boolean topOnly) {}
