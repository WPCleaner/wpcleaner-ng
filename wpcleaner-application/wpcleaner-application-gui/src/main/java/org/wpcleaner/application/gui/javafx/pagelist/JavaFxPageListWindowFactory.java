package org.wpcleaner.application.gui.javafx.pagelist;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import java.util.function.Consumer;
import javafx.application.Platform;
import org.springframework.stereotype.Service;
import org.wpcleaner.api.progress.LongRunningTask;
import org.wpcleaner.application.gui.core.factory.PageListWindowFactory;
import org.wpcleaner.application.gui.javafx.JavaFxInitializer;

@Service
public class JavaFxPageListWindowFactory implements PageListWindowFactory {

  private final JavaFxPageListWindowServices services;

  public JavaFxPageListWindowFactory(final JavaFxPageListWindowServices services) {
    this.services = services;
  }

  @Override
  public void displayPageListWindow(
      final String title,
      final LongRunningTask<List<String>> pagesTask,
      final Consumer<String> pageAction) {
    JavaFxInitializer.initialize();
    Platform.runLater(() -> new JavaFxPageListWindow(services, title, pagesTask, pageAction));
  }
}
