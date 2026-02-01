package org.wpcleaner.application.gui.swing.core.action;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.awt.Component;
import javax.swing.JPopupMenu;
import org.springframework.stereotype.Service;
import org.wpcleaner.application.gui.swing.core.component.MenuItemService;

@Service
public record FeedbackAction(MenuItemService menuItemService) {

  public void execute(final Component component) {
    final JPopupMenu menu = new JPopupMenu();
    menu.add(menuItemService.help());
    menu.add(menuItemService.reportBug());
    menu.add(menuItemService.requestFeature());
    menu.add(menuItemService.askQuestion());
    menu.show(component, 0, component.getHeight());
  }
}
