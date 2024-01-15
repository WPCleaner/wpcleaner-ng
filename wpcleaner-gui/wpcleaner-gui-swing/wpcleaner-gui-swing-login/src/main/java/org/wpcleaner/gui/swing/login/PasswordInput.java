package org.wpcleaner.gui.swing.login;

/*
 * SPDX-FileCopyrightText: © 2024 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import org.wpcleaner.gui.core.action.ActionService;
import org.wpcleaner.gui.swing.core.component.ComponentService;
import org.wpcleaner.lib.image.ImageCollection;
import org.wpcleaner.lib.image.ImageSize;

public class PasswordInput {

  final JPasswordField field;
  final JLabel icon;
  final JLabel label;
  final JToolBar toolBar;

  PasswordInput(final ActionService actionService, final ComponentService componentService) {
    icon =
        componentService
            .labels()
            .builder("Password", false)
            .withIcon(ImageCollection.PASSWORD, ImageSize.LABEL)
            .build();
    field = new JPasswordField(30);
    label =
        componentService
            .labels()
            .builder("Password", true)
            .withHorizontalAlignment(SwingConstants.TRAILING)
            .withComponent(field)
            .build();
    final JButton addPassword =
        componentService
            .buttons()
            .builder("Save user and password", false)
            .withIcon(ImageCollection.LIST_ADD, ImageSize.TOOLBAR)
            .withAction(actionService.notImplemented())
            .build();
    final JButton removePassword =
        componentService
            .buttons()
            .builder("Forget password", false)
            .withIcon(ImageCollection.LIST_REMOVE, ImageSize.TOOLBAR)
            .withAction(actionService.notImplemented())
            .build();
    toolBar =
        componentService
            .toolBars()
            .builder()
            .withComponent(addPassword)
            .withComponent(removePassword)
            .build();
  }
}
