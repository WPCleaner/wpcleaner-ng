package org.wpcleaner.gui.swing.core.action;

/*
 * SPDX-FileCopyrightText: © 2024 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ComponentActionListener implements ActionListener {

  private final ComponentAction action;
  private final Component component;

  public ComponentActionListener(final ComponentAction action, final Component component) {
    this.action = action;
    this.component = component;
  }

  @Override
  public void actionPerformed(final ActionEvent e) {
    action.execute(component);
  }
}
