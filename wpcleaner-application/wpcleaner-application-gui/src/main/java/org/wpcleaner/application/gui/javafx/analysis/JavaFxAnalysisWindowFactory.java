package org.wpcleaner.application.gui.javafx.analysis;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import javafx.application.Platform;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.wpcleaner.application.gui.core.factory.AnalysisWindowFactory;

@Service
public class JavaFxAnalysisWindowFactory implements AnalysisWindowFactory {

  private final JavaFxAnalysisWindowServices services;
  @Nullable private JavaFxAnalysisWindow window;

  public JavaFxAnalysisWindowFactory(final JavaFxAnalysisWindowServices services) {
    this.services = services;
  }

  @Override
  public void displayAnalysisWindow(final String pageName) {
    Platform.runLater(
        () -> {
          if (window == null || !window.isShowing()) {
            window = new JavaFxAnalysisWindow(services);
            window.show();
          } else {
            window.toFront();
          }
          window.analyze(pageName);
        });
  }
}
