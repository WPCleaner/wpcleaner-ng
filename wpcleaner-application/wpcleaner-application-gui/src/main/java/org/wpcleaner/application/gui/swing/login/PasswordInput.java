package org.wpcleaner.application.gui.swing.login;

/*
 * SPDX-FileCopyrightText: © 2024 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import javax.swing.AbstractButton;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import org.wpcleaner.application.base.utils.url.UrlService;
import org.wpcleaner.application.gui.core.action.ActionService;
import org.wpcleaner.application.gui.swing.core.SwingCoreServices;
import org.wpcleaner.application.gui.swing.core.component.ComponentService;
import org.wpcleaner.lib.image.ImageCollection;
import org.wpcleaner.lib.image.ImageSize;

public class PasswordInput {

  final JPasswordField field;
  final JLabel icon;
  final JLabel label;
  final JToolBar toolBar;

  PasswordInput(final SwingCoreServices swingCoreServices) {
    final ActionService actionService = swingCoreServices.action().action();
    final ComponentService componentService = swingCoreServices.component();
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
    final AbstractButton specialBotPasswords =
        componentService
            .buttons()
            .builder("Bot passwords", false)
            .withIcon(ImageCollection.HELP, ImageSize.TOOLBAR)
            .withAction(actionService.openUrl(UrlService.BOT_PASSWORDS))
            .build();
    final AbstractButton addPassword =
        componentService
            .buttons()
            .builder("Save user and password", false)
            .withIcon(ImageCollection.LIST_ADD, ImageSize.TOOLBAR)
            .withAction(actionService.notImplemented())
            .build();
    final AbstractButton removePassword =
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
            .withComponent(specialBotPasswords)
            .withComponent(addPassword)
            .withComponent(removePassword)
            .build();
  }

  public char[] getPassword() {
    return field.getPassword();
  }

  public void setPassword(final String password) {
    field.setText(password);
  }
}
