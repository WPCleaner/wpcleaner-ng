/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wpcleaner.application.gui.javafx.login;

import java.util.Optional;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.api.wiki.definition.KnownDefinitions;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.application.gui.core.desktop.DesktopService;
import org.wpcleaner.application.gui.javafx.JavaFxImageLoader;
import org.wpcleaner.application.gui.javafx.JavaFxInitializer;
import org.wpcleaner.lib.image.ImageCollection;
import org.wpcleaner.lib.image.ImageSize;

final class WikiInput {

  private static final String NO_WARNING = "No warning";
  private static final String WARNING = "Warning";

  final ComboBox<@Nullable WikiDefinition> comboBox;
  final ImageView icon;
  final Label label;
  final ToolBar toolBar;

  WikiInput(
      final KnownDefinitions knownDefinitions,
      final JavaFxImageLoader imageLoader,
      final DesktopService desktopService) {
    icon =
        imageLoader
            .getImageView(ImageCollection.LOGO_MEDIAWIKI, ImageSize.LABEL)
            .orElseGet(ImageView::new);

    comboBox = new ComboBox<>();
    comboBox.getItems().addAll(knownDefinitions.getDefinitions());
    comboBox.getSelectionModel().select(knownDefinitions.getPreferred());
    comboBox.setMaxWidth(Double.MAX_VALUE);

    comboBox.setCellFactory(_ -> new WikiListCell(imageLoader));
    comboBox.setButtonCell(new WikiListCell(imageLoader));

    label = new Label("Wiki");
    label.setMaxWidth(Double.MAX_VALUE);
    label.setAlignment(Pos.CENTER_RIGHT);

    final Button warningButton = new Button();
    warningButton.setStyle("-fx-background-color: transparent; -fx-padding: 1px;");
    imageLoader
        .getImageView(ImageCollection.WARNING, ImageSize.TOOLBAR)
        .ifPresent(warningButton::setGraphic);
    warningButton.setDisable(true);
    warningButton.setTooltip(new Tooltip(NO_WARNING));

    final Button otherWikiButton = new Button();
    otherWikiButton.setStyle("-fx-background-color: transparent; -fx-padding: 1px;");
    imageLoader
        .getImageView(ImageCollection.HELP, ImageSize.TOOLBAR)
        .ifPresent(otherWikiButton::setGraphic);
    otherWikiButton.setTooltip(new Tooltip("Other wiki"));
    otherWikiButton.setOnAction(
        _ ->
            JavaFxInitializer.browse(
                desktopService, "https://en.wikipedia.org/wiki/Wikipedia:WPCleaner/Wikis"));

    final Button addWikiButton = new Button();
    addWikiButton.setStyle("-fx-background-color: transparent; -fx-padding: 1px;");
    imageLoader
        .getImageView(ImageCollection.LIST_ADD, ImageSize.TOOLBAR)
        .ifPresent(addWikiButton::setGraphic);
    addWikiButton.setTooltip(new Tooltip("Add wiki"));
    addWikiButton.setOnAction(_ -> showNotImplementedAlert());

    final Button removeWikiButton = new Button();
    removeWikiButton.setStyle("-fx-background-color: transparent; -fx-padding: 1px;");
    imageLoader
        .getImageView(ImageCollection.LIST_REMOVE, ImageSize.TOOLBAR)
        .ifPresent(removeWikiButton::setGraphic);
    removeWikiButton.setTooltip(new Tooltip("Remove wiki"));
    removeWikiButton.setOnAction(_ -> showNotImplementedAlert());

    toolBar = new ToolBar();
    toolBar.setStyle("-fx-background-color: transparent; -fx-padding: 0; -fx-spacing: 1px;");
    toolBar.getItems().addAll(warningButton, otherWikiButton, addWikiButton, removeWikiButton);

    comboBox
        .getSelectionModel()
        .selectedItemProperty()
        .addListener(
            (_, _, newVal) -> {
              final boolean hasWarning = newVal != null && newVal.warning() != null;
              warningButton.setDisable(!hasWarning);
              warningButton.setTooltip(new Tooltip(hasWarning ? WARNING : NO_WARNING));
            });

    warningButton.setOnAction(
        _ -> {
          final WikiDefinition selected = comboBox.getSelectionModel().getSelectedItem();
          if (selected != null && selected.warning() != null) {
            final Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle(WARNING);
            alert.setHeaderText("Warning for " + selected.name());
            alert.setContentText(selected.warning().text());
            alert.showAndWait();
          }
        });

    final WikiDefinition initialSelected = comboBox.getSelectionModel().getSelectedItem();
    if (initialSelected != null) {
      final boolean hasWarning = initialSelected.warning() != null;
      warningButton.setDisable(!hasWarning);
      warningButton.setTooltip(new Tooltip(hasWarning ? WARNING : NO_WARNING));
    }
  }

  Optional<WikiDefinition> getSelectedWiki() {
    return Optional.ofNullable(comboBox.getSelectionModel().getSelectedItem());
  }

  void addSelectionListener(final ChangeListener<@Nullable WikiDefinition> listener) {
    comboBox.getSelectionModel().selectedItemProperty().addListener(listener);
    final WikiDefinition current = comboBox.getSelectionModel().getSelectedItem();
    if (current != null) {
      listener.changed(comboBox.getSelectionModel().selectedItemProperty(), null, current);
    }
  }

  private void showNotImplementedAlert() {
    final Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle("Not Implemented");
    alert.setHeaderText(null);
    alert.setContentText("This feature is not implemented yet.");
    alert.showAndWait();
  }
}
