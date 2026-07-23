package org.wpcleaner.application.gui.javafx;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;
import org.wpcleaner.application.base.processor.DefaultProgressTracker;
import org.wpcleaner.application.base.processor.ProgressStep;
import org.wpcleaner.application.base.processor.ProgressTracker;

public final class JavaFxProgressTracker implements ProgressTracker {

  private final Label progressLabel;
  private final VBox progressOverlay;
  private final ProgressTracker delegate;

  public static JavaFxProgressTracker forObservable(final BooleanProperty property) {
    final VBox progressOverlay = new VBox(15);
    progressOverlay.setAlignment(Pos.CENTER);
    progressOverlay.setPadding(new Insets(20));
    progressOverlay.setStyle("-fx-background-color: rgba(255, 255, 255, 0.85);");

    final ProgressIndicator progressIndicator = new ProgressIndicator();
    final Label progressLabel = new Label("Processing...");
    progressLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #333333;");
    progressOverlay.getChildren().addAll(progressIndicator, progressLabel);

    progressOverlay.visibleProperty().bind(property);
    progressOverlay.managedProperty().bind(property);
    return new JavaFxProgressTracker(progressLabel, progressOverlay);
  }

  private JavaFxProgressTracker(final Label progressLabel, final VBox progressOverlay) {
    this.progressLabel = progressLabel;
    this.progressOverlay = progressOverlay;
    this.delegate = new DefaultProgressTracker(this::updateProgress);
  }

  public VBox getProgressOverlay() {
    return progressOverlay;
  }

  @Override
  public ProgressStep start(final String description) {
    return delegate.start(description);
  }

  @Override
  public void end(final ProgressStep step) {
    delegate.end(step);
  }

  private void updateProgress(final List<String> steps) {
    Platform.runLater(
        () -> {
          if (!steps.isEmpty()) {
            progressLabel.setText(steps.getLast());
          }
        });
  }
}
