package org.wpcleaner.application.gui.swing.main;

/*
 * SPDX-FileCopyrightText: © 2025 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.awt.EventQueue;
import org.springframework.stereotype.Service;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.application.gui.core.factory.MainWindowFactory;
import org.wpcleaner.application.gui.swing.core.SwingCoreServices;

@Service
public class SwingMainWindowFactory implements MainWindowFactory {

  private final SwingCoreServices swingCoreServices;

  public SwingMainWindowFactory(final SwingCoreServices swingCoreServices) {
    this.swingCoreServices = swingCoreServices;
  }

  @Override
  public void displayMainWindow(final String user, final WikiDefinition wiki) {
    EventQueue.invokeLater(() -> SwingMainWindow.create(swingCoreServices, user, wiki));
  }
}
