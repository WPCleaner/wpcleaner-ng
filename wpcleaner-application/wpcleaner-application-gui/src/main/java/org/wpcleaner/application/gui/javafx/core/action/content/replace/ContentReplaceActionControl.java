package org.wpcleaner.application.gui.javafx.core.action.content.replace;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Objects;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import org.wpcleaner.api.utils.GT;

public final class ContentReplaceActionControl extends TitledPane {

  public static final String SUMMARY_ID = "summary";
  public static final String TEXT_ID = "text";

  private static final double LABEL_WIDTH = 120.0;

  private final TextField summaryField;
  private final TextArea textArea;

  public ContentReplaceActionControl(
      final String title, final ContentReplaceConfiguration initialConfig) {
    super();
    Objects.requireNonNull(initialConfig, "initialConfig");

    setCollapsible(false);
    setMaxHeight(Double.MAX_VALUE);
    setMaxWidth(Double.MAX_VALUE);
    setText(title);

    this.summaryField = new TextField(initialConfig.summary());
    this.summaryField.setId(SUMMARY_ID);
    this.textArea = new TextArea(initialConfig.text());
    this.textArea.setId(TEXT_ID);
    this.textArea.setEditable(false);
    this.textArea.setPrefRowCount(3);

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

    final Label staticTextLabel = new Label(GT._T("Replacement:"));
    staticTextLabel.setMinWidth(LABEL_WIDTH);
    staticTextLabel.setPrefWidth(LABEL_WIDTH);
    grid.add(staticTextLabel, 0, 1);
    grid.add(textArea, 1, 1);
    GridPane.setHgrow(textArea, Priority.ALWAYS);
    GridPane.setVgrow(textArea, Priority.ALWAYS);

    setContent(grid);
  }

  public ContentReplaceConfiguration getContentReplaceConfiguration() {
    return new ContentReplaceConfiguration(summaryField.getText(), textArea.getText());
  }

  public String getSummary() {
    return summaryField.getText();
  }

  public boolean isComplete() {
    return !summaryField.getText().isBlank();
  }
}
