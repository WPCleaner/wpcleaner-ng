package org.wpcleaner.application.gui.javafx.recentchanges;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Optional;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.api.utils.GT;
import org.wpcleaner.application.gui.javafx.JavaFxImageLoader;
import org.wpcleaner.application.gui.javafx.core.action.JavaFxActionServices;
import org.wpcleaner.lib.image.ImageCollection;
import org.wpcleaner.lib.image.ImageSize;

public final class RecentChangesDetailsToolBar extends ToolBar {

  private final ObjectProperty<@Nullable FilteredRecentChange> currentRecentChange =
      new SimpleObjectProperty<>(this, "currentRecentChange");

  public RecentChangesDetailsToolBar(
      final JavaFxImageLoader imageLoader,
      final JavaFxActionServices actionServices,
      final TabPane tabPane,
      final Tab differencesTab,
      final RecentChangesDifferencesPanel differencesPanel) {
    super();
    setStyle("-fx-background-color: transparent; -fx-padding: 0; -fx-spacing: 1px;");

    final Label pageLabel = new Label(GT._T("Page:"));
    imageLoader
        .getImageView(ImageCollection.PAGE, ImageSize.LABEL)
        .ifPresent(pageLabel::setGraphic);

    final TextField titleField = new TextField();
    titleField.setEditable(false);
    titleField.setMaxWidth(Double.MAX_VALUE);
    HBox.setHgrow(titleField, Priority.ALWAYS);

    final Button openPageButton = new Button();
    openPageButton.setStyle("-fx-background-color: transparent; -fx-padding: 1px;");
    imageLoader
        .getImageView(ImageCollection.OPEN_URL, ImageSize.BUTTON)
        .ifPresent(openPageButton::setGraphic);
    openPageButton.setTooltip(new Tooltip(GT._T("Open page in browser")));
    openPageButton.setDisable(true);
    openPageButton.setOnAction(
        _ -> {
          final FilteredRecentChange rc = currentRecentChange.get();
          if (rc != null && rc.pageURI() != null) {
            actionServices.browse(rc.pageURI().toString());
          }
        });

    final Button openDiffButton = new Button();
    openDiffButton.setStyle("-fx-background-color: transparent; -fx-padding: 1px;");
    imageLoader
        .getImageView(ImageCollection.DIFF, ImageSize.BUTTON)
        .ifPresent(openDiffButton::setGraphic);
    openDiffButton.setTooltip(new Tooltip(GT._T("Open modifications in browser")));
    openDiffButton.setDisable(true);
    openDiffButton.setOnAction(
        _ -> {
          final FilteredRecentChange rc = currentRecentChange.get();
          if (rc != null && rc.diffURI() != null) {
            actionServices.browse(rc.diffURI().toString());
          }
        });

    final Button goFirstButton = new Button();
    goFirstButton.setStyle("-fx-background-color: transparent; -fx-padding: 1px;");
    imageLoader
        .getImageView(ImageCollection.GO_FIRST, ImageSize.BUTTON)
        .ifPresent(goFirstButton::setGraphic);
    goFirstButton.setTooltip(new Tooltip(GT._T("Go to first modification")));
    goFirstButton.setDisable(true);
    goFirstButton.setOnAction(
        _ -> {
          tabPane.getSelectionModel().select(differencesTab);
          differencesPanel.selectFirstDelta();
        });

    final Button goPreviousButton = new Button();
    goPreviousButton.setStyle("-fx-background-color: transparent; -fx-padding: 1px;");
    imageLoader
        .getImageView(ImageCollection.GO_PREVIOUS, ImageSize.BUTTON)
        .ifPresent(goPreviousButton::setGraphic);
    goPreviousButton.setTooltip(new Tooltip(GT._T("Go to previous modification")));
    goPreviousButton.setDisable(true);
    goPreviousButton.setOnAction(
        _ -> {
          tabPane.getSelectionModel().select(differencesTab);
          differencesPanel.selectPreviousDelta();
        });

    final Button goNextButton = new Button();
    goNextButton.setStyle("-fx-background-color: transparent; -fx-padding: 1px;");
    imageLoader
        .getImageView(ImageCollection.GO_NEXT, ImageSize.BUTTON)
        .ifPresent(goNextButton::setGraphic);
    goNextButton.setTooltip(new Tooltip(GT._T("Go to next modification")));
    goNextButton.setDisable(true);
    goNextButton.setOnAction(
        _ -> {
          tabPane.getSelectionModel().select(differencesTab);
          differencesPanel.selectNextDelta();
        });

    final Button goLastButton = new Button();
    goLastButton.setStyle("-fx-background-color: transparent; -fx-padding: 1px;");
    imageLoader
        .getImageView(ImageCollection.GO_LAST, ImageSize.BUTTON)
        .ifPresent(goLastButton::setGraphic);
    goLastButton.setTooltip(new Tooltip(GT._T("Go to last modification")));
    goLastButton.setDisable(true);
    goLastButton.setOnAction(
        _ -> {
          tabPane.getSelectionModel().select(differencesTab);
          differencesPanel.selectLastDelta();
        });

    final Separator separator = new Separator();

    getItems()
        .addAll(
            openPageButton,
            openDiffButton,
            goFirstButton,
            goPreviousButton,
            goNextButton,
            goLastButton,
            separator,
            pageLabel,
            titleField);

    currentRecentChange.addListener(
        (_, _, rc) -> {
          titleField.setText(Optional.ofNullable(rc).map(FilteredRecentChange::title).orElse(""));
          openPageButton.setDisable(rc == null || rc.pageURI() == null);
          openDiffButton.setDisable(rc == null || rc.diffURI() == null);
          final boolean hasRc = rc != null;
          goFirstButton.setDisable(!hasRc);
          goPreviousButton.setDisable(!hasRc);
          goNextButton.setDisable(!hasRc);
          goLastButton.setDisable(!hasRc);
        });
  }

  public ObjectProperty<@Nullable FilteredRecentChange> currentRecentChangeProperty() {
    return currentRecentChange;
  }
}
