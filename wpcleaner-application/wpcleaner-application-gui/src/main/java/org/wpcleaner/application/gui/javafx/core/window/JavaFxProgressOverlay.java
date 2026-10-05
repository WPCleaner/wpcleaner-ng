package org.wpcleaner.application.gui.javafx.core.window;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import javafx.beans.property.BooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;
import org.wpcleaner.api.utils.GT;

public final class JavaFxProgressOverlay extends VBox {

  private final Label label;

  JavaFxProgressOverlay(final BooleanProperty progressVisible) {
    super(15);
    setAlignment(Pos.CENTER);
    setPadding(new Insets(20));
    setStyle("-fx-background-color: rgba(255, 255, 255, 0.85);");

    final ProgressIndicator indicator = new ProgressIndicator();
    label = new Label(GT._T("Processing..."));
    label.setStyle("-fx-font-weight: bold; -fx-text-fill: #333333;");
    getChildren().addAll(indicator, label);

    visibleProperty().bind(progressVisible);
    managedProperty().bind(progressVisible);
  }

  void setText(final String text) {
    label.setText(text);
  }
}
