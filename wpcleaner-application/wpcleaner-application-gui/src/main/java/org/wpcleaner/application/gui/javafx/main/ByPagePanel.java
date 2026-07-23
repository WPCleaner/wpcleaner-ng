package org.wpcleaner.application.gui.javafx.main;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import javafx.geometry.Insets;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import org.wpcleaner.api.wiki.definition.WikiDefinition;
import org.wpcleaner.application.gui.javafx.JavaFxImageLoader;
import org.wpcleaner.application.gui.settings.interesting.InterestingSettingsManager;

final class ByPagePanel extends GridPane {

  private final WikiDefinition wiki;
  private final InterestingSettingsManager interestingSettings;
  private final JavaFxImageLoader imageLoader;

  ByPagePanel(
      final WikiDefinition wiki,
      final InterestingSettingsManager interestingSettings,
      final JavaFxImageLoader imageLoader) {
    this.wiki = wiki;
    this.interestingSettings = interestingSettings;
    this.imageLoader = imageLoader;
    initialize();
  }

  private void initialize() {
    setHgap(10);
    setVgap(8);
    setPadding(new Insets(10, 15, 10, 15));

    final ColumnConstraints colLabel = new ColumnConstraints();
    colLabel.setPercentWidth(20);
    colLabel.setHgrow(Priority.NEVER);

    final ColumnConstraints colIcon = new ColumnConstraints();
    colIcon.setPercentWidth(8);
    colIcon.setHgrow(Priority.NEVER);

    final ColumnConstraints colField = new ColumnConstraints();
    colField.setPercentWidth(44);
    colField.setHgrow(Priority.ALWAYS);

    final ColumnConstraints colToolbar = new ColumnConstraints();
    colToolbar.setPercentWidth(28);
    colToolbar.setHgrow(Priority.NEVER);

    getColumnConstraints().addAll(colLabel, colIcon, colField, colToolbar);

    final PageInput page = new PageInput(wiki, interestingSettings, imageLoader);
    add(page.label, 0, 0);
    add(page.icon, 1, 0);
    add(page.comboBox, 2, 0);
    add(page.toolBar, 3, 0);
  }
}
