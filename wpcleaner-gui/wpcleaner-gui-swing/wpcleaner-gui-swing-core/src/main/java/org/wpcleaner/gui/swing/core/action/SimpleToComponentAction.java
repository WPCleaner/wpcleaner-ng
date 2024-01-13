package org.wpcleaner.gui.swing.core.action;

/*
 * SPDX-FileCopyrightText: © 2024 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.awt.Component;
import org.wpcleaner.gui.core.action.SimpleAction;

public record SimpleToComponentAction(SimpleAction action) implements ComponentAction {

  @Override
  public void execute(final Component component) {
    action.execute();
  }
}
