package org.wpcleaner.application.gui.javafx.core.action.content;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.wpcleaner.api.analysis.PageAnalysisFactory;
import org.wpcleaner.application.gui.javafx.core.pageanalysis.PageAnalysisArea;
import org.wpcleaner.application.gui.javafx.core.pageanalysis.PageAnalysisScrollPane;
import org.wpcleaner.application.gui.javafx.core.pageanalysis.coloration.PageSyntaxColorizer;

public final class PageContentDisplay extends VBox {

  public static final String CONTENT_ID = "content";
  public static final String TITLE_ID = "title";

  private final Label label;
  private final PageAnalysisScrollPane scrollPane;

  public PageContentDisplay(
      final PageSyntaxColorizer colorizer,
      final PageAnalysisFactory pageAnalysisFactory,
      final String pageTitle,
      final String pageContent) {
    super(5);
    setMaxHeight(Double.MAX_VALUE);
    setMaxWidth(Double.MAX_VALUE);
    setPrefWidth(0);
    HBox.setHgrow(this, Priority.ALWAYS);

    this.label = new Label(pageTitle);
    this.label.setId(TITLE_ID);
    this.scrollPane = new PageAnalysisScrollPane(colorizer);
    this.scrollPane.setId(CONTENT_ID);
    final PageAnalysisArea previewArea = scrollPane.getArea();
    previewArea.setPrefHeight(700);
    previewArea.updateText(pageTitle, pageContent, pageAnalysisFactory);
    setVgrow(scrollPane, Priority.ALWAYS);

    getChildren().addAll(label, scrollPane);
  }

  public PageAnalysisArea getArea() {
    return scrollPane.getArea();
  }

  public PageAnalysisScrollPane getScrollPane() {
    return scrollPane;
  }

  public void updateText(
      final String pageTitle,
      final String pageContent,
      final PageAnalysisFactory pageAnalysisFactory) {
    label.setText(pageTitle);
    scrollPane.getArea().updateText(pageTitle, pageContent, pageAnalysisFactory);
  }
}
