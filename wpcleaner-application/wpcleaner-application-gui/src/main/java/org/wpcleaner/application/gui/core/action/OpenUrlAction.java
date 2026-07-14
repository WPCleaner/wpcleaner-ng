package org.wpcleaner.application.gui.core.action;

/*
 * SPDX-FileCopyrightText: © 2024 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.wpcleaner.application.gui.core.desktop.DesktopService;

public record OpenUrlAction(DesktopService desktopService, String url) implements Runnable {

  @Override
  public void run() {
    desktopService.browse(url);
  }
}
