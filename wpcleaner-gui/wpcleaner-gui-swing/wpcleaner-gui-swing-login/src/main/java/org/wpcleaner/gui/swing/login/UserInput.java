package org.wpcleaner.gui.swing.login;

/*
 * SPDX-FileCopyrightText: © 2024 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import org.wpcleaner.gui.core.action.ActionService;
import org.wpcleaner.gui.swing.core.component.ComponentService;
import org.wpcleaner.lib.image.ImageCollection;
import org.wpcleaner.lib.image.ImageSize;

public class UserInput {

  final JComboBox<String> comboBox;
  final JLabel icon;
  final JLabel label;
  final JToolBar toolBar;

  UserInput(final ActionService actionService, final ComponentService componentService) {
    icon =
        componentService
            .labels()
            .builder("User", false)
            .withIcon(ImageCollection.USER, ImageSize.LABEL)
            .build();
    comboBox = new JComboBox<>();
    comboBox.setEditable(true);
    label =
        componentService
            .labels()
            .builder("Username", true)
            .withHorizontalAlignment(SwingConstants.TRAILING)
            .withComponent(comboBox)
            .build();
    final JButton addUser =
        componentService
            .buttons()
            .builder("Save user", false)
            .withIcon(ImageCollection.LIST_ADD, ImageSize.TOOLBAR)
            .withAction(actionService.notImplemented())
            .build();
    final JButton removeUser =
        componentService
            .buttons()
            .builder("Forget user", false)
            .withIcon(ImageCollection.LIST_REMOVE, ImageSize.TOOLBAR)
            .withAction(actionService.notImplemented())
            .build();
    toolBar =
        componentService
            .toolBars()
            .builder()
            .withComponent(addUser)
            .withComponent(removeUser)
            .build();
  }
}
