package org.wpcleaner.application.gui.javafx.core.action.content.movetodraft;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.Objects;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import org.wpcleaner.api.utils.GT;
import org.wpcleaner.application.gui.javafx.JavaFxImageLoader;
import org.wpcleaner.application.gui.javafx.core.control.DefaultStyles;
import org.wpcleaner.lib.image.ImageCollection;
import org.wpcleaner.lib.image.ImageLoader;
import org.wpcleaner.lib.image.ImageSize;

public final class MoveToDraftActionControl extends TitledPane {

  public static final String REFRESH_ID = "refresh";
  public static final String SUB_PAGE_NAME_ID = "sub-page-name";
  public static final String SUB_PAGE_NAME_LABEL_ID = "sub-page-name-label";
  public static final String SUMMARY_ID = "summary";
  public static final String SUMMARY_LABEL_ID = "summary-label";

  private static final double LABEL_WIDTH = 120.0;

  private final TextField subPageNameField;
  private final TextField summaryField;

  public MoveToDraftActionControl(
      final ImageLoader imageLoader,
      final String title,
      final MoveToDraftConfiguration initialConfig,
      final Runnable onRefresh) {
    super();
    Objects.requireNonNull(initialConfig, "initialConfig");

    setCollapsible(false);
    setMaxHeight(Double.MAX_VALUE);
    setMaxWidth(Double.MAX_VALUE);
    setText(title);

    this.summaryField = new TextField(initialConfig.summary());
    this.summaryField.setId(SUMMARY_ID);
    this.subPageNameField = new TextField(initialConfig.subPageName());
    this.subPageNameField.setId(SUB_PAGE_NAME_ID);
    this.subPageNameField.setOnAction(_ -> onRefresh.run());

    final Button refreshButton = new Button();
    refreshButton.setId(REFRESH_ID);
    refreshButton.setStyle(DefaultStyles.TOOLBAR_ELEMENT);
    final JavaFxImageLoader fxImageLoader =
        new JavaFxImageLoader(Objects.requireNonNull(imageLoader, "imageLoader"));
    fxImageLoader
        .getImageView(ImageCollection.REFRESH, ImageSize.BUTTON)
        .ifPresent(refreshButton::setGraphic);
    refreshButton.setTooltip(new Tooltip(GT._T("Refresh draft page content")));
    refreshButton.setOnAction(_ -> onRefresh.run());

    final GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);
    grid.setMaxHeight(Double.MAX_VALUE);

    final Label summaryLabel = new Label(GT._T("Summary:"));
    summaryLabel.setId(SUMMARY_LABEL_ID);
    summaryLabel.setMinWidth(LABEL_WIDTH);
    summaryLabel.setPrefWidth(LABEL_WIDTH);
    grid.add(summaryLabel, 0, 0);
    grid.add(summaryField, 1, 0, 2, 1);
    GridPane.setHgrow(summaryField, Priority.ALWAYS);

    final Label subPageNameLabel = new Label(GT._T("Sub-page name:"));
    subPageNameLabel.setId(SUB_PAGE_NAME_LABEL_ID);
    subPageNameLabel.setMinWidth(LABEL_WIDTH);
    subPageNameLabel.setPrefWidth(LABEL_WIDTH);
    grid.add(subPageNameLabel, 0, 1);
    grid.add(subPageNameField, 1, 1);
    GridPane.setHgrow(subPageNameField, Priority.ALWAYS);
    grid.add(refreshButton, 2, 1);

    setContent(grid);
  }

  public String getSummary() {
    return summaryField.getText();
  }

  public String getSubPageName() {
    return subPageNameField.getText();
  }

  public boolean isComplete() {
    return !summaryField.getText().isBlank() && !subPageNameField.getText().isBlank();
  }
}
