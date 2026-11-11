package org.wpcleaner.launcher.display;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

public interface LauncherDisplay {

  void progress(String status, long current, long total);

  void closeProgress();

  void inform(String message);

  void error(String message);

  boolean confirm(String message);
}
