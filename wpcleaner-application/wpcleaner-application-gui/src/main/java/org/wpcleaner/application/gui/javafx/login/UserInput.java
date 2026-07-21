/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wpcleaner.application.gui.javafx.login;

import java.util.Objects;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.application.gui.javafx.JavaFxImageLoader;
import org.wpcleaner.lib.image.ImageCollection;
import org.wpcleaner.lib.image.ImageSize;

final class UserInput {

  final ComboBox<@Nullable String> comboBox;
  final ImageView icon;
  final Label label;
  final ToolBar toolBar;

  UserInput(final JavaFxImageLoader imageLoader) {
    icon =
        imageLoader.getImageView(ImageCollection.USER, ImageSize.LABEL).orElseGet(ImageView::new);

    comboBox = new ComboBox<>();
    comboBox.setEditable(true);
    comboBox.setMaxWidth(Double.MAX_VALUE);

    label = new Label("Username");
    label.setMaxWidth(Double.MAX_VALUE);
    label.setAlignment(Pos.CENTER_RIGHT);

    final Button addUserButton = new Button();
    addUserButton.setStyle("-fx-background-color: transparent; -fx-padding: 1px;");
    imageLoader
        .getImageView(ImageCollection.LIST_ADD, ImageSize.TOOLBAR)
        .ifPresent(addUserButton::setGraphic);
    addUserButton.setTooltip(new Tooltip("Save user"));
    addUserButton.setOnAction(_ -> showNotImplementedAlert());

    final Button removeUserButton = new Button();
    removeUserButton.setStyle("-fx-background-color: transparent; -fx-padding: 1px;");
    imageLoader
        .getImageView(ImageCollection.LIST_REMOVE, ImageSize.TOOLBAR)
        .ifPresent(removeUserButton::setGraphic);
    removeUserButton.setTooltip(new Tooltip("Forget user"));
    removeUserButton.setOnAction(_ -> showNotImplementedAlert());

    toolBar = new ToolBar();
    toolBar.setStyle("-fx-background-color: transparent; -fx-padding: 0; -fx-spacing: 1px;");
    toolBar.getItems().addAll(addUserButton, removeUserButton);
  }

  String getUser() {
    final Object value = comboBox.getSelectionModel().getSelectedItem();
    if (value == null) {
      final String editorText = comboBox.getEditor().getText();
      return Objects.requireNonNullElse(editorText, "");
    }
    return value.toString();
  }

  void setUser(final String user) {
    comboBox.getSelectionModel().select(user);
    comboBox.getEditor().setText(user);
  }

  private void showNotImplementedAlert() {
    final Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle("Not Implemented");
    alert.setHeaderText(null);
    alert.setContentText("This feature is not implemented yet.");
    alert.showAndWait();
  }
}
