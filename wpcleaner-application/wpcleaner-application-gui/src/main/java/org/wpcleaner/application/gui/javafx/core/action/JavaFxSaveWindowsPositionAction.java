package org.wpcleaner.application.gui.javafx.core.action;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import org.springframework.stereotype.Service;
import org.wpcleaner.application.gui.core.action.SaveWindowsPositionAction;
import org.wpcleaner.application.gui.javafx.JavaFxWindowService;

@Service
public record JavaFxSaveWindowsPositionAction(JavaFxWindowService windowService)
    implements SaveWindowsPositionAction {

  @Override
  public void execute() {
    windowService.saveAllWindowsPosition();
  }
}
