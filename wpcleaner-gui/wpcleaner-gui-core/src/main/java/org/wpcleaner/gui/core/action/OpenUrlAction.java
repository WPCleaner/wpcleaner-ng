package org.wpcleaner.gui.core.action;

/*
 * SPDX-FileCopyrightText: © 2024 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.wpcleaner.gui.core.desktop.DesktopService;

public record OpenUrlAction(DesktopService desktopService, String url) implements SimpleAction {

  @Override
  public void execute() {
    desktopService.browse(url);
  }
}
