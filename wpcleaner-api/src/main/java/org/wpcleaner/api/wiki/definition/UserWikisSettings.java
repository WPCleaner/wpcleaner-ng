package org.wpcleaner.api.wiki.definition;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import org.wpcleaner.api.settings.VersionedSettings;

public record UserWikisSettings(int version, List<WikiDefinition> wikis)
    implements VersionedSettings {

  public static final int LAST_VERSION = 1;

  public UserWikisSettings() {
    this(0, List.of());
  }

  @Override
  public int lastVersion() {
    return LAST_VERSION;
  }
}
