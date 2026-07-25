package org.wpcleaner.application.gui.swing.core.window;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.springframework.stereotype.Service;

@Service
public record SwingSaveWindowsPositionAction(WindowService windowService)
    implements org.wpcleaner.application.gui.core.action.SaveWindowsPositionAction {

  @Override
  public void execute() {
    windowService.saveAllWindowsPosition();
  }
}
