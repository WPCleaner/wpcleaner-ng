package org.wpcleaner.application.gui.javafx.main;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import javafx.application.Platform;
import org.springframework.stereotype.Service;
import org.wpcleaner.application.gui.core.factory.MainWindowFactory;
import org.wpcleaner.application.gui.javafx.JavaFxInitializer;

@Service
public class JavaFxMainWindowFactory implements MainWindowFactory {

  private final JavaFxMainWindowServices services;

  public JavaFxMainWindowFactory(final JavaFxMainWindowServices services) {
    this.services = services;
  }

  @Override
  public void displayMainWindow() {
    JavaFxInitializer.initialize();
    Platform.runLater(
        () -> {
          final JavaFxMainWindow window = new JavaFxMainWindow(services);
          window.show();
        });
  }
}
