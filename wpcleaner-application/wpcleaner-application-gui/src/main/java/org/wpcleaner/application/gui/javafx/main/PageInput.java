package org.wpcleaner.application.gui.javafx.main;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

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
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.application.gui.javafx.JavaFxImageLoader;
import org.wpcleaner.application.gui.settings.interesting.InterestingSettingsManager;
import org.wpcleaner.lib.image.ImageCollection;
import org.wpcleaner.lib.image.ImageSize;

public class PageInput {

  final ComboBox<@Nullable String> comboBox;
  final ImageView icon;
  final Label label;
  final ToolBar toolBar;

  PageInput(
      final WikiDefinition wiki,
      final InterestingSettingsManager settingsManager,
      final JavaFxImageLoader imageLoader) {
    icon =
        imageLoader.getImageView(ImageCollection.PAGE, ImageSize.LABEL).orElseGet(ImageView::new);

    comboBox = new ComboBox<>();
    comboBox.setEditable(true);
    comboBox.setMaxWidth(Double.MAX_VALUE);

    settingsManager.getCurrentSettings().getByWikiSettings(wiki).stream()
        .flatMap(settings -> settings.pages().stream())
        .forEach(comboBox.getItems()::add);

    label = new Label("Page");
    label.setMaxWidth(Double.MAX_VALUE);
    label.setAlignment(Pos.CENTER_RIGHT);

    final Button addPage = new Button();
    addPage.setStyle("-fx-background-color: transparent; -fx-padding: 1px;");
    imageLoader
        .getImageView(ImageCollection.LIST_ADD, ImageSize.TOOLBAR)
        .ifPresent(addPage::setGraphic);
    addPage.setTooltip(new Tooltip("Add page"));
    addPage.setOnAction(_ -> showNotImplementedAlert());

    final Button removePage = new Button();
    removePage.setStyle("-fx-background-color: transparent; -fx-padding: 1px;");
    imageLoader
        .getImageView(ImageCollection.LIST_REMOVE, ImageSize.TOOLBAR)
        .ifPresent(removePage::setGraphic);
    removePage.setTooltip(new Tooltip("Forget page"));
    removePage.setOnAction(_ -> showNotImplementedAlert());

    toolBar = new ToolBar();
    toolBar.setStyle("-fx-background-color: transparent; -fx-padding: 0; -fx-spacing: 1px;");
    toolBar.getItems().addAll(addPage, removePage);
  }

  public String getPage() {
    return Objects.requireNonNullElse(comboBox.getValue(), "");
  }

  private void showNotImplementedAlert() {
    final Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle("Not Implemented");
    alert.setHeaderText(null);
    alert.setContentText("This feature is not implemented yet.");
    alert.showAndWait();
  }
}
