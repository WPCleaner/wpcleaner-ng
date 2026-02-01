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

  static <T extends VersionedSettings> String name(final Class<T> clazz) {
    return StringUtils.removeSuffix(clazz.getSimpleName(), "Settings").toLowerCase(Locale.ROOT);
  }
}
