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
import javafx.stage.Stage;
import org.wpcleaner.application.gui.javafx.JavaFxImageLoader;

public final class JavaFxAnalysisWindow extends Stage {

  private final JavaFxAnalysisWindowServices services;
  private final JavaFxImageLoader imageLoader;
  private final TabPane tabPane;

  public JavaFxAnalysisWindow(final JavaFxAnalysisWindowServices services) {
    super();
    this.services = services;
    this.imageLoader = new JavaFxImageLoader(services.imageLoader());
    this.tabPane = new TabPane();
    initialize();
  }

  private void initialize() {
    setTitle("WPCleaner - Analysis");
    imageLoader.setWindowIcon(this);
    services.windowsRegistry().register(this);

    VBox.setVgrow(tabPane, Priority.ALWAYS);
    final VBox root = new VBox(tabPane);

    final Scene scene = new Scene(root, 800, 600);
    setScene(scene);
    services.actionServices().positionWindow(this, "analysis");
  }

  public void analyze(final String pageName) {
    final Tab tab = new Tab(pageName);
    final PageAnalysisPanel panel = new PageAnalysisPanel(services, pageName);
    tab.setContent(panel);
    tabPane.getTabs().add(tab);
    tabPane.getSelectionModel().select(tab);
  }
}
