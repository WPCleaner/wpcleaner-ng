package org.wpcleaner.application.gui.swing.login;

/*
 * SPDX-FileCopyrightText: © 2024 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.awt.EventQueue;
import org.springframework.stereotype.Service;
import org.wpcleaner.application.gui.core.factory.LoginWindowFactory;

@Service
public class SwingLoginWindowFactory implements LoginWindowFactory {

  private final SwingLoginWindowServices services;

  public SwingLoginWindowFactory(final SwingLoginWindowServices services) {
    this.services = services;
  }

  @Override
  public void displayLoginWindow() {
    EventQueue.invokeLater(() -> SwingLoginWindow.create(services));
  }
}
