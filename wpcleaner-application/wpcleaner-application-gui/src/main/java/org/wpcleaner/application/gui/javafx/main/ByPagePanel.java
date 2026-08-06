package org.wpcleaner.application.gui.javafx.main;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import javafx.geometry.Insets;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import org.wpcleaner.api.api.query.list.random.ApiRandom;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.application.gui.javafx.JavaFxImageLoader;
import org.wpcleaner.application.gui.javafx.core.action.JavaFxActionServices;
import org.wpcleaner.application.gui.settings.interesting.InterestingSettingsManager;

final class ByPagePanel extends GridPane {

  private final JavaFxActionServices actionServices;
  private final ApiRandom apiRandom;
  private final JavaFxImageLoader imageLoader;
  private final InterestingSettingsManager interestingSettings;
  private final WikiDefinition wiki;

  ByPagePanel(
      final WikiDefinition wiki,
      final InterestingSettingsManager interestingSettings,
      final JavaFxImageLoader imageLoader,
      final JavaFxActionServices actionServices,
      final ApiRandom apiRandom) {
    this.actionServices = actionServices;
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

    final PageInput page =
        new PageInput(wiki, interestingSettings, imageLoader, actionServices, apiRandom);
    setHgrow(page.comboBox, Priority.ALWAYS);
    add(page.label, 0, 0);
    add(page.icon, 1, 0);
    add(page.comboBox, 2, 0);
    add(page.toolBar, 3, 0);
  }
}
