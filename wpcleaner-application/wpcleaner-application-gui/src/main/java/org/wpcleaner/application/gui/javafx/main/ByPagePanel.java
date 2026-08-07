package org.wpcleaner.application.gui.javafx.main;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import org.wpcleaner.api.api.query.list.random.ApiRandom;
import org.wpcleaner.api.utils.GT;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.application.gui.core.factory.AnalysisWindowFactory;
import org.wpcleaner.application.gui.javafx.JavaFxImageLoader;
import org.wpcleaner.application.gui.settings.interesting.InterestingSettingsManager;
import org.wpcleaner.lib.image.ImageCollection;
import org.wpcleaner.lib.image.ImageSize;

final class ByPagePanel extends GridPane {

  private final AnalysisWindowFactory analysisWindowFactory;
  private final ApiRandom apiRandom;
  private final JavaFxImageLoader imageLoader;
  private final InterestingSettingsManager interestingSettings;
  private final WikiDefinition wiki;

  ByPagePanel(
      final WikiDefinition wiki,
      final InterestingSettingsManager interestingSettings,
      final JavaFxImageLoader imageLoader,
      final ApiRandom apiRandom,
      final AnalysisWindowFactory analysisWindowFactory) {
    this.analysisWindowFactory = analysisWindowFactory;
    this.apiRandom = apiRandom;
    this.imageLoader = imageLoader;
    this.interestingSettings = interestingSettings;
    this.wiki = wiki;
    initialize();
  }

  private void initialize() {
    setHgap(10);
    setVgap(8);
    setPadding(new Insets(10, 15, 10, 15));

    final ColumnConstraints colLabel = new ColumnConstraints();
    colLabel.setPrefWidth(40);
    colLabel.setMinWidth(40);
    colLabel.setHgrow(Priority.NEVER);

    final ColumnConstraints colIcon = new ColumnConstraints();
    colIcon.setPrefWidth(25);
    colIcon.setMinWidth(25);
    colIcon.setHgrow(Priority.NEVER);

    final ColumnConstraints colField = new ColumnConstraints();
    colField.setHgrow(Priority.ALWAYS);

    final ColumnConstraints colToolbar = new ColumnConstraints();
    colToolbar.setPrefWidth(70);
    colToolbar.setMinWidth(70);
    colToolbar.setHgrow(Priority.NEVER);

    getColumnConstraints().addAll(colLabel, colIcon, colField, colToolbar);

    final PageInput page = new PageInput(wiki, interestingSettings, imageLoader, apiRandom);
    setHgrow(page.comboBox, Priority.ALWAYS);

    final Button analysisButton = new Button(GT._T("Analysis"));
    analysisButton.setMaxWidth(Double.MAX_VALUE);
    imageLoader
        .getImageView(ImageCollection.ANALYSIS, ImageSize.BUTTON)
        .ifPresent(analysisButton::setGraphic);
    analysisButton.setOnAction(
        _ -> {
          final String pageName = page.getPage();
          if (pageName.isEmpty()) {
            final Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle(GT._T("Warning"));
            alert.setHeaderText(null);
            alert.setContentText(GT._T("The page name cannot be empty."));
            alert.showAndWait();
          } else {
            analysisWindowFactory.displayAnalysisWindow(pageName);
          }
        });

    add(page.label, 0, 0);
    add(page.icon, 1, 0);
    add(page.comboBox, 2, 0);
    add(page.toolBar, 3, 0);
    add(analysisButton, 0, 1, 4, 1);
  }
}
