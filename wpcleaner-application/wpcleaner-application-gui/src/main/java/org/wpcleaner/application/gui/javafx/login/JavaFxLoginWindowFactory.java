/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wpcleaner.application.gui.javafx.login;

import javafx.application.Platform;
import org.springframework.stereotype.Service;
import org.wpcleaner.application.gui.core.factory.LoginWindowFactory;
import org.wpcleaner.application.gui.javafx.JavaFxInitializer;

@Service
public class JavaFxLoginWindowFactory implements LoginWindowFactory {

  private final JavaFxLoginWindowServices services;

  public JavaFxLoginWindowFactory(final JavaFxLoginWindowServices services) {
    this.services = services;
  }

  @Override
  public void displayLoginWindow() {
    JavaFxInitializer.initialize();
    Platform.runLater(
        () -> {
          final JavaFxLoginWindow window = new JavaFxLoginWindow(services);
          window.show();
        });
  }
}
