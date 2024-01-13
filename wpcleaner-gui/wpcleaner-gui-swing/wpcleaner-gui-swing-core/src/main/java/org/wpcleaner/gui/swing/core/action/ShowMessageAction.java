package org.wpcleaner.gui.swing.core.action;

/*
 * SPDX-FileCopyrightText: © 2024 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.awt.Component;
import javax.swing.JOptionPane;

public record ShowMessageAction(String message, String title, int messageType)
    implements ComponentAction {

  @Override
  public void execute(final Component component) {
    JOptionPane.showMessageDialog(component, message, title, messageType);
  }
}
