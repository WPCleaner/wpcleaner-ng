package org.wpcleaner.api.settings;

/*
 * SPDX-FileCopyrightText: © 2024 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import jakarta.annotation.Nullable;

public record GeneralSettings(int version, @Nullable String preferredWiki)
    implements VersionedSettings {

  public static final int LAST_VERSION = 1;

  public GeneralSettings() {
    this(0, null);
  }

  @Override
  public int lastVersion() {
    return LAST_VERSION;
  }
}
