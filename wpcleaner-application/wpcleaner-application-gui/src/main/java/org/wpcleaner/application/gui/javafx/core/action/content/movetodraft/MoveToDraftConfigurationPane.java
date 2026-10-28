package org.wpcleaner.application.gui.javafx.core.action.content.movetodraft;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.api.utils.GT;

public final class MoveToDraftConfigurationPane extends TitledPane {

  public static final String SUB_PAGE_NAME_ID = "sub-page-name";
  public static final String SUMMARY_ID = "summary";

  private static final double LABEL_WIDTH = 120.0;

  private final TextField subPageNameField;
  private final TextField summaryField;

  public MoveToDraftConfigurationPane(
      final String title, @Nullable final MoveToDraftConfiguration initialConfig) {
    super();
    setText(title);
    setCollapsible(false);
    setMaxHeight(Double.MAX_VALUE);

    this.subPageNameField = new TextField();
    this.subPageNameField.setId(SUB_PAGE_NAME_ID);
    this.summaryField = new TextField();
    this.summaryField.setId(SUMMARY_ID);

    if (initialConfig != null) {
      this.subPageNameField.setText(initialConfig.subPageName());
      this.summaryField.setText(initialConfig.summary());
    }

    final GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);
    grid.setMaxHeight(Double.MAX_VALUE);

    final Label summaryLabel = new Label(GT._T("Summary:"));
    summaryLabel.setMinWidth(LABEL_WIDTH);
    summaryLabel.setPrefWidth(LABEL_WIDTH);
    grid.add(summaryLabel, 0, 0);
    grid.add(summaryField, 1, 0);
    GridPane.setHgrow(summaryField, Priority.ALWAYS);

    final Label subPageNameLabel = new Label(GT._T("Sub-page name:"));
    subPageNameLabel.setMinWidth(LABEL_WIDTH);
    subPageNameLabel.setPrefWidth(LABEL_WIDTH);
    grid.add(subPageNameLabel, 0, 1);
    grid.add(subPageNameField, 1, 1);
    GridPane.setHgrow(subPageNameField, Priority.ALWAYS);

    setContent(grid);
  }

  public MoveToDraftConfiguration getMoveToDraftConfiguration() {
    return new MoveToDraftConfiguration(subPageNameField.getText(), summaryField.getText());
  }

  public String getSubPageName() {
    return subPageNameField.getText();
  }

  public String getSummary() {
    return summaryField.getText();
  }

  public boolean isComplete() {
    return !subPageNameField.getText().isBlank() && !summaryField.getText().isBlank();
  }
}
