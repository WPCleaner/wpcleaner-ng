package org.wpcleaner.application.gui.javafx.analysis;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.wpcleaner.api.utils.GT;

public final class PageSummaryPane extends HBox {

  public static final String SUMMARY_ID = "summary";

  private static final double SPACING = 5;

  private final TextField summaryField;
  private String automaticSummary = "";

  public PageSummaryPane() {
    super(SPACING);
    this.summaryField = new TextField();
    initialize();
  }

  private void initialize() {
    setAlignment(Pos.CENTER_LEFT);
    setPadding(new Insets(SPACING));
    summaryField.setId(SUMMARY_ID);
    summaryField.setPromptText(GT._T("Summary"));
    setHgrow(summaryField, Priority.ALWAYS);
    getChildren().add(summaryField);
  }

  public String getSummary() {
    return summaryField.getText();
  }

  public void updateAutomaticSummary(final String summary) {
    if (summaryField.getText().equals(automaticSummary)) {
      summaryField.setText(summary);
    }
    automaticSummary = summary;
  }

  public void resetSummary() {
    summaryField.setText("");
    automaticSummary = "";
  }
}
