package org.wpcleaner.application.gui.javafx.analysis;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.wpcleaner.api.utils.GT;
import org.wpcleaner.application.gui.javafx.JavaFxImageLoader;
import org.wpcleaner.application.gui.javafx.core.control.DefaultStyles;
import org.wpcleaner.lib.image.ImageCollection;
import org.wpcleaner.lib.image.ImageSize;

public final class PageSavePane extends HBox {

  public static final String SAVE_ID = "save";
  public static final String SUMMARY_ID = "summary";

  private static final double SPACING = 5;

  private final Button saveButton;
  private final TextField summaryField;
  private String automaticSummary = "";

  public PageSavePane(
      final JavaFxImageLoader imageLoader, final boolean demo, final Runnable saveAction) {
    super(SPACING);
    this.saveButton = new Button();
    this.summaryField = new TextField();
    initialize(imageLoader, demo, saveAction);
  }

  private void initialize(
      final JavaFxImageLoader imageLoader, final boolean demo, final Runnable saveAction) {
    setAlignment(Pos.CENTER_LEFT);
    setPadding(new Insets(SPACING));
    summaryField.setId(SUMMARY_ID);
    summaryField.setPromptText(GT._T("Summary"));
    setHgrow(summaryField, Priority.ALWAYS);
    saveButton.setId(SAVE_ID);
    saveButton.setStyle(DefaultStyles.TOOLBAR_ELEMENT);
    imageLoader
        .getImageView(ImageCollection.DOCUMENT_SEND, ImageSize.BUTTON)
        .ifPresent(saveButton::setGraphic);
    saveButton.setTooltip(new Tooltip(GT._T("Save the page")));
    saveButton.setDisable(demo);
    saveButton.setOnAction(_ -> saveAction.run());
    getChildren().addAll(summaryField, new ToolBar(saveButton));
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
