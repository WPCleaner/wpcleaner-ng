package org.wpcleaner.application.gui.javafx.recentchanges;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.wpcleaner.application.gui.javafx.core.window.JavaFxWindow;

public final class JavaFxRecentChangesWindow
    extends JavaFxWindow<JavaFxRecentChangesWindowServices> {

  private final RecentChangesDetailsPanel detailsPanel;

  public JavaFxRecentChangesWindow(final JavaFxRecentChangesWindowServices services) {
    super(services);
    this.detailsPanel =
        new RecentChangesDetailsPanel(services, imageLoader, progressTracker, loading);
    initialize();
    stage.show();
  }

  @Override
  public String getName() {
    return "recentChanges";
  }

  @Override
  protected Scene createScene() {
    final VBox mainContainer = new VBox(10);
    mainContainer.setPadding(new Insets(10, 15, 10, 15));

    final RecentChangesListPanel upperPanel =
        new RecentChangesListPanel(
            this, services, imageLoader, progressTracker, loading, detailsPanel::viewModifications);

    final SplitPane splitPane = new SplitPane();
    splitPane.setOrientation(Orientation.VERTICAL);
    splitPane.getItems().addAll(upperPanel, detailsPanel);
    splitPane.setDividerPositions(0.5);
    VBox.setVgrow(splitPane, Priority.ALWAYS);

    mainContainer.getChildren().add(splitPane);

    mainContainer.disableProperty().bind(loading);

    final StackPane root = new StackPane();
    root.getChildren().addAll(mainContainer, progressTracker.getProgressOverlay());
    stage.setOnCloseRequest(_ -> upperPanel.stop());
    return new Scene(root, 1200, 600);
  }
}
