package org.wpcleaner.api.settings;

/*
 * SPDX-FileCopyrightText: © 2024 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Locale;
import org.wpcleaner.api.utils.StringUtils;

public interface VersionedSettings extends SettingsElement {

  int version();

  int lastVersion();

  default String name() {
    return StringUtils.removeSuffix(getClass().getSimpleName(), "Settings")
        .toLowerCase(Locale.ROOT);
  }
}
