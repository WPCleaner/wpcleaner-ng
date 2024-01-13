package org.wpcleaner.gui.core.action;

/*
 * SPDX-FileCopyrightText: © 2024 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.springframework.stereotype.Service;
import org.wpcleaner.gui.core.desktop.DesktopService;

@Service
public class ActionService {

  private final DesktopService desktopService;
  private final NotImplementedAction notImplementedAction;

  public ActionService(
      final DesktopService desktopService, final NotImplementedAction notImplementedAction) {
    this.desktopService = desktopService;
    this.notImplementedAction = notImplementedAction;
  }

  public SimpleAction openUrl(final String url) {
    return new OpenUrlAction(desktopService, url);
  }

  public SimpleAction notImplemented() {
    return notImplementedAction;
  }
}
