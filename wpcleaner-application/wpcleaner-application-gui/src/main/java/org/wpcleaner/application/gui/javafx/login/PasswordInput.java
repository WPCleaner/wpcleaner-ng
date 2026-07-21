/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wpcleaner.application.gui.javafx.login;

import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import org.wpcleaner.application.base.utils.url.UrlService;
import org.wpcleaner.application.gui.core.desktop.DesktopService;
import org.wpcleaner.application.gui.javafx.JavaFxImageLoader;
import org.wpcleaner.application.gui.javafx.JavaFxInitializer;
import org.wpcleaner.lib.image.ImageCollection;
import org.wpcleaner.lib.image.ImageSize;

final class PasswordInput {

  final PasswordField field;
  final ImageView icon;
  final Label label;
  final ToolBar toolBar;

  PasswordInput(final JavaFxImageLoader imageLoader, final DesktopService desktopService) {
    icon =
        imageLoader
            .getImageView(ImageCollection.PASSWORD, ImageSize.LABEL)
            .orElseGet(ImageView::new);

    field = new PasswordField();
    field.setMaxWidth(Double.MAX_VALUE);

    label = new Label("Password");
    label.setMaxWidth(Double.MAX_VALUE);
    label.setAlignment(Pos.CENTER_RIGHT);

    final Button botPasswordsButton = new Button();
    botPasswordsButton.setStyle("-fx-background-color: transparent; -fx-padding: 1px;");
    imageLoader
        .getImageView(ImageCollection.HELP, ImageSize.TOOLBAR)
        .ifPresent(botPasswordsButton::setGraphic);
    botPasswordsButton.setTooltip(new Tooltip("Bot passwords"));
    botPasswordsButton.setOnAction(
        _ -> JavaFxInitializer.browse(desktopService, UrlService.BOT_PASSWORDS));

    final Button addPasswordButton = new Button();
    addPasswordButton.setStyle("-fx-background-color: transparent; -fx-padding: 1px;");
    imageLoader
        .getImageView(ImageCollection.LIST_ADD, ImageSize.TOOLBAR)
        .ifPresent(addPasswordButton::setGraphic);
    addPasswordButton.setTooltip(new Tooltip("Save user and password"));
    addPasswordButton.setOnAction(_ -> showNotImplementedAlert());

    final Button removePasswordButton = new Button();
    removePasswordButton.setStyle("-fx-background-color: transparent; -fx-padding: 1px;");
    imageLoader
        .getImageView(ImageCollection.LIST_REMOVE, ImageSize.TOOLBAR)
        .ifPresent(removePasswordButton::setGraphic);
    removePasswordButton.setTooltip(new Tooltip("Forget password"));
    removePasswordButton.setOnAction(_ -> showNotImplementedAlert());

    toolBar = new ToolBar();
    toolBar.setStyle("-fx-background-color: transparent; -fx-padding: 0; -fx-spacing: 1px;");
    toolBar.getItems().addAll(botPasswordsButton, addPasswordButton, removePasswordButton);
  }

  char[] getPassword() {
    final String text = field.getText();
    return text == null ? new char[0] : text.toCharArray();
  }

  void setPassword(final String password) {
    field.setText(password);
  }

  private void showNotImplementedAlert() {
    final Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle("Not Implemented");
    alert.setHeaderText(null);
    alert.setContentText("This feature is not implemented yet.");
    alert.showAndWait();
  }
}
