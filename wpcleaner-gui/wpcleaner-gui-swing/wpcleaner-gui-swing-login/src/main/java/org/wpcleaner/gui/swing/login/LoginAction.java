package org.wpcleaner.gui.swing.login;

/*
 * SPDX-FileCopyrightText: © 2024 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.awt.Component;
import java.util.function.Supplier;
import javax.swing.JOptionPane;
import org.springframework.util.StringUtils;
import org.wpcleaner.gui.core.action.SimpleAction;
import org.wpcleaner.gui.swing.core.action.ComponentAction;

public record LoginAction(
    SimpleAction action, Supplier<String> userSupplier, Supplier<char[]> passwordSupplier)
    implements ComponentAction {

  @Override
  public void execute(final Component component) {
    if (!StringUtils.hasText(userSupplier().get())) {
      JOptionPane.showMessageDialog(
          null,
          "You must input your username before login!",
          "Missing username",
          JOptionPane.WARNING_MESSAGE);
      return;
    }

    final char[] password = passwordSupplier().get();
    if (password == null || password.length == 0) {
      JOptionPane.showMessageDialog(
          null,
          "You must input your password before login!",
          "Missing password",
          JOptionPane.WARNING_MESSAGE);
      return;
    }

    action.execute();
  }
}
