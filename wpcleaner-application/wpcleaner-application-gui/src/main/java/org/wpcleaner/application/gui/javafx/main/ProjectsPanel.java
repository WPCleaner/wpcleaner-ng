package org.wpcleaner.application.gui.javafx.main;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

final class ProjectsPanel extends VBox {

  private final JavaFxMainWindowServices services;

  ProjectsPanel(final JavaFxMainWindowServices services) {
    super(10);
    this.services = services;
    initialize();
  }

  private void initialize() {
    setPadding(new Insets(10, 15, 10, 15));
    setAlignment(Pos.TOP_LEFT);

    final Button button = new Button("Recent changes");
    button.setOnAction(_ -> services.recentChangesWindowFactory().displayRecentChangesWindow());

    getChildren().add(button);
  }
}
