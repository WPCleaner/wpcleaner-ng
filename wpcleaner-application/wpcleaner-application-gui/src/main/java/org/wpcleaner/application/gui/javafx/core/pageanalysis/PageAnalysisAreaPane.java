package org.wpcleaner.application.gui.javafx.core.pageanalysis;

/*
 * SPDX-FileCopyrightText: © 2026 Nicolas Vervelle <[WPCleaner](https://github.com/WPCleaner)>
 * SPDX-License-Identifier: Apache-2.0
 */

import java.util.List;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.wpcleaner.application.gui.javafx.JavaFxImageLoader;
import org.wpcleaner.application.gui.javafx.core.pageanalysis.coloration.PageSyntaxColorizer;
import org.wpcleaner.lib.check.CheckError;

public final class PageAnalysisAreaPane extends VBox {

  private final PageAnalysisScrollPane scrollPane;
  private final CheckErrorsToolBar toolBar;

  public PageAnalysisAreaPane(
      final PageSyntaxColorizer colorizer, final JavaFxImageLoader imageLoader) {
    super();
    this.scrollPane = new PageAnalysisScrollPane(colorizer);
    this.toolBar = new CheckErrorsToolBar(imageLoader, scrollPane.getArea());
    setVgrow(scrollPane, Priority.ALWAYS);
    getChildren().addAll(toolBar, scrollPane);
  }

  public CheckErrorsToolBar getToolBar() {
    return toolBar;
  }

  public PageAnalysisArea getArea() {
    return scrollPane.getArea();
  }

  public void highlight(final List<CheckError> errors) {
    getArea().highlight(errors);
    toolBar.updateErrors(errors);
  }
}
