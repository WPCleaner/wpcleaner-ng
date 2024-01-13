package org.wpcleaner.gui.swing.core.action;

/*
 * SPDX-FileCopyrightText: © 2024 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import javax.swing.JOptionPane;
import org.springframework.stereotype.Component;
import org.wpcleaner.gui.core.action.NotImplementedAction;

@Component
public class NotImplementedSwingAction implements NotImplementedAction {

  @Override
  public void execute() {
    JOptionPane.showMessageDialog(
        null,
        "This feature is not implemented yet. Try again later!",
        "Not implemented",
        JOptionPane.INFORMATION_MESSAGE);
  }
}
