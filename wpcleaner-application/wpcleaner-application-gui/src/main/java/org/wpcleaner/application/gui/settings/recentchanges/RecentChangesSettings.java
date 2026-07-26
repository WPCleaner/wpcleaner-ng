package org.wpcleaner.application.gui.settings.recentchanges;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.api.settings.VersionedSettings;
import org.wpcleaner.application.gui.javafx.recentchanges.RecentChangesOptions;

public record RecentChangesSettings(
    int version, List<RecentChangesOptions> options, @Nullable String selectedOption)
    implements VersionedSettings {

  public static final int LAST_VERSION = 1;

  public RecentChangesSettings() {
    this(0, List.of(), null);
  }

  @Override
  public int lastVersion() {
    return LAST_VERSION;
  }
}
