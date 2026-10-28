package org.wpcleaner.application.gui.javafx.core.action.content.replace;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import org.jspecify.annotations.Nullable;
import org.wpcleaner.api.utils.GT;

public final class ContentReplaceConfigurationPane extends TitledPane {

  public static final String SUMMARY_ID = "summary";
  public static final String TEXT_ID = "text";

  private static final double LABEL_WIDTH = 120.0;

  private final TextField summaryField;
  private final TextArea textArea;

  public ContentReplaceConfigurationPane(
      final String title, @Nullable final ContentReplaceConfiguration initialConfig) {
    super();
    setText(title);
    setCollapsible(false);
    setMaxHeight(Double.MAX_VALUE);

    this.summaryField = new TextField();
    this.summaryField.setId(SUMMARY_ID);
    this.textArea = new TextArea();
    this.textArea.setId(TEXT_ID);
    this.textArea.setPrefRowCount(4);

    if (initialConfig != null) {
      this.summaryField.setText(initialConfig.summary());
      this.textArea.setText(initialConfig.text());
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

    final Label textLabel = new Label(GT._T("Replacement:"));
    textLabel.setMinWidth(LABEL_WIDTH);
    textLabel.setPrefWidth(LABEL_WIDTH);
    grid.add(textLabel, 0, 1);
    grid.add(textArea, 1, 1);
    GridPane.setHgrow(textArea, Priority.ALWAYS);
    GridPane.setVgrow(textArea, Priority.ALWAYS);

    setContent(grid);
  }

  public ContentReplaceConfiguration getContentReplaceConfiguration() {
    return new ContentReplaceConfiguration(summaryField.getText(), textArea.getText());
  }

  public boolean isComplete() {
    return !summaryField.getText().isBlank() && !textArea.getText().isBlank();
  }
}
