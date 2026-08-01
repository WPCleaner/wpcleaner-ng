package org.wpcleaner.application.gui.javafx.recentchanges;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.wpcleaner.api.utils.GT;
import org.wpcleaner.application.gui.javafx.JavaFxImageLoader;
import org.wpcleaner.application.gui.javafx.JavaFxProgressTracker;

public final class JavaFxRecentChangesWindow extends Stage {

  private final JavaFxRecentChangesWindowServices services;
  private final JavaFxImageLoader imageLoader;
  private final BooleanProperty loading;
  private final JavaFxProgressTracker progressTracker;
  private final RecentChangesDetailsPanel detailsPanel;

  public JavaFxRecentChangesWindow(final JavaFxRecentChangesWindowServices services) {
    super();
    this.services = services;
    this.imageLoader = new JavaFxImageLoader(services.imageLoader());
    this.loading = new SimpleBooleanProperty(false);
    this.progressTracker = JavaFxProgressTracker.forObservable(loading);
    this.detailsPanel =
        new RecentChangesDetailsPanel(services, imageLoader, progressTracker, loading);
    initialize();
  }

  private void initialize() {
    setTitle("WPCleaner - " + GT._T("Recent changes"));
    imageLoader.setWindowIcon(this);
    services.windowsRegistry().register(this);

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

    final Scene scene = new Scene(root, 1200, 600);
    setScene(scene);

    setOnCloseRequest(_ -> upperPanel.stop());
    services.actionServices().positionWindow(this, "recentChanges");
  }
}
