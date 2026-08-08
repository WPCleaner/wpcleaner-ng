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
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.api.api.query.meta.siteinfo.ApiSiteInfo;
import org.wpcleaner.api.utils.GT;
import org.wpcleaner.api.wiki.definition.KnownDefinitions;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.application.gui.javafx.JavaFxImageLoader;
import org.wpcleaner.application.gui.javafx.core.action.JavaFxActionServices;
import org.wpcleaner.application.gui.javafx.core.control.DefaultStyles;
import org.wpcleaner.lib.image.ImageCollection;
import org.wpcleaner.lib.image.ImageSize;

final class WikiInput {

  private static final String NO_WARNING = "No warning";
  private static final String WARNING = "Warning";

  private final ApiSiteInfo apiSiteInfo;

  final ComboBox<@Nullable WikiDefinition> comboBox;
  final ImageView icon;
  final Label label;
  final ToolBar toolBar;

  WikiInput(
      final KnownDefinitions knownDefinitions,
      final ApiSiteInfo apiSiteInfo,
      final JavaFxImageLoader imageLoader,
      final JavaFxActionServices actionServices) {
    this.apiSiteInfo = apiSiteInfo;
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

    label = new Label(GT._T("Wiki"));
    label.setMaxWidth(Double.MAX_VALUE);
    label.setAlignment(Pos.CENTER_RIGHT);

    final Button warningButton = new Button();
    warningButton.setStyle(DefaultStyles.TOOLBAR_ELEMENT);
    imageLoader
        .getImageView(ImageCollection.WARNING, ImageSize.TOOLBAR)
        .ifPresent(warningButton::setGraphic);
    warningButton.setDisable(true);
    warningButton.setTooltip(new Tooltip(NO_WARNING));
    setupWarningButton(warningButton);

    final Button otherWikiButton = new Button();
    otherWikiButton.setStyle(DefaultStyles.TOOLBAR_ELEMENT);
    imageLoader
        .getImageView(ImageCollection.HELP, ImageSize.TOOLBAR)
        .ifPresent(otherWikiButton::setGraphic);
    otherWikiButton.setTooltip(new Tooltip(GT._T("Other wiki")));
    otherWikiButton.setOnAction(
        _ -> actionServices.browse("https://en.wikipedia.org/wiki/Wikipedia:WPCleaner/Wikis"));

    final Button addWikiButton = new Button();
    addWikiButton.setStyle(DefaultStyles.TOOLBAR_ELEMENT);
    imageLoader
        .getImageView(ImageCollection.LIST_ADD, ImageSize.TOOLBAR)
        .ifPresent(addWikiButton::setGraphic);
    addWikiButton.setTooltip(new Tooltip(GT._T("Add wiki")));
    setupAddWikiButton(addWikiButton, knownDefinitions, imageLoader);

    final Button removeWikiButton = new Button();
    removeWikiButton.setStyle(DefaultStyles.TOOLBAR_ELEMENT);
    imageLoader
        .getImageView(ImageCollection.LIST_REMOVE, ImageSize.TOOLBAR)
        .ifPresent(removeWikiButton::setGraphic);
    removeWikiButton.setTooltip(new Tooltip(GT._T("Remove wiki")));
    removeWikiButton.setDisable(true);
    setupRemoveWikiButton(removeWikiButton, knownDefinitions);

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

              final boolean isUserAdded = newVal != null && knownDefinitions.isUserAdded(newVal);
              removeWikiButton.setDisable(!isUserAdded);
            });

    final WikiDefinition initialSelected = comboBox.getSelectionModel().getSelectedItem();
    if (initialSelected != null) {
      final boolean hasWarning = initialSelected.warning() != null;
      warningButton.setDisable(!hasWarning);
      warningButton.setTooltip(new Tooltip(hasWarning ? WARNING : NO_WARNING));

      final boolean isUserAdded = knownDefinitions.isUserAdded(initialSelected);
      removeWikiButton.setDisable(!isUserAdded);
    }
  }

  private void setupAddWikiButton(
      final Button addWikiButton,
      final KnownDefinitions knownDefinitions,
      final JavaFxImageLoader imageLoader) {
    addWikiButton.setOnAction(
        _ -> {
          final WikiDefinitionDialog dialog =
              new WikiDefinitionDialog(comboBox.getScene().getWindow(), imageLoader, apiSiteInfo);
          dialog
              .showAndWait()
              .ifPresent(
                  newWiki -> {
                    knownDefinitions.addDefinition(newWiki);
                    comboBox.getItems().setAll(knownDefinitions.getDefinitions());
                    comboBox.getSelectionModel().select(newWiki);
                  });
        });
  }

  private void setupRemoveWikiButton(
      final Button removeWikiButton, final KnownDefinitions knownDefinitions) {
    removeWikiButton.setOnAction(
        _ -> {
          final WikiDefinition selected = comboBox.getSelectionModel().getSelectedItem();
          if (selected != null && knownDefinitions.isUserAdded(selected)) {
            final Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.initOwner(comboBox.getScene().getWindow());
            alert.setTitle(GT._T("Confirmation"));
            alert.setHeaderText(null);
            alert.setContentText(
                GT._T("Are you sure you want to remove the wiki \"%s\"?", selected.name()));
            alert
                .showAndWait()
                .ifPresent(
                    buttonType -> {
                      if (buttonType == ButtonType.OK) {
                        knownDefinitions.removeDefinition(selected);
                        comboBox.getItems().setAll(knownDefinitions.getDefinitions());
                        comboBox.getSelectionModel().select(knownDefinitions.getPreferred());
                      }
                    });
          }
        });
  }

  private void setupWarningButton(final Button warningButton) {
    warningButton.setOnAction(
        _ -> {
          final WikiDefinition selected = comboBox.getSelectionModel().getSelectedItem();
          if (selected != null && selected.warning() != null) {
            final Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle(WARNING);
            alert.setHeaderText(GT._T("Warning for %s", selected.name()));
            alert.setContentText(selected.warning().text());
            alert.showAndWait();
          }
        });
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
}
