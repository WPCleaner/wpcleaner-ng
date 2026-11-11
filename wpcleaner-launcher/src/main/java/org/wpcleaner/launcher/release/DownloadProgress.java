package org.wpcleaner.launcher.release;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

@FunctionalInterface
public interface DownloadProgress {

  void downloaded(long current, long total);
}
