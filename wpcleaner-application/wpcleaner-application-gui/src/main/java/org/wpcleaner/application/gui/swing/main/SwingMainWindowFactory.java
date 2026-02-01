package org.wpcleaner.application.gui.swing.main;

/*
 * SPDX-FileCopyrightText: © 2025 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.awt.EventQueue;
import org.springframework.stereotype.Service;
import org.wpcleaner.api.api.CurrentUserService;
import org.wpcleaner.application.gui.core.factory.MainWindowFactory;
import org.wpcleaner.application.gui.settings.graphical.GraphicalSettingsManager;
import org.wpcleaner.application.gui.swing.core.SwingCoreServices;

@Service
public class SwingMainWindowFactory implements MainWindowFactory {

  private final CurrentUserService currentUserService;
  private final GraphicalSettingsManager settingsManager;
  private final SwingCoreServices swingCoreServices;

  public SwingMainWindowFactory(
      final CurrentUserService currentUserService,
      final GraphicalSettingsManager settingsManager,
      final SwingCoreServices swingCoreServices) {
    this.currentUserService = currentUserService;
    this.settingsManager = settingsManager;
    this.swingCoreServices = swingCoreServices;
  }

  @Override
  public void displayMainWindow() {
    EventQueue.invokeLater(
        () ->
            SwingMainWindow.create(
                settingsManager, swingCoreServices, currentUserService.getCurrentUser()));
  }
}
