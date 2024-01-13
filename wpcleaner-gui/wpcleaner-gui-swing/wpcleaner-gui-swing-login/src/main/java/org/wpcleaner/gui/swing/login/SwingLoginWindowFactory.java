package org.wpcleaner.gui.swing.login;

/*
 * SPDX-FileCopyrightText: © 2024 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.awt.EventQueue;
import org.springframework.stereotype.Service;
import org.wpcleaner.api.wiki.definition.KnownDefinitions;
import org.wpcleaner.gui.core.factory.LoginWindowFactory;
import org.wpcleaner.gui.swing.core.SwingCoreServices;

@Service
public class SwingLoginWindowFactory implements LoginWindowFactory {

  private final KnownDefinitions knownDefinitions;
  private final SwingCoreServices swingCoreServices;

  public SwingLoginWindowFactory(
      final KnownDefinitions knownDefinitions, final SwingCoreServices swingCoreServices) {
    this.knownDefinitions = knownDefinitions;
    this.swingCoreServices = swingCoreServices;
  }

  @Override
  public void displayLoginWindow() {
    EventQueue.invokeLater(() -> SwingLoginWindow.create(knownDefinitions, swingCoreServices));
  }
}
