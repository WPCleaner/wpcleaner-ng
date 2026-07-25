package org.wpcleaner.application.gui.swing.main;

/*
 * SPDX-FileCopyrightText: © 2025 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.awt.EventQueue;
import org.springframework.stereotype.Service;
import org.wpcleaner.application.gui.core.factory.MainWindowFactory;

@Service
public class SwingMainWindowFactory implements MainWindowFactory {

  private final SwingMainWindowServices services;

  public SwingMainWindowFactory(final SwingMainWindowServices services) {
    this.services = services;
  }

  @Override
  public void displayMainWindow() {
    EventQueue.invokeLater(() -> SwingMainWindow.create(services));
  }
}
