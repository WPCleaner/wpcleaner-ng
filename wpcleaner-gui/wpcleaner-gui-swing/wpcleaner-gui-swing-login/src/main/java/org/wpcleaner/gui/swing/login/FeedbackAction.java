package org.wpcleaner.gui.swing.login;

/*
 * SPDX-FileCopyrightText: © 2024 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.awt.Component;
import javax.swing.JPopupMenu;
import org.wpcleaner.gui.swing.core.action.ComponentAction;
import org.wpcleaner.gui.swing.core.component.MenuItemService;

public record FeedbackAction(MenuItemService menuItemService) implements ComponentAction {

  @Override
  public void execute(final Component component) {
    final JPopupMenu menu = new JPopupMenu();
    menu.add(menuItemService.help());
    menu.add(menuItemService.reportBug());
    menu.add(menuItemService.requestFeature());
    menu.add(menuItemService.askQuestion());
    menu.show(component, 0, component.getHeight());
  }
}
