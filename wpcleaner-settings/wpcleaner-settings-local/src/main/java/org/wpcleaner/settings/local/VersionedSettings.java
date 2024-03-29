package org.wpcleaner.settings.local;

/*
 * SPDX-FileCopyrightText: © 2024 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

public interface VersionedSettings {

  int version();

  int lastVersion();
}
