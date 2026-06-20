package org.wpcleaner.application.gui.swing.recentchanges;

/*
 * SPDX-FileCopyrightText: © 2025 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.awt.EventQueue;
import org.springframework.stereotype.Service;

@Service
public class SwingRecentChangesWindowFactory {

  private final SwingRecentChangesWindowServices services;

  public SwingRecentChangesWindowFactory(final SwingRecentChangesWindowServices services) {
    this.services = services;
  }

  public void displayRecentChangesWindow() {
    EventQueue.invokeLater(() -> SwingRecentChangesWindow.create(services));
  }
}
