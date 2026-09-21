package org.wpcleaner.application.gui.javafx.analysis;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.wpcleaner.application.gui.javafx.core.window.JavaFxWindow;

public final class JavaFxAnalysisWindow extends JavaFxWindow<JavaFxAnalysisWindowServices> {

  private final TabPane tabPane;

  public JavaFxAnalysisWindow(final JavaFxAnalysisWindowServices services) {
    super(services);
    this.tabPane = new TabPane();
    initialize();
    stage.show();
  }

  @Override
  public String getName() {
    return "analysis";
  }

  @Override
  protected Scene createScene() {
    VBox.setVgrow(tabPane, Priority.ALWAYS);
    final VBox root = new VBox(tabPane);
    return new Scene(root, 800, 600);
  }

  public void analyze(final String pageName) {
    final Tab tab = new Tab(pageName);
    final PageAnalysisPanel panel = new PageAnalysisPanel(services, pageName);
    tab.setContent(panel);
    tabPane.getTabs().add(tab);
    tabPane.getSelectionModel().select(tab);
  }
}
